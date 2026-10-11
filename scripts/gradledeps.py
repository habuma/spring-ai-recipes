"""Shared helpers for check-updates and update-deps: find Gradle projects,
extract versioned dependencies from build.gradle, look up latest versions."""
import re
import sys
import urllib.request
import xml.etree.ElementTree as ET
from dataclasses import dataclass
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
CENTRAL = "https://repo1.maven.org/maven2"
PLUGIN_PORTAL = "https://plugins.gradle.org/m2"

UNSTABLE = re.compile(r"(?i)(snapshot|alpha|beta|preview|[.-]m\d+$|[.-]rc\d*$|[.-]cr\d+$|[.-]ea$)")

PLUGIN_RE = re.compile(r"""id\s*\(?\s*['"]([\w.\-]+)['"]\s*\)?\s*version\s*\(?\s*['"]([^'"]+)['"]""")
VARDEF_RE = re.compile(r"""(?:set\(\s*['"](\w+)['"]\s*,\s*|\b(\w+Version)\s*=\s*)['"]([^'"$]+)['"]""")
LITERAL_RE = re.compile(r"""['"]([\w.\-]+):([\w.\-]+):(\d[^'"$:@]*)['"]""")
VARUSE_RE = re.compile(r"""['"]([\w.\-]+):([\w.\-]+):\$\{?(\w+)\}?['"]""")


@dataclass
class Ref:
    """One editable version string in a build file."""
    file: Path
    start: int          # offset of version text within file
    end: int
    current: str
    group: str          # maven group (or plugin id)
    artifact: str       # maven artifact (or plugin marker artifact)
    label: str          # human-readable name
    kind: str           # 'plugin' | 'dependency' | 'variable'

    @property
    def coord(self):
        return f"{self.group}:{self.artifact}"


def find_projects():
    """Every directory containing a build.gradle (excluding build output)."""
    found = [p.parent for p in ROOT.rglob("build.gradle")
             if not {"build", "node_modules", ".gradle", ".git"} & set(p.relative_to(ROOT).parts[:-1])]
    return sorted(found)


def resolve_projects(name):
    """Resolve a folder name to projects: exact relative path, then a group
    folder containing projects, then any project whose folder name matches."""
    projects = find_projects()
    if not name:
        return projects
    name = name.strip("/")
    exact = [p for p in projects if str(p.relative_to(ROOT)) == name]
    if exact:
        return exact
    group = [p for p in projects if str(p.relative_to(ROOT)).startswith(name + "/")]
    if group:
        return group
    return [p for p in projects if p.name == name]


def _code_spans(text):
    """Yield (offset, line) for non-comment lines."""
    off = 0
    for line in text.splitlines(keepends=True):
        if not line.lstrip().startswith("//"):
            yield off, line
        off += len(line)


def extract_refs(project):
    f = project / "build.gradle"
    text = f.read_text()
    lines = list(_code_spans(text))

    # variable -> maven coordinate, from `mavenBom "g:a:${v}"` / `"g:a:$v"` usages
    var_coord = {}
    for _, line in lines:
        for g, a, v in VARUSE_RE.findall(line):
            var_coord.setdefault(v, (g, a))

    refs = []
    for off, line in lines:
        for m in PLUGIN_RE.finditer(line):
            pid = m.group(1)
            refs.append(Ref(f, off + m.start(2), off + m.end(2), m.group(2), pid,
                            f"{pid}.gradle.plugin", pid, "plugin"))
        for m in VARDEF_RE.finditer(line):
            var = m.group(1) or m.group(2)
            if var in var_coord:
                g, a = var_coord[var]
                refs.append(Ref(f, off + m.start(3), off + m.end(3), m.group(3), g, a,
                                f"{g}:{a} ({var})", "variable"))
        for m in LITERAL_RE.finditer(line):
            refs.append(Ref(f, off + m.start(3), off + m.end(3), m.group(3), m.group(1),
                            m.group(2), f"{m.group(1)}:{m.group(2)}", "dependency"))
    return refs


# ---------- version lookup ----------

_cache = {}


def _fetch_versions(ref):
    key = (ref.kind == "plugin", ref.coord)
    if key in _cache:
        return _cache[key]
    path = ref.group.replace(".", "/") + "/" + ref.artifact
    bases = [PLUGIN_PORTAL, CENTRAL] if ref.kind == "plugin" else [CENTRAL, PLUGIN_PORTAL]
    versions = None
    for base in bases:
        try:
            with urllib.request.urlopen(f"{base}/{path}/maven-metadata.xml", timeout=20) as r:
                root = ET.fromstring(r.read())
            versions = [v.text for v in root.iter("version")]
            break
        except Exception:
            continue
    _cache[key] = versions
    return versions


def _shape(v):
    return re.sub(r"\d+", "N", v)


def _key(v):
    return tuple(int(x) for x in re.findall(r"\d+", v))


def latest_version(ref):
    """Latest stable version with the same version 'shape' (e.g. keeps a
    '.Final' suffix); a prerelease current version may move to prereleases."""
    versions = _fetch_versions(ref)
    if versions is None:
        return None
    cur_stable = not UNSTABLE.search(ref.current)
    cands = [v for v in versions if _shape(v) == _shape(ref.current)
             and (not cur_stable or not UNSTABLE.search(v))]
    return max(cands, key=_key, default=ref.current)


def bump_kind(old, new):
    o, n = _key(old), _key(new)
    if n <= o:
        return None
    if n[0] != o[0]:
        return "major"
    if len(n) > 1 and len(o) > 1 and n[1] != o[1]:
        return "minor"
    return "patch"


def find_updates(project, allow_major=True):
    """Return [(ref, latest, kind)] for refs with newer versions, plus lookup failures."""
    updates, failures = [], []
    for ref in extract_refs(project):
        latest = latest_version(ref)
        if latest is None:
            failures.append(ref)
            continue
        kind = bump_kind(ref.current, latest)
        if kind and (allow_major or kind != "major"):
            updates.append((ref, latest, kind))
    return updates, failures


def apply_updates(updates):
    """Rewrite build files in place. Returns {file: original_text} for rollback."""
    originals = {}
    by_file = {}
    for ref, latest, _ in updates:
        by_file.setdefault(ref.file, []).append((ref, latest))
    for f, items in by_file.items():
        text = originals[f] = f.read_text()
        for ref, latest in sorted(items, key=lambda i: i[0].start, reverse=True):
            text = text[:ref.start] + latest + text[ref.end:]
        f.write_text(text)
    return originals


def rel(p):
    return str(p.relative_to(ROOT))


def err(*a):
    print(*a, file=sys.stderr)
