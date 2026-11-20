#!/bin/zsh
#
# Runs the tests of every Gradle project in this repo and prints a summary.
#
#   scripts/runtests.sh                 # all projects
#   scripts/runtests.sh mcp rag         # only projects whose path contains "mcp" or "rag"
#   scripts/runtests.sh --clean         # force tests to rerun (cleanTest --rerun-tasks)
#   scripts/runtests.sh --parallel 4    # run 4 projects at once (default 1)
#
# Notes:
#  - Projects are discovered by finding gradlew files, so new ones are picked up.
#  - Tests that need Docker (Redis, Mongo, Postgres, Cassandra, Neo4j, Qdrant)
#    are skipped automatically when Docker isn't available.
#  - Per-project output goes to <project>/build/runtests.log.
#  - Exit status is non-zero if any project fails.

SCRIPT=${0:A}
ROOT=${SCRIPT:h:h}   # repo root (this script lives in scripts/)
cd "$ROOT" || exit 1

# --- internal mode: run one project (used for parallel execution) ----------
if [[ "$1" == "--one" ]]; then
  proj=$2; clean=$3
  args=(test -q)
  [[ "$clean" == "1" ]] && args=(cleanTest test --rerun-tasks -q)
  mkdir -p "$proj/build"
  ( cd "$proj" && ./gradlew "${args[@]}" > build/runtests.log 2>&1 )
  rc=$?
  counts=$(grep -ho 'tests="[0-9]*" skipped="[0-9]*" failures="[0-9]*" errors="[0-9]*"' \
      "$proj"/build/test-results/test/*.xml 2>/dev/null \
    | sed 's/[a-z]*="//g; s/"//g' \
    | awk '{t+=$1; s+=$2; f+=$3+$4} END {printf "%d %d %d", t, t-s-f, f}')
  [[ -z "$counts" ]] && counts="0 0 0"
  print -r -- "$proj $rc $counts"
  exit 0
fi

clean=0; parallel=1; filters=()
while (( $# )); do
  case "$1" in
    --clean) clean=1 ;;
    --parallel) shift; parallel=$1 ;;
    -h|--help) sed -n '2,16p' "$0"; exit 0 ;;
    *) filters+=("$1") ;;
  esac
  shift
done

projects=("${(@f)$(find . -name gradlew -not -path '*/node_modules/*' -not -path '*/build/*' \
  | sed 's|^\./||; s|/gradlew$||' | sort)}")

if (( ${#filters} )); then
  selected=()
  for p in $projects; do
    for f in $filters; do
      if [[ "$p" == *"$f"* ]]; then selected+=("$p"); break; fi
    done
  done
  projects=("${selected[@]}")
fi

if (( ${#projects} == 0 )); then echo "No matching projects."; exit 1; fi
echo "Running tests for ${#projects} project(s) (parallel=$parallel)..."

results=$(print -rl -- $projects \
  | xargs -P "$parallel" -I{} "$SCRIPT" --one {} "$clean")

printf "\n%-52s %7s %7s %7s\n" PROJECT TESTS PASSED FAILED
failed=()
while read -r proj rc total passed fail; do
  flag=""
  if (( rc != 0 )); then flag="  <-- FAILED"; failed+=("$proj"); fi
  printf "%-52s %7s %7s %7s%s\n" "$proj" "$total" "$passed" "$fail" "$flag"
done <<< "$(print -r -- "$results" | sort)"

echo
if (( ${#failed} )); then
  echo "${#failed} project(s) failed:"
  for p in $failed; do echo "  - $p  (see $p/build/runtests.log)"; done
  exit 1
fi
echo "All projects passed."
