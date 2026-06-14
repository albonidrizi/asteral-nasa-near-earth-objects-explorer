# Cache performance evidence

Run:

```bash
./mvnw -Pperformance -Dtest=CachePerformanceEvidenceTest test
```

Method:

- Local WireMock upstream with a fixed 75 ms response delay
- 10 uncached requests using distinct date-range keys
- 10 requests using one shared cache key
- Median wall-clock duration reported by the test
- Expected upstream request count: 11

Latest measured result on June 14, 2026:

| Metric | Median | Samples |
| --- | ---: | ---: |
| Uncached request | 92 ms | 10 |
| Cached request | below 1 ms | 10 |

The test verified 11 upstream requests: 10 distinct uncached keys and one
request for the repeatedly accessed cached key.

This synthetic test demonstrates that cache hits avoid upstream latency. It is
not a production throughput or NASA API benchmark.
