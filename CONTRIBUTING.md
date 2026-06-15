# Contributing

## Development workflow

1. Use Java 21 and a running Docker engine for the complete integration suite.
2. Create a focused branch and keep secrets out of files, commits, logs, and
   screenshots.
3. Run:

```bash
./mvnw clean verify
./mvnw -Pintegration clean verify
DB_PASSWORD=verification-only docker compose config --quiet
kubectl kustomize k8s
```

4. Add or update tests for behavioral changes.
5. Update README claims and ADRs when architecture or operational behavior
   changes.

## Design expectations

- Keep controllers thin.
- Put use-case orchestration in application services.
- Keep NASA transport concerns behind `NasaClient`.
- Treat cached values as shared and free of user-specific state.
- Use Flyway for schema changes.
- Prefer deterministic tests over live external services.

## Commits

Use small, descriptive commits such as `fix: protect favorite mutations with
csrf` or `docs: record cache benchmark evidence`.
