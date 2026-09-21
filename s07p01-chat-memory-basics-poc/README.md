# s07p01-chat-memory-basics-poc

Demonstrates Spring AI chat memory using `ChatMemory`, `MessageWindowChatMemory`, and `MessageChatMemoryAdvisor`.

## Features
* `InMemoryChatMemory` for session storage
* `MessageWindowChatMemory` wrapper restricting the memory history size
* `MessageChatMemoryAdvisor` injected into `ChatClient` with a parameterized `conversationId` per request

## Setup
```bash
export ANTHROPIC_API_KEY=your_key
export GEMINI_API_KEY=your_key
```

No `.gitignore` is needed as the repository root `.gitignore` already ignores `target/` directories.

## Running Tests
Run tests with no keys to verify context loading:
```bash
mvn clean test
```

## Running the Application
```bash
mvn spring-boot:run
```

With Gemini profile:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=gemini
```

## Java Version Mismatch Gotchas
If you get a `UnsupportedClassVersionError`, set `JAVA_HOME` to a Java 25 JDK:
```bash
export JAVA_HOME=/path/to/java-25
```
