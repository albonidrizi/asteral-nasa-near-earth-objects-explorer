# ADR-004: Flyway owns schema changes

Status: Accepted

Flyway migrations create and evolve the PostgreSQL schema. Hibernate validates
the mapped schema and does not mutate it. No user credentials are seeded.

This makes schema changes repeatable, reviewable, and compatible with
Testcontainers and Compose.
