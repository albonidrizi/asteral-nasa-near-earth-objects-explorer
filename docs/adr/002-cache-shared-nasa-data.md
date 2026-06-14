# ADR-002: Cache shared NASA data only

Status: Accepted

Cache keys identify upstream resources: date range for feed data and reference
ID for asteroid details. Cached values never contain usernames or favorite
flags. Application services copy shared responses before applying user state.

This prevents user-specific data leakage and reduces cache cardinality.
