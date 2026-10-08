# s07p04-multi-tenant-chat-memory-poc

This proof-of-concept demonstrates a Redis-backed `ChatMemory` implementation with per-tenant conversation isolation
and TTL-based session eviction, using Spring AI's `ChatClient` abstraction.

## Requirements
- Custom Redis-backed `ChatMemory` implementation (`RedisChatMemory`)
- Per-tenant conversation isolation: each tenant gets its own `ChatMemory` bean and Redis key namespace
  (`chat_memory:<tenantId>:<conversationId>`), so the same `conversationId` never leaks across tenants
- TTL-based session eviction: each tenant's memory has an independent TTL (Tenant A: 10 minutes, Tenant B: 30 minutes)
- Multi-provider support via Spring profiles (Claude / Gemini)

## How it works
- `RedisChatMemory` stores each conversation turn as a JSON-serialized message in a Redis list, keyed by
  `chat_memory:<tenantId>:<conversationId>`, and refreshes the key's TTL on every write.
- `MultiTenantChatMemoryConfig` wires up one `ChatMemory` bean per tenant (different TTLs) and one `ChatClient`
  per tenant, each with its own `MessageChatMemoryAdvisor` bound to that tenant's memory.
- Because isolation happens at the bean/key level (not just the `conversationId`), two tenants can safely reuse
  the exact same `conversationId` without ever seeing each other's history.
- This design also supports horizontal scaling: since all state lives in Redis rather than in-process, any
  stateless service instance can serve any tenant's request.

## Run

Requires a running Redis instance (`localhost:6379` by default, configurable via `REDIS_HOST`/`REDIS_PORT`).

```sh
docker run -d --name redis -p 6379:6379 redis:7.2-alpine
```

Use `SPRING_PROFILES_ACTIVE=claude` or `SPRING_PROFILES_ACTIVE=gemini` to run with different providers:

```sh
ANTHROPIC_API_KEY=sk-... SPRING_PROFILES_ACTIVE=claude mvn spring-boot:run
```

```sh
GEMINI_API_KEY=... SPRING_PROFILES_ACTIVE=gemini mvn spring-boot:run
```

With no API key set, the `DemoRunner` skips the live demo and exits cleanly.

## Tests
- `MultiTenantChatMemoryApplicationTests` — context loads against a Testcontainers-managed Redis instance
- `AnthropicLiveTest` / `GeminiLiveTest` — live isolation tests gated behind `ANTHROPIC_API_KEY` / `GEMINI_API_KEY`
  env vars, verifying that Tenant A's and Tenant B's memories never cross-contaminate even when using the same
  `conversationId`

Tests require Docker (Testcontainers) to be running.
