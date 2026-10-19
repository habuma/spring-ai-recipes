package com.example.agui;

import com.agui.community.core.agent.Agent;
import com.agui.community.core.agent.RunAgentInput;
import com.agui.community.core.event.ActivitySnapshotEvent;
import com.agui.community.core.event.Event;
import com.agui.community.core.event.RunErrorEvent;
import com.agui.community.core.event.RunFinishedEvent;
import com.agui.community.core.event.RunStartedEvent;
import com.agui.community.core.event.ToolCallArgsEvent;
import com.agui.community.core.event.ToolCallResultEvent;
import com.agui.community.core.event.ToolCallStartEvent;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Flow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.adapter.JdkFlowAdapter;
import reactor.core.publisher.Flux;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * Wraps another {@link Agent} to support MCP Apps in the CopilotKit frontend.
 *
 * <ul>
 *   <li>When a tool declaring a UI resource ({@code _meta.ui.resourceUri}) is called,
 *       an {@code mcp-apps} activity snapshot is emitted after the tool result so that
 *       the frontend renders the app.</li>
 *   <li>Runs carrying {@code forwardedProps.__proxiedMCPRequest} (sent by the rendered
 *       app: {@code resources/read} and {@code tools/call}) are answered directly from
 *       the MCP server without involving the model.</li>
 * </ul>
 */
public class McpAppsAgent implements Agent {

  private static final Logger log = LoggerFactory.getLogger(McpAppsAgent.class);

  static final String ACTIVITY_TYPE = "mcp-apps";
  private static final String PROXIED_REQUEST = "__proxiedMCPRequest";

  private final Agent delegate;
  private final List<McpSyncClient> clients;
  private final JsonMapper json = JsonMapper.builder().build();

  public McpAppsAgent(Agent delegate, List<McpSyncClient> clients) {
    this.delegate = delegate;
    this.clients = clients;
  }

  @Override
  public Flow.Publisher<Event> run(RunAgentInput input) {
    Map<String, Object> proxied = proxiedRequest(input);
    Flux<Event> events = proxied != null
        ? proxy(input, proxied)
        : withAppActivities(JdkFlowAdapter.flowPublisherToFlux(delegate.run(input)));
    return JdkFlowAdapter.publisherToFlowPublisher(events);
  }

  // ---- model-driven runs: attach MCP App activities to UI-enabled tool calls ----

  private Flux<Event> withAppActivities(Flux<Event> events) {
    return Flux.defer(() -> {
      Map<String, String> toolNames = new HashMap<>();
      Map<String, StringBuilder> toolArgs = new HashMap<>();
      Set<String> renderedApps = new HashSet<>();
      return events.concatMap(event -> {
        switch (event) {
          case ToolCallStartEvent start -> toolNames.put(start.toolCallId(), start.toolCallName());
          case ToolCallArgsEvent args -> toolArgs
              .computeIfAbsent(args.toolCallId(), k -> new StringBuilder()).append(args.delta());
          case ToolCallResultEvent result -> {
            Event activity = activityFor(result, toolNames.get(result.toolCallId()),
                toolArgs.get(result.toolCallId()));
            if (activity != null) {
              String resourceUri = (String) ((Map<?, ?>) ((ActivitySnapshotEvent) activity).content())
                  .get("resourceUri");
              // The model may call the tool repeatedly; render each app once per run.
              if (renderedApps.add(resourceUri)) {
                log.info("Emitting MCP App {} for tool {}", resourceUri, toolNames.get(result.toolCallId()));
                return Flux.just(event, activity);
              }
              log.info("Skipping duplicate MCP App {}", resourceUri);
            }
          }
          default -> { }
        }
        return Flux.just(event);
      });
    });
  }

  private Event activityFor(ToolCallResultEvent result, String toolName, StringBuilder args) {
    if (toolName == null) {
      return null;
    }
    for (McpSyncClient client : clients) {
      Optional<McpSchema.Tool> tool = client.listTools().tools().stream()
          .filter(t -> t.name().equals(toolName)).findFirst();
      Optional<String> resourceUri = tool.flatMap(t -> uiResourceUri(t.meta()));
      if (resourceUri.isPresent()) {
        String serverId = serverId(client);
        Map<String, Object> content = new HashMap<>();
        content.put("result", toCallToolResult(result.content()));
        content.put("resourceUri", resourceUri.get());
        content.put("serverHash", hash(serverId));
        content.put("serverId", serverId);
        content.put("toolInput", parseObject(args == null ? null : args.toString()));
        return new ActivitySnapshotEvent(UUID.randomUUID().toString(), ACTIVITY_TYPE, content);
      }
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  private static Optional<String> uiResourceUri(Map<String, Object> meta) {
    if (meta == null) {
      return Optional.empty();
    }
    if (meta.get("ui") instanceof Map<?, ?> ui && ui.get("resourceUri") instanceof String uri) {
      return Optional.of(uri);
    }
    // legacy flat key
    return meta.get("ui/resourceUri") instanceof String uri ? Optional.of(uri) : Optional.empty();
  }

  /** Rebuilds a CallToolResult from the string Spring AI hands back for a tool call. */
  @SuppressWarnings("unchecked")
  private Map<String, Object> toCallToolResult(String toolOutput) {
    Map<String, Object> result = new HashMap<>();
    try {
      Object parsed = json.readValue(toolOutput, Object.class);
      if (parsed instanceof List<?> list && list.stream().allMatch(i -> i instanceof Map<?, ?>)) {
        // Spring AI drops the "type" discriminator from text items; restore it.
        result.put("content", list.stream().map(i -> {
          Map<String, Object> item = new HashMap<>((Map<String, Object>) i);
          if (!item.containsKey("type") && item.get("text") instanceof String) {
            item.put("type", "text");
          }
          return item;
        }).toList());
        return result;
      }
      if (parsed instanceof Map<?, ?> map) {
        result.put("structuredContent", map);
      }
    } catch (RuntimeException e) {
      // not JSON: treat as plain text
    }
    result.put("content", List.of(Map.of("type", "text", "text", String.valueOf(toolOutput))));
    return result;
  }

  private Map<String, Object> parseObject(String jsonText) {
    if (jsonText == null || jsonText.isBlank()) {
      return Map.of();
    }
    try {
      return json.readValue(jsonText, new TypeReference<Map<String, Object>>() { });
    } catch (RuntimeException e) {
      return Map.of();
    }
  }

  // ---- proxied runs: the rendered app talking to its MCP server ----

  @SuppressWarnings("unchecked")
  private static Map<String, Object> proxiedRequest(RunAgentInput input) {
    return input.forwardedProps() instanceof Map<?, ?> props
        && props.get(PROXIED_REQUEST) instanceof Map<?, ?> request
            ? (Map<String, Object>) request : null;
  }

  @SuppressWarnings("unchecked")
  private Flux<Event> proxy(RunAgentInput input, Map<String, Object> request) {
    return Flux.<Event>defer(() -> {
      try {
        McpSyncClient client = clientFor(request);
        String method = String.valueOf(request.get("method"));
        Map<String, Object> params = request.get("params") instanceof Map<?, ?> p
            ? (Map<String, Object>) p : Map.of();
        Object result = switch (method) {
          case "resources/read" -> client.readResource(
              new McpSchema.ReadResourceRequest(String.valueOf(params.get("uri"))));
          case "tools/call" -> client.callTool(new McpSchema.CallToolRequest(
              String.valueOf(params.get("name")),
              params.get("arguments") instanceof Map<?, ?> a ? (Map<String, Object>) a : Map.of()));
          default -> throw new IllegalArgumentException("Unsupported MCP request: " + method);
        };
        Object plain = json.convertValue(result, Object.class);
        return Flux.<Event>just(
            new RunStartedEvent(input.threadId(), input.runId()),
            new RunFinishedEvent(input.threadId(), input.runId(), null, plain, null, null));
      } catch (RuntimeException e) {
        return Flux.<Event>just(
            new RunStartedEvent(input.threadId(), input.runId()),
            new RunErrorEvent(String.valueOf(e.getMessage())));
      }
    });
  }

  private McpSyncClient clientFor(Map<String, Object> request) {
    String serverHash = String.valueOf(request.get("serverHash"));
    return clients.stream()
        .filter(c -> hash(serverId(c)).equals(serverHash))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Unknown MCP server: " + serverHash));
  }

  private static String serverId(McpSyncClient client) {
    return client.getServerInfo().name();
  }

  private static String hash(String value) {
    try {
      return HexFormat.of().formatHex(
          MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
    } catch (java.security.NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }
}
