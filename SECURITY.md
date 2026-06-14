# Security policy

## Reporting

Do not open a public issue for a suspected vulnerability. Use GitHub's private
vulnerability reporting feature or contact the repository owner privately.

Include the affected revision, reproduction steps, impact, and suggested
mitigation. Do not include live secrets in a report.

## Required owner action for the previously exposed NASA key

A NASA API key was committed before the security hardening work. The current
tree no longer contains it, but it must be treated as compromised.

1. Revoke or regenerate the key in the owner's NASA API account immediately.
2. Replace the key in every legitimate deployment secret store.
3. Coordinate a history rewrite with all collaborators. Rewriting history
   invalidates existing clones and commit hashes.
4. Install `git-filter-repo`, make a fresh mirror clone, and replace the revoked
   value without committing the replacement file:

```bash
git clone --mirror https://github.com/albonidrizi/asteral-nasa-near-earth-objects-explorer.git
cd asteral-nasa-near-earth-objects-explorer.git
printf 'literal:<REVOKED_NASA_KEY>==>***REMOVED***\n' > ../replacements.txt
git filter-repo --replace-text ../replacements.txt --force
git push --force --mirror
rm ../replacements.txt
```

5. Ask collaborators to delete old clones and clone again.
6. Re-run Gitleaks across the full rewritten history.

Revocation is required even after history cleanup because forks, caches, and
existing clones may retain the old value.

## Supported versions

Only the default branch is maintained.

## Operational guidance

- Run production with `SPRING_PROFILES_ACTIVE=prod`.
- Supply `NASA_API_KEY`, `DB_USERNAME`, and `DB_PASSWORD` from a secret manager.
- Never use NASA's `DEMO_KEY` or local Compose credentials for production.
- Keep Actuator endpoints private except the health probe paths.
- Review CodeQL, Gitleaks, dependency review, and Trivy findings before release.
