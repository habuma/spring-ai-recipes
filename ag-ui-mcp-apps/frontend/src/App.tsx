import {
  CopilotKitProvider,
  CopilotChat,
  useRenderTool,
} from "@copilotkit/react-core/v2";

import { HttpAgent } from "@ag-ui/client";

import "@copilotkit/react-core/v2/styles.css";

const agent = new HttpAgent({
  url: "/agent",
});

// MCP tools that have an MCP App are shown by the app itself (via the
// "mcp-apps" activity), so the tool call needs no renderer of its own.
function HideToolCalls() {
  useRenderTool({ name: "*", render: () => <></> });
  return null;
}

export default function App() {
  return (
    <CopilotKitProvider
      agents__unsafe_dev_only={{
        spring: agent,
      }}
      agentId="spring"
    >
      <HideToolCalls />
      <div style={{ height: "100vh" }}>
        <CopilotChat />
      </div>
    </CopilotKitProvider>
  );
}