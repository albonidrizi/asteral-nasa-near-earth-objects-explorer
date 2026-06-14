# ADR-003: Retry owns fallback

Status: Accepted

`NasaApiClient` applies circuit breaker and retry, with fallback attached to
retry. The breaker observes failed attempts; retry decides when attempts are
exhausted and invokes the defined fallback.

A WireMock rate-limit test guards against the regression where breaker fallback
made retry observe a successful empty response.
