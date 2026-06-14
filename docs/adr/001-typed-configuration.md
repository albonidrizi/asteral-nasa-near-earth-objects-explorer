# ADR-001: Typed configuration and production secret validation

Status: Accepted

Use validated `NasaApiProperties` instead of scattered `@Value` fields.
Default/demo mode may use NASA's public `DEMO_KEY`; the `prod` profile requires
`NASA_API_KEY`, `DB_USERNAME`, and `DB_PASSWORD` placeholders to resolve.

An environment post-processor checks those names before the application context
or database connection is created. This preserves easy local onboarding while
making missing production secrets an explicit startup failure.
