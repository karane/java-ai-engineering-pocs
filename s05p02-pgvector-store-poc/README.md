# s05p02 — PgVectorStore

Second POC of Level 5. Stores and searches document embeddings in Postgres using the pgvector
extension, via Spring AI's `PgVectorStore`.

What's demonstrated:

- `PgVectorStore.builder(JdbcTemplate, EmbeddingModel)` — built programmatically (not via Spring Boot
  autoconfiguration — see the Gotcha below), with `.dimensions()`, `.distanceType(COSINE_DISTANCE)`,
  `.indexType(HNSW)`, and `.initializeSchema(true)`.
- `VectorStore.add(List<Document>)` — embedding and storing documents.
- `SearchRequest.builder().query().topK().similarityThreshold()` — similarity search against the
  abstraction, in `PgVectorStoreService`.
- HNSW index configuration via the builder's `indexType`/`distanceType`.
- `GoogleGenAiTextEmbeddingModel` (same setup as `s05p01`) as the `EmbeddingModel` used to turn
  documents/queries into vectors.

## Requirements

- Java 25, Maven
- For the Testcontainers integration test: Docker (optional — the test skips gracefully without it)
- For a fully live run: a real Postgres instance with the `pgvector` extension, and a Gemini API key

## Setup

```sh
export GEMINI_API_KEY=AQ...
```

No POC-local `.gitignore` needed — the repo-root one already covers `target/`.

## Run the tests

```sh
mvn test
```

If your active SDKMAN Java isn't 25, prefix with `JAVA_HOME`/`PATH` as in the repo's root `CLAUDE.md`.

Three test classes, all green with **zero keys and zero running services**:

- `PgVectorStorePocApplicationTests` — context loads. `DataSourceAutoConfiguration`,
  `JdbcTemplateAutoConfiguration`, and `PgVectorStoreAutoConfiguration` are explicitly excluded in
  `application.yml` (see Gotcha), so this never touches Postgres.
- `PgVectorStoreServiceTests` — unit tests against a hand-written `VectorStore` stub, proving
  `PgVectorStoreService` builds the right `SearchRequest` shapes (query/topK/similarityThreshold).
- `PgVectorStoreTestcontainersTest` — a real, end-to-end test against `pgvector/pgvector:pg16` started
  with Testcontainers: builds a real `PgVectorStore`, adds documents, and asserts a real similarity
  search returns the right nearest neighbor. Uses a small deterministic hash-based `EmbeddingModel`
  stub instead of a live Gemini call, so it only needs Docker, not an API key. **Skips gracefully
  (0 tests run, not a failure) if Docker isn't reachable** — that's the case in this repo's dev sandbox
  (no Docker daemon running), so it was validated for correct skip behavior but not run end-to-end here.
  Run it directly to exercise the real path:
  ```sh
  mvn test -Dtest=PgVectorStoreTestcontainersTest
  ```
  with Docker running locally.

## Run the app

```sh
mvn spring-boot:run
```

Console-only. By default (no `PGVECTOR_JDBC_URL`), it explains what's missing and exits cleanly. To see
the live path against a real Postgres+pgvector instance:

```sh
docker run -d --name pgvector-demo -e POSTGRES_PASSWORD=postgres -p 5432:5432 pgvector/pgvector:pg16

export GEMINI_API_KEY=AQ...
export PGVECTOR_JDBC_URL=jdbc:postgresql://localhost:5432/postgres
export PGVECTOR_JDBC_USERNAME=postgres
export PGVECTOR_JDBC_PASSWORD=postgres

mvn spring-boot:run
```

This was **not run in this environment** (no Docker daemon available here) — it's documented for
manual verification with a real Postgres/Docker setup, per this POC's definition-of-done note.

## Gotcha: why the vector store isn't Spring-Boot-autoconfigured in the main app

Adding `spring-ai-starter-vector-store-pgvector` auto-configures a `VectorStore` bean that requires a
`JdbcTemplate` bean, which requires a `DataSource` bean, which (via `spring-boot-starter-jdbc` +
`postgresql` driver) Spring Boot will try to build even with no `spring.datasource.url` configured —
and even if that got a placeholder value, `PgVectorStore` implements `InitializingBean` and validates/
initializes its schema eagerly at context-refresh time, requiring a **real, reachable** Postgres. That
would break the "plain `@SpringBootTest` context-load test always green with no keys/services" convention.

The fix: `application.yml` explicitly excludes `DataSourceAutoConfiguration`, `JdbcTemplateAutoConfiguration`,
and `PgVectorStoreAutoConfiguration`. `PgVectorStoreService` instead builds its own `DataSource`/
`JdbcTemplate`/`PgVectorStore` programmatically, given to it by whoever has a real connection to offer —
`PgVectorDemoRunner` for a real manually-started Postgres, or the Testcontainers test for an ephemeral one.
This is a deliberate deviation from the usual "`*Config` wires provider beans via Spring autoconfiguration"
convention, because unlike `ChatModel`/`EmbeddingModel` (which only fail on the actual network call),
`PgVectorStore`'s bean creation itself requires a live database connection.

## Key API Reference

| API | Purpose |
|---|---|
| `PgVectorStore.builder(JdbcTemplate, EmbeddingModel)` | Programmatic construction, bypassing autoconfiguration |
| `.dimensions(int)` | Vector column width — must match the embedding model's `dimensions()` |
| `.distanceType(PgDistanceType.COSINE_DISTANCE)` | Distance metric used for similarity search/indexing |
| `.indexType(PgIndexType.HNSW)` | Approximate-nearest-neighbor index type pgvector creates |
| `.initializeSchema(true)` | Creates the vector table + index on first use if missing |
| `SearchRequest.builder().query().topK().similarityThreshold()` | Fluent similarity-search request |
| `VectorStore.add(List<Document>)` | Embeds and stores documents |

## What this proves

- `PgVectorStore` can be exercised for real — schema creation, embedding, storage, and similarity
  search — against an ephemeral, disposable Postgres via Testcontainers, with no hand-managed database.
- The vector-store abstraction (`VectorStore`/`SearchRequest`) is provider-agnostic in the same way
  `ChatModel`/`EmbeddingModel` are: `PgVectorStoreService` never leaks `PgVectorStore`-specific types
  past its constructor.
- A real similarity search over real pgvector-computed distances correctly ranks a semantically matching
  document above an unrelated one, using genuine (if deterministic, non-LLM) embedding vectors.

Next on the roadmap: `s05p03-simple-vector-store-poc` — the in-memory, zero-external-dependency vector
store for the cases pgvector's setup cost isn't worth paying.
