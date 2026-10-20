# s08p04 — Multi-Server MCP Aggregation (Stretch)

One jar, three roles, switched by Spring profile:

- **`server-a`** — a stdio MCP server exposing `UnitConversionTools` (`celsiusToFahrenheit`, `ping`).
- **`server-b`** — a stdio MCP server exposing `TextTools` (`shout`, `wordCount`, `ping`).
- **default (aggregator)** — spawns *both* of the above as subprocesses, combines their discovered
  tools into one `ChatClient`, and inspects the combined list for name collisions.

Both toy servers deliberately define a tool literally named `ping` with different behavior, so the
aggregator has to deal with a real namespace collision rather than a hypothetical one.

- `UnitConversionTools` / `TextTools` — the two servers' `@Tool` methods.
- `ServerToolCallbackProviderConfig` — registers each server's own tools, scoped to its own profile.
- `AggregatedToolInspectionService` — autowires the aggregator profile's combined
  `ToolCallbackProvider`, groups tools by name, and reports which names collided.
- `AggregatingDemoRunner` — prints the aggregated tool list and (with a real key) asks Claude to use
  one tool from each server in a single answer.

## What actually happens with the `ping` collision

The honest finding from running this for real: **Spring AI's `McpToolCallbackProvider` resolves the
collision itself**, before any application code sees the list. Running the aggregator (`mvn
spring-boot:run`, no API key needed to see this part) logs:

```
WARN  o.s.a.m.DefaultMcpToolNamePrefixGenerator - Tool name 'ping' already exists. Using unique tool name 'alt_1_ping'
[s08p04] Aggregated tool names: [alt_1_ping, wordCount, celsiusToFahrenheit, ping, shout]
[s08p04] No tool-name collisions across the two servers.
```

`server-a`'s `ping` keeps its original name; `server-b`'s `ping` is silently renamed to `alt_1_ping`
by `DefaultMcpToolNamePrefixGenerator` as the two connections' tool lists are merged - by the time
`AggregatedToolInspectionService.duplicateToolNames()` runs, there's genuinely nothing left to
report, because the framework already de-duplicated the names. This is "namespace collision
handling" working as intended, just resolved a layer lower than application code - worth knowing
before writing your own collision-detection logic on top, since you'd be solving an already-solved
problem.

`AggregationLiveTest` (gated on `ANTHROPIC_API_KEY`) asserts this renamed-tool behavior directly
against the real spawned subprocesses, rather than against a guess.

## Dynamic registration

Each stdio connection in `spring.ai.mcp.client.stdio.connections` is just a map entry - adding a third
server is adding a third YAML block, no code changes. The aggregator's `ToolCallbackProvider` is
re-resolved from whatever connections exist at startup, so "dynamic registration" here means
configuration-driven registration: no server list is hardcoded in Java.

## Requirements

- Java 25
- Maven
- No API key required to see the aggregation/collision-handling behavior (it's all protocol-level);
  a real `ANTHROPIC_API_KEY` is only needed for the final "Claude actually uses both servers' tools"
  step.

## Run the tests

```sh
mvn test
```

Green with zero keys: `contextLoads()` (MCP client disabled via test `application.yml`, no
subprocesses spawned), `ToolRegistrationTests` (ungated, confirms both servers really do define a
`ping` tool with different bodies), `AggregatedToolInspectionServiceUnitTests` (ungated, mocked
provider proving the grouping/duplicate-detection logic itself). `AggregationLiveTest` reports
skipped without `ANTHROPIC_API_KEY` and needs this module's own jar built first.

## Run it

```sh
mvn -DskipTests package
mvn spring-boot:run
```

Spawns both `server-a` and `server-b` as subprocesses of the freshly-built jar, prints the aggregated
tool list (watch for `alt_1_ping`), and - with `ANTHROPIC_API_KEY` exported - asks Claude a question
answerable only by calling a tool from each server.

## Dependency note

No POC-local `.gitignore` needed — the repo root's covers `target/` for every module.

If your SDKMAN default Java isn't 25:

```sh
JAVA_HOME=~/.sdkman/candidates/java/25.0.3-tem \
PATH=~/.sdkman/candidates/java/25.0.3-tem/bin:$PATH \
mvn test
```
