# s05p01 — Embedding Model

First POC of Level 5 (Embeddings & Vector Stores). Anthropic has no native embedding model, so this POC
pairs the roadmap's Claude-focused earlier levels with Google's `GoogleGenAiTextEmbeddingModel` — the
Spring AI 1.1.8 successor to `VertexAiTextEmbeddingModel` for the Gemini Developer API path (no GCP
project/ADC needed, same as `s01p02`).

What's demonstrated:

- `EmbeddingModel` — the provider-agnostic interface `EmbeddingService` programs against.
- `GoogleGenAiTextEmbeddingModel` — the concrete bean, auto-configured once `spring-ai-google-genai-embedding`
  is added alongside `spring-ai-starter-model-google-genai` (the starter only brings the chat model).
- `.embed(String)` / `.embed(List<String>)` and `.embedForResponse(List<String>)` — single and batch
  embedding, the latter returning full `EmbeddingResponse` metadata.
- `EmbeddingResponse` / `Embedding` / `EmbeddingResponseMetadata` — inspecting the model name, per-item
  vectors, and token usage on the response.
- A small cosine-similarity helper to make the "semantically similar sentences embed closer together"
  point concretely, without needing a vector store yet (that's `s05p03`).

**Note on Spring AI version:** pinned to **1.1.8**, not 2.0.1, for the same reason as `s01p02` (see that
POC's README/pom comment on the Boot 3.5.9 vs. Spring Framework 7 incompatibility).

## Requirements

- Java 25
- Maven
- A Gemini API key (from [Google AI Studio](https://aistudio.google.com/apikey))

## Setup

```sh
export GEMINI_API_KEY=AQ...
```

No POC-local `.gitignore` needed — the repo-root one already covers `target/`.

## Run the tests

```sh
mvn test
```

If your active SDKMAN Java isn't 25, prefix the command:

```sh
JAVA_HOME=~/.sdkman/candidates/java/25.0.3-tem \
PATH=~/.sdkman/candidates/java/25.0.3-tem/bin:$PATH \
mvn test
```

- No `GEMINI_API_KEY`: context-load test + unit tests for the cosine-similarity helper run (4 tests),
  the 2 live tests skip.
- With `GEMINI_API_KEY` set: all 6 run, including live calls to `gemini-embedding-001`.

## Run the app

```sh
mvn spring-boot:run
```

Console-only app — with `GEMINI_API_KEY` set it embeds a few sentences, prints the vector dimensions,
batch usage metadata, and cosine similarities; without it, it explains that the key isn't set and exits
cleanly.

## Gotcha: model naming and a second, separate connection-properties namespace

Two non-obvious things came up wiring this:

1. **`text-embedding-004` returns a 404** against the Gemini Developer API's `v1beta` endpoint used by
   `com.google.genai.Client` in Gemini-Developer-API mode (`vertex-ai: false`) — that model id is served
   from the Vertex AI endpoint instead. `gemini-embedding-001` (3072 dimensions) is what actually works
   here; see `GoogleGenAiTextEmbeddingModelName` for the enum of known model ids/dimensions if you want to
   try `text-multilingual-embedding-002` or the Vertex-only path instead.
2. **The embedding connection has its own properties, separate from chat's.** `spring.ai.google.genai.embedding.*`
   (backed by `GoogleGenAiEmbeddingConnectionProperties`) does **not** inherit `spring.ai.google.genai.api-key`
   or `.vertex-ai` from the chat side — `GoogleGenAiEmbeddingConnectionAutoConfiguration` validates its own
   `api-key`/`project-id` eagerly at bean-creation time and fails startup with `Google GenAI project-id
   must be set!` if only the chat-side properties are set. `application.yml` sets both namespaces
   explicitly with the same placeholder-friendly defaults.
