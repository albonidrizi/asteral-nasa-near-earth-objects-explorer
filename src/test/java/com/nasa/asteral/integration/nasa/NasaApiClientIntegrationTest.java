package com.nasa.asteral.integration.nasa;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import com.nasa.asteral.model.response.nasa.api.AsteroidFeedResponse;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;

@SpringBootTest
@ActiveProfiles("test")
class NasaApiClientIntegrationTest {

    private static final WireMockServer NASA = new WireMockServer(options().dynamicPort());

    static {
        NASA.start();
    }

    @Autowired
    private NasaClient nasaClient;
    @Autowired
    private CacheManager cacheManager;
    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @DynamicPropertySource
    static void nasaProperties(DynamicPropertyRegistry registry) {
        registry.add("asteral.nasa.api.base-url", NASA::baseUrl);
        registry.add("asteral.nasa.api.read-timeout", () -> "100ms");
        registry.add("resilience4j.retry.instances.nasaApi.max-attempts", () -> "3");
        registry.add("resilience4j.retry.instances.nasaApi.wait-duration", () -> "5ms");
        registry.add("resilience4j.circuitbreaker.instances.nasaApi.minimum-number-of-calls", () -> "4");
        registry.add("resilience4j.circuitbreaker.instances.nasaApi.sliding-window-size", () -> "4");
        registry.add("resilience4j.circuitbreaker.instances.nasaApi.wait-duration-in-open-state", () -> "1h");
    }

    @BeforeEach
    void resetState() {
        NASA.resetAll();
        cacheManager.getCache("asteroid-feed").clear();
        cacheManager.getCache("asteroid-details").clear();
        circuitBreakerRegistry.circuitBreaker("nasaApi").reset();
    }

    @AfterAll
    static void stopServer() {
        NASA.stop();
    }

    @Test
    void parsesSuccessfulFeedAndCachesByDateRange() {
        NASA.stubFor(get(urlPathEqualTo("/feed"))
                .withQueryParam("api_key", equalTo("test-key"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(feedJson())));

        AsteroidFeedResponse first = nasaClient.fetchFeed(date("2026-06-13"), date("2026-06-14"));
        AsteroidFeedResponse second = nasaClient.fetchFeed(date("2026-06-13"), date("2026-06-14"));

        assertEquals("123", first.getNearEarthObjects().get("2026-06-14").get(0).getReferenceId());
        assertEquals(first, second);
        NASA.verify(1, getRequestedFor(urlPathEqualTo("/feed")));
    }

    @Test
    void retriesRateLimitThenSucceeds() {
        NASA.stubFor(get(urlPathEqualTo("/feed")).inScenario("rate-limit")
                .whenScenarioStateIs(Scenario.STARTED)
                .willSetStateTo("second")
                .willReturn(aResponse().withStatus(429)));
        NASA.stubFor(get(urlPathEqualTo("/feed")).inScenario("rate-limit")
                .whenScenarioStateIs("second")
                .willSetStateTo("success")
                .willReturn(aResponse().withStatus(429)));
        NASA.stubFor(get(urlPathEqualTo("/feed")).inScenario("rate-limit")
                .whenScenarioStateIs("success")
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(feedJson())));

        AsteroidFeedResponse response = nasaClient.fetchFeed(date("2026-06-13"), date("2026-06-14"));

        assertEquals(1, response.getNearEarthObjects().size());
        NASA.verify(3, getRequestedFor(urlPathEqualTo("/feed")));
    }

    @Test
    void malformedResponseUsesDefinedFeedFallback() {
        NASA.stubFor(get(urlPathEqualTo("/feed"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody("{broken")));

        AsteroidFeedResponse response = nasaClient.fetchFeed(date("2026-06-13"), date("2026-06-14"));

        assertTrue(response.getNearEarthObjects().isEmpty());
    }

    @Test
    void timeoutUsesDefinedFeedFallback() {
        NASA.stubFor(get(urlPathEqualTo("/feed"))
                .willReturn(aResponse().withFixedDelay(500)
                        .withHeader("Content-Type", "application/json").withBody(feedJson())));

        AsteroidFeedResponse response = nasaClient.fetchFeed(date("2026-06-13"), date("2026-06-14"));

        assertTrue(response.getNearEarthObjects().isEmpty());
    }

    @Test
    void repeatedFailuresOpenCircuit() {
        NASA.stubFor(get(urlPathMatching("/feed")).willReturn(aResponse().withStatus(500)));

        nasaClient.fetchFeed(date("2026-06-08"), date("2026-06-09"));
        nasaClient.fetchFeed(date("2026-06-10"), date("2026-06-11"));

        assertEquals(CircuitBreaker.State.OPEN, circuitBreakerRegistry.circuitBreaker("nasaApi").getState());
    }

    private java.time.LocalDate date(String value) {
        return java.time.LocalDate.parse(value);
    }

    private String feedJson() {
        return """
                {
                  "near_earth_objects": {
                    "2026-06-14": [
                      {
                        "neo_reference_id": "123",
                        "name": "Test Asteroid",
                        "absolute_magnitude_h": 20.1,
                        "estimated_diameter": {
                          "kilometers": {
                            "estimated_diameter_min": 0.1,
                            "estimated_diameter_max": 0.2
                          }
                        },
                        "is_potentially_hazardous_asteroid": false,
                        "close_approach_data": []
                      }
                    ]
                  }
                }
                """;
    }
}
