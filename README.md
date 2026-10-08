# Rate Limiter & Circuit Breaker Library

A Java library implementing rate limiting and fault tolerance patterns from scratch, with Spring Boot integration using custom annotations and AOP.

The project focuses on understanding how rate limiting, circuit breakers, adaptive limiting, and distributed rate limiting work internally rather than simply using existing libraries such as Bucket4j or Resilience4j.

## Features

- **Token Bucket Rate Limiter**
  - Supports controlled bursts while limiting the average request rate.

- **Sliding Window Rate Limiter**
  - Provides strict and smooth request limiting over a configurable time window.

- **Adaptive Rate Limiter**
  - Dynamically adjusts capacity and refill rate based on system CPU utilization.

- **Circuit Breaker**
  - Implements the `CLOSED → OPEN → HALF_OPEN` state machine.
  - Prevents repeated calls to unhealthy downstream services.

- **Spring AOP Integration**
  - Provides `@RateLimited` and `@CircuitBreakerProtected` annotations.
  - Protection can be added to Spring methods without modifying their business logic.

- **SpEL-Based Dynamic Keys**
  - Supports runtime keys such as user IDs for isolated rate-limit buckets.
  - Example: `key = "#userId"`.

- **Redis-Based Distributed Rate Limiting**
  - `RedisTokenBucket` stores rate-limit state in Redis.
  - Allows multiple application instances to share the same rate-limit state.

- **HTTP Error Handling**
  - Rate-limit violations return `429 Too Many Requests`.
  - Circuit-breaker rejections return `503 Service Unavailable`.

## Tech Stack

- Java 21
- Spring Boot 3
- Spring AOP
- Spring Data Redis
- Redis
- JUnit 5
- Maven

## Installation

The library is published to Maven Central.

Add the following dependency to your Spring Boot project's `pom.xml`:

```xml
<dependency>
    <groupId>io.github.raut-abhishek</groupId>
    <artifactId>rate-limiter-circuit-breaker</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Usage

### Rate Limiting

Use the `@RateLimited` annotation to protect a Spring method:

```java
@RateLimited(
    capacity = 10,
    refillRate = 2,
    key = "#userId"
)
@GetMapping("/api/resource")
public String getResource(@RequestParam String userId) {
    return "Request allowed";
}
```

In this example:

- `capacity = 10` defines the maximum number of tokens.
- `refillRate = 2` replenishes two tokens per second.
- `key = "#userId"` creates a separate rate-limit bucket for each user.

### Circuit Breaker

Protect a method using `@CircuitBreakerProtected`:

```java
@CircuitBreakerProtected(
    threshold = 5,
    cooldownTime = 5000
)
@GetMapping("/api/external-call")
public String callExternalService() {
    return "External service response";
}
```

After the configured failure threshold is reached, the circuit opens and prevents further calls until the cooldown period has elapsed.

## Redis Rate Limiting

The library provides `RedisTokenBucket` for distributed rate limiting.

A Redis-backed limiter allows multiple application instances to share rate-limit state:

```text
                  Load Balancer
                       |
             +---------+---------+
             |                   |
       Application A       Application B
             |                   |
             +---------+---------+
                       |
                     Redis
```

This is useful when an application is deployed across multiple instances because each instance can access the same rate-limit state.

## Project Structure

```text
com.demo
├── annotation
│   ├── RateLimited
│   └── CircuitBreakerProtected
│
├── aspect
│   ├── RateLimitedAspect
│   ├── CircuitBreakerAspect
│   ├── RateLimitExceededException
│   ├── CircuitBreakerOpenException
│   └── ErrorResponse
│
└── core
    ├── RateLimiter
    ├── TokenBucket
    ├── SlidingWindowLimiter
    ├── AdaptiveTokenBucket
    ├── RedisTokenBucket
    └── CircuitBreaker
```

## Design Notes

### RateLimiter Interface

The `RateLimiter` interface provides a common contract for the different rate-limiting algorithms.

The following implementations use this interface:

- `TokenBucket`
- `SlidingWindowLimiter`
- `AdaptiveTokenBucket`
- `RedisTokenBucket`

This allows the rate-limiting implementations to be treated interchangeably.

### Why `@Around` Advice?

Both rate limiting and circuit breaking may need to prevent the target method from executing.

`@Around` advice allows the aspect to decide whether `joinPoint.proceed()` should be called.

This makes it possible to:

- Allow a request to continue.
- Block a request when the rate limit is exceeded.
- Block a request when the circuit is open.

### Thread Safety

The in-memory rate limiter implementations use synchronization to prevent race conditions between concurrent threads within a single JVM instance.

## Known Limitations

### Redis Operation Is Not Atomic

`RedisTokenBucket.tryConsume()` currently performs separate `GET` and `SET` operations.

Under concurrent requests from different application instances, there is a narrow race-condition window where multiple instances could read the same state and both be allowed through.

A future implementation should move the read-check-write operation into an atomic Redis Lua script using `EVAL`, or use Redis transactions.

### Adaptive Limiter High-Load Testing

`AdaptiveTokenBucketTest` currently verifies the low-load path.

Reliably forcing high CPU utilization during a unit test is not practical, so the medium/high-load scaling branches have been manually verified but are not fully automated.

### Synchronous Redis Calls

Every `tryConsume()` call on `RedisTokenBucket` performs a synchronous round trip to Redis.

A future implementation could introduce local caching and asynchronous synchronization to reduce Redis latency while maintaining distributed rate-limit behavior.

## Testing

The project includes unit tests for:

- `TokenBucket`
- `SlidingWindowLimiter`
- `AdaptiveTokenBucket`
- `CircuitBreaker`

Current test suite:

```text
Tests run: 4
Failures: 0
Errors: 0
Skipped: 0
```

## Future Work

- Atomic Redis operations using Lua scripts.
- Improved distributed concurrency handling.
- Optional local caching for Redis-backed rate limiting.
- Integration tests using `@SpringBootTest` and `MockMvc`.
- More comprehensive adaptive rate-limiter tests.
- Improved Javadocs and public API documentation.
- Additional rate-limiting algorithms.

## License

This project is licensed under the MIT License.
