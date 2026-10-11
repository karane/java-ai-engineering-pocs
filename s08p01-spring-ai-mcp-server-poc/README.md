# s08p01 — Spring AI MCP Server

Demonstrates exposing plain `@Tool`-annotated Spring beans as **MCP (Model Context Protocol) tools**
over HTTP, rather than wiring them directly into one app's `ChatClient` (as s04p01 did). Any MCP-aware
client - Claude Desktop, this repo's own `s08p02` client, or a generic MCP Inspector - can discover and
call these tools without knowing anything about Spring AI.

- `UnitConversionTools` — three `@Tool`-annotated methods (°C→°F, km→mi, kg→lb), identical shape to
  s04p01's `CalculatorTools`.
- `ToolCallbackProviderConfig` — wraps those methods in a `ToolCallbackProvider` via
  `MethodToolCallbackProvider.builder().toolObjects(...)`; the MCP server auto-configuration converts
  each `ToolCallback` into an `McpServerFeatures.SyncToolSpecification` registered with the underlying
  `McpSyncServer`.
- `McpServerStartupLogger` — logs the registered tool names on boot so you can see the conversion
  happened without needing an MCP client connected yet.
- `application.yml` / `application-stdio.yml` — two transport configurations for the same server (see
  "Deviation from the usual app shape" below).

## Deviation from the usual app shape

Every other POC in this repo is a `spring.main.web-application-type: none` console `CommandLineRunner`
that runs once and exits. **This one is not** — per the roadmap's own convention note ("keep following
this shape unless a POC has a specific reason to deviate, and note the reason if so"), an MCP server
is, by definition, a long-running endpoint that an external client connects to. There is nothing to
"run once and print a result": the point of this POC is that the server stays up, under
`spring-ai-starter-mcp-server-webmvc`, serving:

- `GET /sse` — the SSE transport endpoint
- `POST /mcp` — the streamable-HTTP transport endpoint

on port `8081`. This mirrors the roadmap's own precedent for `s12p01-streaming-rest-api-poc`, which the
conventions section explicitly calls out as the other recognized exception.

A second profile, `stdio` (`application-stdio.yml`), reconfigures the *same* tool beans onto the stdio
transport instead (`spring.ai.mcp.server.stdio: true`, `spring.main.web-application-type: none`) - this
is the shape a host process (Claude Desktop, or `s08p02`'s client) spawns as a subprocess and talks to
over stdin/stdout rather than HTTP. See `s08p02`'s README and `s08p03`'s README for how each consumes
this server.

## Requirements

- Java 25
- Maven
- No API key is required — this POC only serves tools, it never calls an LLM itself.

## Run the tests

```sh
mvn test
```

Green with zero keys/config needed: `SpringAiMcpServerApplicationTests` boots the full webmvc MCP
server on a random port and proves the context (MCP auto-configuration + `ToolCallbackProvider` bean)
wires up; `ToolRegistrationTests` is an ungated unit test asserting the three tools are registered and
`celsiusToFahrenheit` is directly callable.

## Run the server

HTTP/SSE transport (default):

```sh
mvn spring-boot:run
```

Starts on `http://localhost:8081`, serving `/sse` and `/mcp`.

Stdio transport (what `s08p02` and Claude Desktop expect to spawn):

```sh
mvn spring-boot:run -Dspring-boot.run.profiles=stdio
```

No port is opened; the server speaks MCP JSON-RPC over stdin/stdout.

## Gotcha: stdio logging corrupts the protocol

Spring Boot's default console appender writes to **stdout** — the exact stream the stdio MCP
transport uses as its wire format. Any ordinary log line would be interpreted as a malformed JSON-RPC
message by the client. `logback-spring.xml` handles this with a `stdio` Spring profile that redirects
the console appender to `System.err` instead, leaving stdout clean for protocol traffic. If you add your
own logging to this module later, make sure it goes through this profile-aware config rather than a
fresh `System.out.println`.

## Dependency note

No POC-local `.gitignore` is needed — the repo root's already excludes `target/` for every module.

Pinned to `spring-ai-bom` `1.1.8` for the same reason as every other POC in this repo: Google GenAI 2.0.1
needs the Boot 4 / Framework 7 line (irrelevant here, since this POC has no Google dependency, but the
BOM is imported as a whole and must stay pinned together with the rest of the repo).

If your SDKMAN default Java isn't 25:

```sh
JAVA_HOME=~/.sdkman/candidates/java/25.0.3-tem \
PATH=~/.sdkman/candidates/java/25.0.3-tem/bin:$PATH \
mvn test
```
