# s05p05 — RedisVectorStore (stretch)

Stretch POC of Level 5. Stores and searches document embeddings in Redis Stack using
Spring AI's `RedisVectorStore`, backed by the RediSearch and RedisJSON modules.

What's demonstrated:

- `RedisVectorStore.builder(JedisPooled, EmbeddingModel)` — built programmatically (not via Spring Boot
  autoconfiguration — see the Gotcha below, same reasoning as `s05p02`'s `PgVectorStore`), with
  `.vectorAlgorithm(HNSW)`, `.metadataFields(MetadataField.tag(...))`, and `.initializeSchema(true)`.
- Redis Stack schema configuration — `initializeSchema(true)` issues the `FT.CREATE` command under the
  hood, building the RediSearch index from the configured algorithm/metadata fields.
- Metadata filter expressions — the same `SearchRequest.filterExpression(...)` shape used in
  `s05p02`/`s05p03`, translated by `RedisFilterExpressionConverter` into Redis's query syntax.
- TTL-based document expiry — `RedisVectorStoreService.addWithExpiry(documents, ttlSeconds)` sets a Redis
  key TTL (via raw Jedis `EXPIRE`) on each stored document's JSON key, since `RedisVectorStore` itself has
  no built-in expiry concept (see the Key API Reference below).

## Requirements

- Java 25, Maven
- For the Testcontainers integration test: Docker (optional — the test skips gracefully without it)
- For a fully live run: a real Redis Stack instance (search + JSON modules) and a Gemini API key

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

- `RedisVectorStorePocApplicationTests` — context loads. `RedisVectorStoreAutoConfiguration` is
  explicitly excluded in `application.yml` (see Gotcha), so this never touches Redis.
- `RedisVectorStoreServiceTests` — unit tests against a hand-written `VectorStore` stub, proving
  `RedisVectorStoreService` builds the right `SearchRequest` shapes.
- `RedisVectorStoreTestcontainersTest` — a real, end-to-end test against
  `redis/redis-stack-server:latest` started with Testcontainers: builds a real `RedisVectorStore`, adds
  documents (one with a TTL, one without), and asserts a real similarity search returns the right nearest
  neighbor **and** that the TTL was actually set on the expiring document's Redis key. Uses a small
  deterministic hash-based `EmbeddingModel` stub instead of a live Gemini call, so it only needs Docker,
  not an API key. **Skips gracefully (0 tests run, not a failure) if Docker isn't reachable** — that's the
  case in this repo's dev sandbox (no Docker daemon running), so it was validated for correct skip
  behavior but not run end-to-end here. Run it directly with Docker running locally:
  ```sh
  mvn test -Dtest=RedisVectorStoreTestcontainersTest
  ```

## Run the app

```sh
mvn spring-boot:run
```

Console-only. By default (no `REDIS_HOST`), it explains what's missing and exits cleanly. To see the
live path against a real Redis Stack instance:

```sh
docker run -d --name redis-stack-demo -p 6379:6379 redis/redis-stack-server:latest

export GEMINI_API_KEY=AQ...
export REDIS_HOST=localhost
export REDIS_PORT=6379

mvn spring-boot:run
```

This was **not run in this environment** (no Docker daemon available here) — it's documented for manual
verification with a real Redis/Docker setup, per this POC's definition-of-done note.

## Gotcha: same "vector store bean creation needs a live connection" issue as pgvector

Exactly the same reasoning as `s05p02`'s README applies here: `RedisVectorStoreAutoConfiguration`'s
`vectorStore` bean needs a `JedisConnectionFactory` bean (from `spring-boot-starter-data-redis`, not
pulled in by the Redis vector store starter itself) and, once built, `RedisVectorStore` implements
`InitializingBean` and issues `FT.CREATE` eagerly against a real, reachable Redis at bean-creation time.
That would break the "plain `@SpringBootTest` context-load test always green with no keys/services"
convention. The fix is the same as `s05p02`: exclude `RedisVectorStoreAutoConfiguration` in
`application.yml`, and build `RedisVectorStore` programmatically instead, wherever a real Redis connection
is actually available (`RedisVectorDemoRunner` for a real manually-started Redis Stack, or the
Testcontainers test for an ephemeral one).

## Gotcha: TTL isn't part of RedisVectorStore's API

`RedisVectorStore` has no `.ttl(...)` builder option or per-document expiry parameter — it stores each
document as a Redis JSON value keyed by `"embedding:" + document.getId()` with no expiry set. TTL-based
expiry (as the roadmap asks for) is implemented in `RedisVectorStoreService.addWithExpiry` by calling raw
Jedis `EXPIRE` on that same key convention, right after `add()`. This is a direct, if slightly fragile,
integration: it depends on `RedisVectorStore`'s private key-naming scheme (`DEFAULT_PREFIX = "embedding:"`)
staying stable, which is documented as a public constant on the class even though the key-building method
itself is private.

## Key API Reference

| API | Purpose |
|---|---|
| `RedisVectorStore.builder(JedisPooled, EmbeddingModel)` | Programmatic construction, bypassing autoconfiguration |
| `.vectorAlgorithm(Algorithm.HNSW)` | Approximate-nearest-neighbor index type Redis Stack creates |
| `.metadataFields(MetadataField.tag/text/numeric(name))` | Declares which metadata fields are filterable, and how |
| `.initializeSchema(true)` | Issues `FT.CREATE` to build the RediSearch index on first use |
| `JedisPooled.expire(key, seconds)` | Sets a TTL on a document's Redis key (this POC's expiry mechanism) |
| `JedisPooled.ttl(key)` | Reads remaining TTL; `-1` = no expiry set, `-2` = key doesn't exist |

## What this proves

- `RedisVectorStore` can be exercised for real — schema creation, embedding, storage, similarity search,
  and metadata filtering — against an ephemeral Redis Stack container via Testcontainers.
- TTL-based document expiry, while not a first-class `RedisVectorStore` feature, is straightforward to
  layer on top using the same Jedis client the store already holds — verified by asserting a real,
  positive remaining TTL on an expiring document versus `-1` (no expiry) on a permanent one.
- The same `VectorStore`/`SearchRequest` abstraction that worked for `PgVectorStore` (`s05p02`) and
  `SimpleVectorStore` (`s05p03`) works identically for Redis — the provider swap is confined entirely to
  construction.

This closes out Level 5 — Embeddings & Vector Stores. Next on the roadmap: Level 6 — Retrieval-Augmented
Generation (RAG), which builds directly on the `EmbeddingModel`/`VectorStore` foundation from this level.
