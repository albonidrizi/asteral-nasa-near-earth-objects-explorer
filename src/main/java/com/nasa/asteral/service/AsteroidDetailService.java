package com.nasa.asteral.service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.nasa.asteral.model.response.dto.AsteroidDetailResponse;
import com.nasa.asteral.model.response.nasa.api.AsteroidResponse;
import com.nasa.asteral.model.response.nasa.api.CloseApprochResponse;
import com.nasa.asteral.utility.DateUtility;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AsteroidDetailService {

	@Value("${asteral.nasa.api.key}")
	private String apiKey;

	@Value("${asteral.nasa.api.details.endpoint}")
	private String asteroidDetailsEndpoint;

	private final IntegrationService integrationService;

	@Cacheable(cacheNames = "asteroid-details", key = "#referenceId")
	@CircuitBreaker(name = "nasaApi")
	@Retry(name = "nasaApi")
	public AsteroidDetailResponse getAsteroidDetailsById(String referenceId) {
		String requestEndpoint = "%s/%s".formatted(asteroidDetailsEndpoint, referenceId);

		Map<String, String> parameters = new HashMap<>();
		parameters.put("api_key", apiKey);

		AsteroidResponse asteroidResponse = integrationService
				.doGetRequest(requestEndpoint, parameters, AsteroidResponse.class);

		return mapToDetailResponse(asteroidResponse);
	}

	private AsteroidDetailResponse mapToDetailResponse(AsteroidResponse asteroidResponse) {
		AsteroidDetailResponse asteroidDetailResponse = AsteroidDetailResponse
				.builder()
				.referenceId(asteroidResponse.getReferenceId())
				.name(asteroidResponse.getName())
				.absoluteMagnitude(asteroidResponse.getAbsoluteMagnitude())
				.estimatedDiameterMinKm(
						asteroidResponse.getEstimatedDiameter().getKilometers().getEstimatedDiameterMin())
				.estimatedDiameterMaxKm(
						asteroidResponse.getEstimatedDiameter().getKilometers().getEstimatedDiameterMax())
				.build();

		if (!asteroidResponse.getCloseApprochData().isEmpty()) {
			String orbitingBody = asteroidResponse.getCloseApprochData().get(0).getOrbitingBody();
			asteroidDetailResponse.setOrbitingBody(orbitingBody);
		}

		/*
		 * Get the next approaching date by filtering all the past dates and now date
		 * and getting
		 * the first element from what is left in the list.
		 */
		LocalDate nextApproachingDate = asteroidResponse.getCloseApprochData()
				.stream()
				.map(this::mapCloseApproachToDate)
				.filter(this::filterPastDate)
				.findFirst().orElse(null);

		if (nextApproachingDate != null) {
			asteroidDetailResponse.setLastCloseApproachingDate(DateUtility.getDateAsString(nextApproachingDate));
		}

		return asteroidDetailResponse;
	}

	private LocalDate mapCloseApproachToDate(CloseApprochResponse closeApprochResponse) {
		return DateUtility.getStringAsDate(closeApprochResponse.getCloseApprochDate());
	}

	private boolean filterPastDate(LocalDate date) {
		return date.isAfter(DateUtility.getTodaysDateIgnoringTime());
	}

}
