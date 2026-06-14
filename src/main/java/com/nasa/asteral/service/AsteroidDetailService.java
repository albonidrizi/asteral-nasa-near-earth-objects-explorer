package com.nasa.asteral.service;

import java.time.LocalDate;
import java.time.Clock;
import java.util.Comparator;

import org.springframework.stereotype.Service;

import com.nasa.asteral.integration.nasa.NasaClient;
import com.nasa.asteral.model.response.dto.AsteroidDetailResponse;
import com.nasa.asteral.model.response.nasa.api.AsteroidResponse;
import com.nasa.asteral.model.response.nasa.api.CloseApprochResponse;
import com.nasa.asteral.utility.DateUtility;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AsteroidDetailService {

	private final NasaClient nasaClient;
	private final Clock clock;

	public AsteroidDetailResponse getAsteroidDetailsById(String referenceId) {
		return mapToDetailResponse(nasaClient.fetchAsteroid(referenceId));
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
				.potentiallyHazardous(asteroidResponse.isPotentiallyHazardous())
				.build();

		if (asteroidResponse.getCloseApprochData() != null && !asteroidResponse.getCloseApprochData().isEmpty()) {
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
				.min(Comparator.naturalOrder())
				.orElse(null);

		if (nextApproachingDate != null) {
			asteroidDetailResponse.setLastCloseApproachingDate(DateUtility.getDateAsString(nextApproachingDate));
		}

		return asteroidDetailResponse;
	}

	private LocalDate mapCloseApproachToDate(CloseApprochResponse closeApprochResponse) {
		return DateUtility.getStringAsDate(closeApprochResponse.getCloseApprochDate());
	}

	private boolean filterPastDate(LocalDate date) {
		return date.isAfter(LocalDate.now(clock));
	}

}
