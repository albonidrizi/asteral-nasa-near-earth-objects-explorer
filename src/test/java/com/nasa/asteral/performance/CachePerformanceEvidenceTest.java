package com.nasa.asteral.performance;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.nasa.asteral.integration.nasa.NasaClient;

@SpringBootTest
@ActiveProfiles("test")
@Tag("performance")
class CachePerformanceEvidenceTest {

    private static final WireMockServer NASA = new WireMockServer(options().dynamicPort());

    static {
        NASA.start();
    }

    @Autowired
    private NasaClient nasaClient;
    @Autowired
    private CacheManager cacheManager;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("asteral.nasa.api.base-url", NASA::baseUrl);
        registry.add("asteral.nasa.api.read-timeout", () -> "1s");
    }

    @BeforeEach
    void setUp() {
        NASA.resetAll();
        cacheManager.getCache("asteroid-feed").clear();
        NASA.stubFor(get(urlPathEqualTo("/feed"))
                .willReturn(aResponse().withFixedDelay(75)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"near_earth_objects\":{}}")));
    }

    @AfterAll
    static void stop() {
        NASA.stop();
    }

    @Test
    void measureCachedAndUncachedFeedRequests() {
        List<Duration> uncached = new ArrayList<>();
        for (int index = 0; index < 10; index++) {
            LocalDate start = LocalDate.of(2026, 1, 1).plusDays(index);
            uncached.add(measure(() -> nasaClient.fetchFeed(start, start.plusDays(1))));
        }

        cacheManager.getCache("asteroid-feed").clear();
        List<Duration> cached = new ArrayList<>();
        for (int index = 0; index < 10; index++) {
            cached.add(measure(() -> nasaClient.fetchFeed(LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 2))));
        }

        NASA.verify(11, getRequestedFor(urlPathEqualTo("/feed")));
        System.out.printf("CACHE_BENCHMARK uncached_median_ms=%d cached_median_ms=%d samples=10 synthetic_upstream_delay_ms=75%n",
                median(uncached).toMillis(), median(cached).toMillis());
    }

    private Duration measure(Runnable action) {
        long start = System.nanoTime();
        action.run();
        return Duration.ofNanos(System.nanoTime() - start);
    }

    private Duration median(List<Duration> samples) {
        List<Duration> sorted = samples.stream().sorted(Comparator.naturalOrder()).toList();
        return sorted.get(sorted.size() / 2);
    }
}
