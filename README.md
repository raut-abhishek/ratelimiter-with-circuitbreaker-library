# Rate Limiter & Circuit Breaker Library

A Java library implementing production-grade rate limiting and fault tolerance patterns from scratch, with native Spring Boot integration via custom annotations and AOP — built to understand the mechanics that libraries like Bucket4j and Resilience4j abstract away, rather than just consuming them.

## Features

- **Two rate-limiting algorithms** — Token Bucket (burst-tolerant) and Sliding Window (strict, smooth limiting), both implementing a common `RateLimiter` interface so they're interchangeable.
- **Adaptive rate limiting** — `AdaptiveTokenBucket` dynamically scales capacity and refill rate based on real-time CPU load (via `OperatingSystemMXBean`), tightening limits automatically under system strain.
- **Circuit Breaker** — a full `CLOSED` → `OPEN` → `HALF_OPEN` state machine protecting against cascading failures when a downstream dependency is unhealthy.
- **Spring AOP integration** — custom `@RateLimited` and `@CircuitBreakerProtected` annotations apply these behaviors to any method with zero boilerplate, via `@Around` advice.
- **SpEL-based dynamic key resolution** — rate limits can be scoped per-user, per-IP, or any runtime value (`key = "#userId"`) instead of one global limit per endpoint.
- **Distributed rate limiting with Redis** — `RedisTokenBucket` shares bucket state across multiple application instances, solving the correctness gap a purely in-memory limiter has in a load-balanced deployment.
- **Proper HTTP semantics** — rate limit and circuit breaker rejections return `429 Too Many Requests` and `503 Service Unavailable` respectively, via dedicated exceptions and `@ExceptionHandler`s.

## Tech Stack

Java 21 · Spring Boot 3 · Spring AOP · Spring Data Redis · Redis (via Docker) · JUnit 5 · Maven

## Project Structure

```
com.demo
├── core/        → RateLimiter interface, TokenBucket, SlidingWindowLimiter,
│                  AdaptiveTokenBucket, RedisTokenBucket, CircuitBreaker
├── annotation/  → @RateLimited, @CircuitBreakerProtected
├── aspect/      → RateLimitedAspect, CircuitBreakerAspect, custom exceptions
└── DemoApplication / DemoController  → runnable demo
```

## Getting Started

### Prerequisites
- Java 21+
- Maven
- Docker (for the Redis-backed limiter)

### Run Redis
```bash
docker run -d --name redis -p 6379:6379 redis
```

### Run the demo app
```bash
mvn spring-boot:run
```

### Try it out

| Endpoint | Demonstrates |
|---|---|
| `GET /home` | Basic `@RateLimited` — shared bucket, 3 requests/sec |
| `GET /greet?userId=alice` | SpEL key resolution — per-user bucket, isolated from other users |
| `GET /test?fail=true` | `@CircuitBreakerProtected` — trips open after repeated failures, recovers after cooldown |
| `GET /redis-test` | `RedisTokenBucket` — state persisted in Redis, verifiable via `redis-cli` |

Example usage in your own code:
```java
@RateLimited(capacity = 10, refillRate = 2, key = "#userId")
@GetMapping("/api/resource")
public String getResource(@RequestParam String userId) { ... }

@CircuitBreakerProtected(threshold = 5, cooldownTime = 5000)
@GetMapping("/api/external-call")
public String callExternalService() { ... }
```

## Design Notes

- **Why an interface (`RateLimiter`) for the algorithms?** It was introduced specifically when the AOP layer needed to treat different implementations interchangeably — not added upfront as a default. `TokenBucket`, `SlidingWindowLimiter`, `AdaptiveTokenBucket`, and `RedisTokenBucket` all satisfy the same contract, so the Aspect doesn't need to know which one it's using.
- **Why `@Around` advice?** Rate limiting and circuit breaking both need to *conditionally prevent* the original method from running — something only `@Around` (not `@Before`) can do, since it controls whether `joinPoint.proceed()` is ever called.
- **Thread safety:** all in-memory implementations use `synchronized` to prevent races between threads within a single JVM instance.

## Known Limitations

- **Distributed race condition in `RedisTokenBucket`:** `tryConsume()` currently performs separate `GET`/`SET` calls to Redis rather than one atomic operation. Under concurrent requests from *different* application instances, there is a narrow window where two instances could both read the same state and both be allowed through, slightly over-admitting requests. The correct fix is to move the read-check-write logic into a Redis Lua script (executed atomically via `EVAL`) or use Redis transactions (`MULTI`/`EXEC`). This is understood and left as a documented next step rather than implemented under this project's time constraints.
- **Adaptive limiter's high-load behavior is not unit tested.** `AdaptiveTokenBucketTest` only verifies the low-load (<50% CPU) path, since reliably forcing high CPU load in a unit test isn't practical. The medium/high-load scaling branches have been manually verified but not automated.
- **No async/local-cache layer for Redis calls.** Every `tryConsume()` on the Redis-backed limiter makes a synchronous round-trip to Redis. A local in-memory cache with asynchronous background sync (reducing per-request latency) was scoped as a future enhancement rather than built now, to keep the Redis integration correctness-first.

## Possible Future Work

- Atomic Lua-script-based Redis operations (see above)
- Async local-cache prefetching for the distributed limiter
- Integration tests (`@SpringBootTest` + `MockMvc`) for the Aspects
- JitPack-based publishing so the library is installable via Maven/Gradle from GitHub directly
