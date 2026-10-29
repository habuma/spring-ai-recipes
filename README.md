# Spring AI Recipes

This repository holds the code for the examples presented in Spring AI
Recipes that I post periodically. Find all Spring AI Recipes published so far at
https://www.habuma.com/springairecipes/.

## Projects

Each project is a standalone Gradle build (`./gradlew bootRun`, or `./gradlew build`).
Some recipes span several projects (for example, a client and a server); these
are folders that contain a project per subfolder.

| Project | Recipe |
| --- | --- |
| [`a2a-client`](a2a-client/) | [Invoking A2A Sub-Agents with TaskTool](https://thetalkingapp.medium.com/spring-ai-recipe-invoking-a2a-sub-agents-with-tasktool-e4f1a4b92b5f) |
| [`a2a-server`](a2a-server/) | [Enabling Agent-to-Agent Communication with A2A](https://thetalkingapp.medium.com/spring-ai-recipe-enabling-agent-to-agent-communication-with-a2a-2cdd97794a6f) |
| [`ag-ui`](ag-ui/) | [Adding a Web UI with AG-UI and CopilotKit](https://thetalkingapp.medium.com/spring-ai-recipe-adding-a-web-ui-with-ag-ui-and-copilotkit-4922d6a0b441) |
| [`ag-ui-mcp-apps`](ag-ui-mcp-apps/) | [Rendering MCP Apps UIs in an AG-UI/CopilotKit Client](https://medium.com/@thetalkingapp/spring-ai-recipe-rendering-mcp-apps-uis-in-an-ag-ui-copilotkit-client-2e8745e66579) |
| [`ask-user-question`](ask-user-question/) | [Asking Questions to the User](https://thetalkingapp.medium.com/spring-ai-recipe-asking-questions-to-the-user-99ec1a0e2394) |
| [`chatclient-and-react`](chatclient-and-react/) | [Reasoning and Acting (ReAct)](https://medium.com/@thetalkingapp/spring-ai-recipe-reasoning-and-acting-react-30d364955c2f) |
| [`chatclientcustomizer`](chatclientcustomizer/) | [Composing ChatClient Behavior](https://thetalkingapp.medium.com/spring-ai-recipe-composing-chatclient-behavior-5f643e727781) |
| [`chatloop`](chatloop/) | [Building a Text-Based Chat Loop Around ChatClient](https://thetalkingapp.medium.com/spring-ai-recipe-building-a-text-based-chat-loop-around-chatclient-444573e4106c) |
| [`claude-style-subagents`](claude-style-subagents/) | [Defining Claude-Style Subagents](https://thetalkingapp.medium.com/spring-ai-recipe-defining-claude-style-subagents-9563644b9121) |
| [`conversation-aware-rag`](conversation-aware-rag/) | [Making RAG Conversation-Aware](https://medium.com/@thetalkingapp/spring-ai-recipe-making-rag-conversation-aware-189b82a37060) |
| [`decomposed-claim-checking`](decomposed-claim-checking/) | [Decomposing Answers into Claims for Fact-Checking](https://medium.com/@thetalkingapp/spring-ai-recipe-decomposing-answers-into-claims-for-fact-checking-49861bed1374) |
| [`embabel-hello`](embabel-hello/) | [Getting Started with Embabel](https://thetalkingapp.medium.com/spring-ai-recipe-getting-started-with-embabel-9fad29eba178) |
| [`embabel-hitl`](embabel-hitl/) | [Adding Human-in-the-Loop to an Embabel Agent](https://medium.com/@thetalkingapp/spring-ai-recipe-adding-human-in-the-loop-to-an-embabel-agent-8781154dfd9c) |
| [`embabel-invoke-programmatically`](embabel-invoke-programmatically/) | [Invoking an Embabel Agent in Java](https://medium.com/@thetalkingapp/spring-ai-recipe-invoking-an-embabel-agent-in-java-32f7b6ccaf7d) |
| [`embabel-state`](embabel-state/) | [Modeling Agent State in Embabel](https://medium.com/@thetalkingapp/spring-ai-recipe-modeling-agent-state-in-embabel-3c2ae05eb104) |
| [`essential-rag`](essential-rag/) | [Essential RAG](https://medium.com/@thetalkingapp/spring-ai-recipe-essential-rag-c1798773668b) |
| [`fact-checking-evaluator`](fact-checking-evaluator/) | [Fact-Checking AI Responses](https://medium.com/@thetalkingapp/spring-ai-recipe-fact-checking-ai-responses-43de77740493) |
| [`graph-workflow`](graph-workflow/) | [Building a Graph-Based Agentic Workflow](https://thetalkingapp.medium.com/spring-ai-recipe-building-a-graph-based-agentic-workflow-becfae64170a) |
| [`graph-workflow-hitl`](graph-workflow-hitl/) | [Adding Human-in-the-Loop to a Graph-based Agentic Workflow](https://thetalkingapp.medium.com/spring-ai-recipe-adding-human-in-the-loop-to-a-graph-based-agentic-workflow-4a826cf89902) |
| [`graph-workflow-langgraph4j`](graph-workflow-langgraph4j/) | [Building a Graph-Based Agentic Workflow with LangGraph4j](https://medium.com/@thetalkingapp/spring-ai-recipe-building-a-graph-based-agentic-workflow-with-langgraph4j-0fbfcce25a57) |
| [`graph-workflow-loop`](graph-workflow-loop/) | [Adding a Loop to a Graph-Based Workflow](https://thetalkingapp.medium.com/spring-ai-recipe-adding-a-loop-to-a-graph-based-workflow-e062040e0440), [Visualizing Spring AI Alibaba Graph Workflows](https://medium.com/@thetalkingapp/spring-ai-recipe-visualizing-spring-ai-alibaba-graph-workflows-7784465d2afc) |
| [`hybrid-rag`](hybrid-rag/) | [Better RAG Results with Hybrid Search](https://thetalkingapp.medium.com/spring-ai-recipe-better-rag-results-with-hybrid-search-6e6ab2d09003) |
| [`hyde-rag`](hyde-rag/) | [Better RAG Results with HyDE](https://medium.com/@thetalkingapp/spring-ai-recipe-better-rag-results-with-hyde-1d2d360a1b58) |
| [`jbang-chat-loop`](jbang-chat-loop/) | [A No-Build Chat Loop with JBang](https://medium.com/@thetalkingapp/spring-ai-recipe-a-no-build-chat-loop-with-jbang-36cee6039980) |
| [`jbang-mcp`](jbang-mcp/) | [No-Build MCP with JBang](https://medium.com/@thetalkingapp/spring-ai-recipe-no-build-mcp-with-jbang-8b26ce148cf1) |
| [`local-embeddings`](local-embeddings/) | [Speeding up RAG with Faster Embeddings](https://medium.com/@thetalkingapp/spring-ai-recipe-speeding-up-rag-with-faster-embeddings-952bcc242b0e) |
| [`local-llms/dockermodelrunner`](local-llms/dockermodelrunner/) | [Running Against Local LLMs](https://medium.com/@thetalkingapp/spring-ai-recipe-running-against-local-llms-3b7af32f035b) |
| [`local-llms/lmstudio`](local-llms/lmstudio/) | [Running Against Local LLMs](https://medium.com/@thetalkingapp/spring-ai-recipe-running-against-local-llms-3b7af32f035b) |
| [`local-llms/modeljars`](local-llms/modeljars/) | [Running Against an in-JVM Model](https://medium.com/@thetalkingapp/spring-ai-recipe-running-against-an-in-jvm-model-70311a054eaf) |
| [`local-llms/ollama`](local-llms/ollama/) | [Running Against Local LLMs](https://medium.com/@thetalkingapp/spring-ai-recipe-running-against-local-llms-3b7af32f035b) |
| [`local-llms/qxotic`](local-llms/qxotic/) | [Running LLMs in the JVM with Qxotic](https://medium.com/@thetalkingapp/spring-ai-recipe-running-llms-in-the-jvm-with-qxotic-b9a8c4405e88) |
| [`logging-requests-and-responses`](logging-requests-and-responses/) | [Logging LLM Requests and Responses](https://thetalkingapp.medium.com/spring-ai-recipe-logging-llm-requests-and-responses-c1adfba50082) |
| [`longterm-memory`](longterm-memory/) | [Enabling Long-Term Memory](https://thetalkingapp.medium.com/spring-ai-recipe-enabling-long-term-memory-49186aa60cb0) |
| [`mcp-apps`](mcp-apps/) | [Rendering Rich UIs with MCP Apps](https://thetalkingapp.medium.com/spring-ai-recipe-rendering-rich-uis-with-mcp-apps-fd72569c11de) |
| [`mcp-client`](mcp-client/) | [Creating an MCP Client](https://thetalkingapp.medium.com/spring-ai-recipe-creating-an-mcp-client-9691ddc42dfd) |
| [`mcp-elicitation`](mcp-elicitation/) | [MCP Elicitation](https://medium.com/@thetalkingapp/spring-ai-recipe-mcp-elicitation-cdfed4dd213e) |
| [`mcp-logging`](mcp-logging/) | [MCP Logging](https://medium.com/@thetalkingapp/spring-ai-recipe-mcp-logging-f7ad3041a249) |
| [`mcp-prompt-completions`](mcp-prompt-completions/) | [MCP Prompt Completions](https://medium.com/@thetalkingapp/spring-ai-recipe-mcp-prompt-completions-2a8cbc97b8f3) |
| [`mcp-prompts`](mcp-prompts/) | [Defining MCP Prompts](https://medium.com/@thetalkingapp/spring-ai-recipe-defining-mcp-prompts-6a1f2302aeae) |
| [`mcp-sampling`](mcp-sampling/) | [MCP Sampling](https://medium.com/@thetalkingapp/spring-ai-recipe-mcp-sampling-87b3db2a58da) |
| [`mcp-tool-filtering`](mcp-tool-filtering/) | [Controlling MCP Tool Visibility](https://thetalkingapp.medium.com/spring-ai-recipe-controlling-mcp-tool-visibility-18258e66f278) |
| [`mcp-tool-progress`](mcp-tool-progress/) | [MCP Tool Progress](https://medium.com/@thetalkingapp/spring-ai-recipe-mcp-tool-progress-474a080621e5) |
| [`multi-llm`](multi-llm/) | [Using More than One LLM](https://medium.com/@thetalkingapp/spring-ai-recipes-using-more-than-one-llm-76e3e2ead908) |
| [`output-safeguard-advisor`](output-safeguard-advisor/) | [Safeguarding LLM Output](https://medium.com/@thetalkingapp/spring-ai-recipe-safeguarding-llm-output-61f53af2392b) |
| [`persistent-chat-memory/cassandra-memory`](persistent-chat-memory/cassandra-memory/) | [3 More Ways to Persist Chat Memory](https://medium.com/@thetalkingapp/spring-ai-recipe-3-more-ways-to-persist-chat-memory-289cfb995495) |
| [`persistent-chat-memory/jdbc-memory`](persistent-chat-memory/jdbc-memory/) | [Persisting Chat Memory with JDBC](https://medium.com/@thetalkingapp/spring-ai-recipe-persisting-chat-memory-with-jdbc-564609f8b2f7) |
| [`persistent-chat-memory/mongo-memory`](persistent-chat-memory/mongo-memory/) | [3 More Ways to Persist Chat Memory](https://medium.com/@thetalkingapp/spring-ai-recipe-3-more-ways-to-persist-chat-memory-289cfb995495) |
| [`persistent-chat-memory/redis-memory`](persistent-chat-memory/redis-memory/) | [3 More Ways to Persist Chat Memory](https://medium.com/@thetalkingapp/spring-ai-recipe-3-more-ways-to-persist-chat-memory-289cfb995495) |
| [`rag-with-metadata`](rag-with-metadata/) | [Filtering RAG Results with Metadata](https://medium.com/@thetalkingapp/spring-ai-recipe-filtering-rag-results-with-metadata-bef2f8a7cb72) |
| [`rerank-rag`](rerank-rag/) | [Better RAG Results with Reranking](https://medium.com/@thetalkingapp/spring-ai-recipe-better-rag-results-with-reranking-37c76fb325da) |
| [`safeguard-advisor`](safeguard-advisor/) | [Applying Input Guardrails with SafeGuardAdvisor](https://medium.com/@thetalkingapp/spring-ai-recipe-applying-input-guardrails-with-safeguardadvisor-6177541fb22f) |
| [`secured-mcp-server-api-key`](secured-mcp-server-api-key/) | [Securing an MCP Server with an API Key](https://thetalkingapp.medium.com/spring-ai-recipe-securing-an-mcp-server-with-an-api-key-0a4b84fdf0dc) |
| [`secured-mcp-server-oauth`](secured-mcp-server-oauth/) | [Securing an MCP Server with OAuth](https://thetalkingapp.medium.com/spring-ai-recipe-securing-an-mcp-server-with-oauth-01496ca9e17e) |
| [`semantic-caching`](semantic-caching/) | [Semantic Caching (with Redis)](https://medium.com/@thetalkingapp/spring-ai-recipe-semantic-caching-548274a1733a) |
| [`semantic-caching-anyvs`](semantic-caching-anyvs/) | [Semantic Caching with Any Vector Store](https://medium.com/@thetalkingapp/spring-ai-recipe-semantic-caching-with-any-vector-store-8b5022be91cb) |
| [`semantic-safeguard-advisor`](semantic-safeguard-advisor/) | [Applying Semantic Guardrails](https://medium.com/@thetalkingapp/spring-ai-recipe-applying-semantic-guardrails-523ec4b89b3b) |
| [`skills`](skills/) | [Guiding Agentic Behavior with Skills](https://thetalkingapp.medium.com/spring-ai-recipe-guiding-agent-behavior-with-skills-739d1b29f3c2) |
| [`skillsjars`](skillsjars/) | [Reusing Agent Behavior with SkillsJars](https://thetalkingapp.medium.com/spring-ai-recipe-reusing-agent-behavior-with-skillsjars-61636549fa79) |
| [`stdio-mcp-server`](stdio-mcp-server/) | [Creating an STDIO MCP Server](https://thetalkingapp.medium.com/spring-ai-recipe-creating-an-stdio-mcp-server-ccacce8c5349) |
| [`streamable-http-mcp-server`](streamable-http-mcp-server/) | [Streamable HTTP MCP Server](https://thetalkingapp.medium.com/spring-ai-recipe-streamable-http-mcp-server-e9c70590b872) |
| [`structured-output-validation`](structured-output-validation/) | [Enabling Resilient Structured Output](https://medium.com/@thetalkingapp/spring-ai-recipe-enabling-resilient-structured-output-5ea7cc20f651) |
| [`summarized-memory`](summarized-memory/) | [Summarizing Chat Memory](https://medium.com/@thetalkingapp/spring-ai-recipe-summarizing-chat-memory-e61236ac05cd) |
| [`system-one-fact-checking`](system-one-fact-checking/) | [Fact-Checking with a System One Model](https://medium.com/@thetalkingapp/spring-ai-recipe-fact-checking-with-a-system-one-model-cc06cd8296e2) |
| [`systemone-local`](systemone-local/) | [Running System One Models Locally](https://medium.com/@thetalkingapp/spring-ai-recipe-running-system-one-models-locally-b64cc0ed52cd) |
| [`todo-write-tool`](todo-write-tool/) | [Agentic Planning with TodoWriteTool](https://thetalkingapp.medium.com/spring-ai-recipe-agentic-planning-with-todowritetool-5c415ef5a816) |
| [`tool-call-advisor-logging`](tool-call-advisor-logging/) | [Better LLM Request/Response Logging with ToolCallAdvisor](https://medium.com/@thetalkingapp/spring-ai-recipe-better-llm-request-response-logging-with-toolcalladvisor-de3028af3d46) |
| [`tool-choice-explanation`](tool-choice-explanation/) | [Explaining Tool Selection](https://medium.com/@thetalkingapp/spring-ai-recipe-explaining-tool-selection-8cce493b5f94) |
| [`tool-rag`](tool-rag/) | [Tool-Based RAG](https://medium.com/@thetalkingapp/spring-ai-recipe-tool-based-rag-6a4dd283a650) |
| [`tool-search-tool`](tool-search-tool/) | [Efficient Tool Use](https://medium.com/@thetalkingapp/spring-ai-recipe-efficient-tool-use-ab9f57abf45f) |
| [`typesafe-intro`](typesafe-intro/) | [Making AI Judgements with Jev](https://medium.com/@thetalkingapp/spring-ai-recipe-making-ai-judgments-with-jev-12ee580ae976) |
| [`vectors-rag`](vectors-rag/) | [RAG Without a Separate Vector Database](https://medium.com/@thetalkingapp/spring-ai-recipe-rag-without-a-separate-vector-database-7c47047ddbb1) |
| [`voicechat-stt`](voicechat-stt/) | [Talking to AI](https://medium.com/@thetalkingapp/spring-ai-recipe-talking-to-ai-837795d7bf9e) |
| [`voicechat-tts`](voicechat-tts/) | [Adding Voice to AI](https://thetalkingapp.medium.com/spring-ai-recipe-adding-voice-to-ai-802a16b3683e) |
| [`voicechat-tts-elevenlabs`](voicechat-tts-elevenlabs/) | [Using ElevenLabs Voices](https://medium.com/@thetalkingapp/spring-ai-recipe-using-elevenlabs-voices-53ffbd1e89c4) |

`graph-workflow-loop` is listed under two recipes because its `DiagramBuilder`
covers the visualization recipe as well as the loop. `jbang-chat-loop` and
`jbang-mcp` are JBang scripts rather than Gradle projects.

## Maintenance scripts

- `scripts/check-updates [folder] [--no-major]` reports, per project, which
  plugins and dependencies have newer versions.
- `scripts/update-deps [folder] [--no-major] [--dry-run] [--keep-failed]` bumps
  those versions and runs `./gradlew clean build` in each changed project. A
  project whose build fails is rolled back. Omit `folder` to update everything.
