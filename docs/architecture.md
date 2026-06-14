# Architecture

## Boundaries

| Boundary | Responsibility |
| --- | --- |
| `controller` / `api` | HTTP, authentication context, views, JSON responses |
| `service` | Application use cases and user-specific behavior |
| `repository` | Persistence ports implemented by Spring Data JPA |
| `integration.nasa` | NASA transport, resilience, and shared caching |
| `model.response.nasa.api` | NASA wire models |
| `model.db` | Persistence entities |
| `configuration` | Typed configuration, security, cache, HTTP client, correlation IDs |

The codebase is intentionally a modular monolith. Splitting it into services
would add deployment and consistency cost without a demonstrated need.

## Request flows

### Feed

1. MVC controller obtains the authenticated username.
2. `AsteroidFeedService` asks `NasaClient` for shared upstream data.
3. `NasaApiClient` applies cache, retry, circuit breaker, and timeout behavior.
4. The application service creates a defensive copy.
5. Favorite flags are applied only to that copy.

### Favorite mutation

1. Spring Security requires an authenticated `USER` and valid CSRF token.
2. The application service scopes every lookup by username and asteroid ID.
3. A database unique constraint makes duplicate concurrent adds idempotent.

## Operations

- `/actuator/health/liveness` reports process liveness.
- `/actuator/health/readiness` includes dependency readiness.
- Every response receives `X-Correlation-ID`; the same value appears in logs.
- API errors use a stable JSON shape with timestamp, status, code, message,
  path, and correlation ID.
