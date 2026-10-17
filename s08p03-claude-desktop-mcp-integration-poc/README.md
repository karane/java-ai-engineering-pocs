# s08p03 — Claude Desktop MCP Integration

Two things live in this module:

1. **A stdio MCP server** (reusing `s08p01`'s `UnitConversionTools` shape) meant to be registered in
   Claude Desktop's `claude_desktop_config.json` and tested manually through the Desktop app's UI.
2. **An automatable latency-comparison harness** (`-Dspring-boot.run.profiles=latency`) that spawns
   this same jar as an MCP subprocess and measures calling a tool directly in-process vs. over the
   MCP protocol - the one part of "MCP tool-call latency vs. direct `@Tool` calls" that doesn't need
   a human, an LLM, or an API key.

- `UnitConversionTools` — three `@Tool` methods, duplicated from s08p01 so this jar is self-contained
  (the exact artifact Claude Desktop is pointed at).
- `ToolCallbackProviderConfig` — registers those tools with the MCP server, active outside the
  `latency` profile.
- `LatencyComparisonService` / `LatencyDemoRunner` — the harness, active only under the `latency`
  profile.
- `claude_desktop_config.sample.json` — the config block to merge into Claude Desktop's own config
  file.

## Part 1: manual Claude Desktop integration (not automatable)

**This part genuinely cannot be covered by `mvn test` or a `CommandLineRunner`.** There is no
programmatic way to ask "did Claude Desktop's UI discover and successfully call this tool" - it
requires the actual Desktop app, a human clicking through its UI, and reading the app's own tool-call
transcript. Steps to verify this manually:

1. Build the server jar:
   ```sh
   mvn -DskipTests package
   ```
2. Open Claude Desktop's config file:
   - macOS: `~/Library/Application Support/Claude/claude_desktop_config.json`
   - Windows: `%APPDATA%\Claude\claude_desktop_config.json`
   - Linux (community builds): `~/.config/Claude/claude_desktop_config.json`
3. Merge in the block from `claude_desktop_config.sample.json`, replacing the `args` path with this
   module's **absolute** jar path on your machine:
   ```json
   {
     "mcpServers": {
       "s08p03-unit-conversion": {
         "command": "java",
         "args": ["-jar", "/absolute/path/to/.../target/s08p03-claude-desktop-mcp-integration-poc-0.1.0.jar"]
       }
     }
   }
   ```
4. Fully quit and restart Claude Desktop (a reload isn't enough - it only reads this file on launch).
5. Open a new chat and look for the 🔌/tools icon to confirm `s08p03-unit-conversion` is listed, with
   its three tools (`celsiusToFahrenheit`, `kilometersToMiles`, `kilogramsToPounds`) visible.
6. Ask something only answerable via the tool, e.g. *"Using your tools, what is 100°C in Fahrenheit?"*,
   and confirm Claude Desktop shows a tool-call step before answering `212`.

If step 5 doesn't show the server: check Claude Desktop's own MCP log (Settings → Developer → view
logs on macOS/Windows) for a stderr line from this process - the `logback-spring.xml` in this module
deliberately sends all logging to stderr so it never corrupts the stdio JSON-RPC stream (see s08p01's
BLOG.md gotcha), so any startup error will show up there, not silently vanish.

## Part 2: the automatable latency comparison

```sh
mvn -DskipTests package
mvn spring-boot:run -Dspring-boot.run.profiles=latency
```

Under the `latency` profile this same jar does **not** act as the server - it disables its own MCP
server (`spring.ai.mcp.server.enabled=false`) and instead spawns a second JVM running the *default*
profile (the real stdio server) as a subprocess, using the exact same `spring.ai.mcp.client.stdio`
mechanism s08p02 uses to talk to s08p01. It then calls `celsiusToFahrenheit` 50 times directly
in-process and 50 times over that MCP connection, and prints the average per-call latency for each:

```
[s08p03] Latency comparison over 50 calls to celsiusToFahrenheit:
[s08p03]   direct @Tool call  : 0 us/call avg
[s08p03]   MCP tool call      : 3611 us/call avg
[s08p03]   MCP overhead factor: 27783.1x
```

(Numbers from a real local run in this environment - no API key involved, since this harness never
calls an LLM; it isolates the MCP transport/serialization overhead itself.) The overhead is entirely
expected and is the honest finding here: a direct method call is a few nanoseconds; an MCP call pays
for JSON-RPC serialization plus a pipe round-trip to a separate OS process, several orders of
magnitude more. That's the real tradeoff MCP asks you to accept in exchange for cross-process,
cross-language tool reuse.

## Requirements

- Java 25
- Maven
- No API key required for anything in this module — neither the server, nor the latency harness,
  calls an LLM. (The Claude Desktop manual test obviously does, inside the Desktop app itself, but
  that's outside this module's build.)

## Run the tests

```sh
mvn test
```

Green with zero keys: `contextLoads()` boots the default (stdio server) profile; `UnitConversionToolsTests`
is an ungated unit test on the tool methods and their derived schema; `LatencyComparisonServiceUnitTests`
exercises the comparison harness's own averaging/overhead-factor logic against a mocked MCP
`ToolCallback`, without spawning a real subprocess (kept fast and deterministic for CI).

## Run it

```sh
mvn -DskipTests package
```

- As the Claude Desktop server: register the jar per Part 1 above.
- As the latency harness: `mvn spring-boot:run -Dspring-boot.run.profiles=latency` (see Part 2).

## Dependency note

No POC-local `.gitignore` needed — the repo root's covers `target/` for every module.

If your SDKMAN default Java isn't 25:

```sh
JAVA_HOME=~/.sdkman/candidates/java/25.0.3-tem \
PATH=~/.sdkman/candidates/java/25.0.3-tem/bin:$PATH \
mvn test
```
