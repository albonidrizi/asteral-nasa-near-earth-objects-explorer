# Technical case study

## Problem

The original application demonstrated a useful NASA data workflow, but secrets,
schema mutation, user-aware cache entries, state-changing GET requests, and
unverified portfolio claims made it difficult to trust or operate.

## Decisions

- **Modular monolith:** the application is small enough that explicit package
  boundaries provide clarity without distributed-system overhead.
- **Shared upstream cache:** NASA responses are cached by upstream identity
  (date range or asteroid ID). User favorites are added after a defensive copy,
  preventing identity leakage and stale user-specific cache entries.
- **Retry owns fallback:** fallback is attached to retry, while the circuit
  breaker observes each attempt. Attaching fallback to the breaker caused retry
  to see a successful empty result and never retry.
- **Flyway over Hibernate updates:** migrations make schema changes reviewable
  and repeatable.
- **Deterministic external tests:** WireMock proves failure behavior without a
  real NASA credential or network dependency.
- **Generated demo password:** the Compose demo remains one command without
  committing a reusable password.

## Tradeoffs

- Empty-feed fallback keeps the UI available but can hide stale data; a future
  stale-cache design should expose freshness.
- The public NASA `DEMO_KEY` makes onboarding easy but is heavily rate limited.
- H2 keeps the default suite fast; the explicit `integration` Maven profile
  requires Testcontainers and fails if PostgreSQL-specific proof cannot run.
- Kubernetes intentionally omits database and ingress provisioning.

## Lessons

Resilience annotations are executable control flow, not decoration. Their
ordering must be tested. Cache keys are also security boundaries when values can
contain user-specific state. Finally, portfolio claims are strongest when every
claim points to a command, test, or artifact that can be reproduced.
