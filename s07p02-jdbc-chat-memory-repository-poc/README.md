# s07p02-jdbc-chat-memory-repository-poc

This POC demonstrates building a custom Spring Data JPA backed `ChatMemory` implementation.

- `JpaChatMemory`: Implements the `ChatMemory` interface, allowing conversational state to persist across restarts.
- `ChatMessageEntity`: JPA Entity mapping conversational turns.
- `ChatMessageRepository`: Spring Data JPA repository for persisting chat interactions to an H2 database.

## Requirements
- Java 25
- H2 in-memory or file-based database (used here).
- Valid Anthropic API key (`ANTHROPIC_API_KEY`) or Google Gemini API key (`GEMINI_API_KEY`).

## Setup
Export your preferred provider's key:
```bash
export ANTHROPIC_API_KEY=...
# OR
export GEMINI_API_KEY=...
```

## Running
Tests:
```bash
mvn clean test
```

App:
```bash
# Run with Claude (default profile)
mvn spring-boot:run

# Run with Gemini
mvn spring-boot:run -Dspring-boot.run.profiles=gemini
```

Note: No POC-local `.gitignore` is needed; the root `.gitignore` covers `target/`.
