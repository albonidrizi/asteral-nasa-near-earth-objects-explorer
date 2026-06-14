package com.nasa.asteral.integration.nasa;

import java.time.LocalDate;
import java.util.LinkedHashMap;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.nasa.asteral.configuration.NasaApiProperties;
import com.nasa.asteral.exception.NasaApiUnavailableException;
import com.nasa.asteral.model.response.nasa.api.AsteroidFeedResponse;
import com.nasa.asteral.model.response.nasa.api.AsteroidResponse;
import com.nasa.asteral.utility.DateUtility;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NasaApiClient implements NasaClient {

    private final WebClient nasaWebClient;
    private final NasaApiProperties properties;

    @Override
    @Cacheable(cacheNames = "asteroid-feed", key = "#startDate + ':' + #endDate", sync = true)
    @CircuitBreaker(name = "nasaApi")
    @Retry(name = "nasaApi", fallbackMethod = "feedFallback")
    public AsteroidFeedResponse fetchFeed(LocalDate startDate, LocalDate endDate) {
        return nasaWebClient.get()
                .uri(uriBuilder -> uriBuilder.path("/feed")
                        .queryParam("start_date", DateUtility.getDateAsString(startDate))
                        .queryParam("end_date", DateUtility.getDateAsString(endDate))
                        .queryParam("api_key", properties.key())
                        .build())
                .retrieve()
                .bodyToMono(AsteroidFeedResponse.class)
                .blockOptional()
                .orElseThrow(() -> new NasaApiUnavailableException("NASA feed returned an empty response.", null));
    }

    @Override
    @Cacheable(cacheNames = "asteroid-details", key = "#referenceId", sync = true)
    @CircuitBreaker(name = "nasaApi")
    @Retry(name = "nasaApi", fallbackMethod = "asteroidFallback")
    public AsteroidResponse fetchAsteroid(String referenceId) {
        return nasaWebClient.get()
                .uri(uriBuilder -> uriBuilder.path("/neo/{referenceId}")
                        .queryParam("api_key", properties.key())
                        .build(referenceId))
                .retrieve()
                .bodyToMono(AsteroidResponse.class)
                .blockOptional()
                .orElseThrow(() -> new NasaApiUnavailableException("NASA asteroid endpoint returned an empty response.", null));
    }

    AsteroidFeedResponse feedFallback(LocalDate startDate, LocalDate endDate, Throwable cause) {
        AsteroidFeedResponse response = new AsteroidFeedResponse();
        response.setNearEarthObjects(new LinkedHashMap<>());
        return response;
    }

    AsteroidResponse asteroidFallback(String referenceId, Throwable cause) {
        throw new NasaApiUnavailableException("NASA asteroid details are temporarily unavailable.", cause);
    }
}
