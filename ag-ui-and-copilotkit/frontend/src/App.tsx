import {
  CopilotKitProvider,
  CopilotChat,
} from "@copilotkit/react-core/v2";

import { HttpAgent } from "@ag-ui/client";

import "@copilotkit/react-core/v2/styles.css";

const agent = new HttpAgent({
  url: "/agent",
});

export default function App() {
  return (
    <CopilotKitProvider
      agents__unsafe_dev_only={{
        spring: agent,
      }}
      agentId="spring"
    >
      <div style={{ height: "100vh" }}>
        <CopilotChat />
      </div>
    </CopilotKitProvider>
  );
}