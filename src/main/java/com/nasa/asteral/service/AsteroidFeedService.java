package com.nasa.asteral.service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.nasa.asteral.model.response.nasa.api.AsteroidFeedResponse;
import com.nasa.asteral.utility.DateUtility;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AsteroidFeedService {

	@Value("${asteral.nasa.api.key}")
	private String apiKey;

	@Value("${asteral.nasa.api.feed.endpoint}")
	private String asteroidFeedEndpoint;

	private final IntegrationService integrationService;

	private final FavoriteAsteroidService favoriteAsteroidService;

	@Cacheable(cacheNames = "asteroid-feed", key = "#username ?: 'anonymous'")
	@CircuitBreaker(name = "nasaApi")
	@Retry(name = "nasaApi")
	public AsteroidFeedResponse getAsteroidFeed(String username) {
		LocalDate startDate = LocalDate.now().minusDays(1);
		LocalDate endDate = LocalDate.now();

		Map<String, String> parameters = new HashMap<>();
		parameters.put("start_date", DateUtility.getDateAsString(startDate));
		parameters.put("end_date", DateUtility.getDateAsString(endDate));
		parameters.put("api_key", apiKey);

		AsteroidFeedResponse feedResponse = integrationService
				.doGetRequest(asteroidFeedEndpoint, parameters, AsteroidFeedResponse.class);

		setFavoriteToResponse(feedResponse, username);

		return feedResponse;
	}

	private void setFavoriteToResponse(AsteroidFeedResponse asteroidFeedResponse, String username) {
		if (username == null || username.isEmpty()) {
			return;
		}

		asteroidFeedResponse.getNearEarthObjects().forEach((key, value) -> {
			value.forEach(asteroid -> {
				boolean isFavorite = favoriteAsteroidService.isFavoriteAsteroid(asteroid.getReferenceId(), username);
				asteroid.setFavorite(isFavorite);
			});
		});
	}

}
