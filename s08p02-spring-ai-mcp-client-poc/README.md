# s08p02 — Spring AI MCP Client

Demonstrates the **client** side of MCP: connecting to an external MCP server (the `s08p01` server
from this same repo) over stdio, discovering its tools via the auto-configured
`McpToolCallbackProvider`, and handing those tools to a Claude `ChatClient` exactly as if they were
local `@Tool` beans.

- `ChatClientConfig` — the usual `ChatClient.create(AnthropicChatModel)` bean.
- `McpToolCallingService` — autowires the auto-configured `ToolCallbackProvider` (Spring AI's
  `McpToolCallbackProvider`, built from every connected `McpSyncClient`) and registers its tools via
  `.toolCallbacks(...)` on a `ChatClient.prompt()` call.
- `McpClientDemoRunner` — prints the tool names discovered from the server, then (with a real key)
  asks Claude a question only answerable by actually calling those remote tools.
- `NoOpMcpToolCallbackProviderConfig` — a test-only fallback bean (see "How tests avoid spawning a
  subprocess" below).

## How this connects to s08p01

This client is configured to **spawn `s08p01`'s server jar as a stdio subprocess** - the natural way
to demonstrate an MCP client/server pair end to end without a separate network service. `application.yml`
declares the connection declaratively:

```yaml
spring:
  ai:
    mcp:
      client:
        stdio:
          connections:
            unit-conversion-server:
              command: java
              args: [-jar, ../s08p01-spring-ai-mcp-server-poc/target/s08p01-spring-ai-mcp-server-poc-0.1.0.jar, --spring.profiles.active=stdio]
```

**Before running this app or its live test**, build the s08p01 jar:

```sh
(cd ../s08p01-spring-ai-mcp-server-poc && mvn -DskipTests package)
```

Then from this module:

```sh
export ANTHROPIC_API_KEY=sk-ant-...
mvn spring-boot:run
```

On startup, Spring Boot's `McpSyncClient` auto-configuration launches `java -jar
...s08p01.../target/...jar --spring.profiles.active=stdio` as a child process, performs the MCP
initialize handshake over its stdin/stdout, and lists its tools - `celsiusToFahrenheit`,
`kilometersToMiles`, `kilogramsToPounds` - into the `ToolCallbackProvider` bean this app consumes.

## How tests avoid spawning a subprocess

`src/test/resources/application.yml` sets `spring.ai.mcp.client.enabled=false`, so `mvn test`'s
`contextLoads()` test never launches the s08p01 subprocess (keeping `mvn test` fast and independent of
whether that jar has been built yet). `NoOpMcpToolCallbackProviderConfig` supplies an empty
`ToolCallbackProvider` bean in that case so `McpToolCallingService` still wires up.

`McpClientLiveTest` is the one test that actually re-enables the client (via `@TestPropertySource`)
and spawns the real subprocess - it's gated on `ANTHROPIC_API_KEY` *and* requires the s08p01 jar to
already be built at the relative path above.

## Requirements

- Java 25
- Maven
- The `s08p01` module's jar built (`mvn -DskipTests package` there) before running this app or its live test
- An Anthropic API key (not required for `mvn test`'s default run)

## Run the tests

```sh
mvn test
```

Green with zero keys: `contextLoads()` (MCP client disabled, no subprocess spawned) and
`McpToolCallingServiceUnitTests` (ungated, mocked `ToolCallbackProvider`/`ChatClient`).
`McpClientLiveTest` reports skipped without `ANTHROPIC_API_KEY`.

## Run the app

```sh
mvn spring-boot:run
```

Prints the tools discovered from the s08p01 subprocess, then (with a real `ANTHROPIC_API_KEY`) Claude's
answer after calling those MCP tools.

## Dependency note

No POC-local `.gitignore` needed — the repo root's covers `target/` for every module.

If your SDKMAN default Java isn't 25:

```sh
JAVA_HOME=~/.sdkman/candidates/java/25.0.3-tem \
PATH=~/.sdkman/candidates/java/25.0.3-tem/bin:$PATH \
mvn test
```
