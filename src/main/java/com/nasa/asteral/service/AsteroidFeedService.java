package com.nasa.asteral.service;

import java.time.LocalDate;
import java.time.Clock;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.nasa.asteral.integration.nasa.NasaClient;
import com.nasa.asteral.model.response.nasa.api.AsteroidFeedResponse;
import com.nasa.asteral.model.response.nasa.api.AsteroidResponse;
import com.nasa.asteral.model.response.nasa.api.CloseApprochResponse;
import com.nasa.asteral.model.response.nasa.api.EstimatedDiameterDetailResponse;
import com.nasa.asteral.model.response.nasa.api.EstimatedDiameterResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AsteroidFeedService {

	private final NasaClient nasaClient;
	private final FavoriteAsteroidService favoriteAsteroidService;
	private final Clock clock;

	public AsteroidFeedResponse getAsteroidFeed(String username) {
		LocalDate endDate = LocalDate.now(clock);
		AsteroidFeedResponse feedResponse = copy(nasaClient.fetchFeed(endDate.minusDays(1), endDate));
		setFavoriteToResponse(feedResponse, username);
		return feedResponse;
	}

	private void setFavoriteToResponse(AsteroidFeedResponse asteroidFeedResponse, String username) {
		if (username == null || username.isEmpty()) {
			return;
		}

		if (asteroidFeedResponse.getNearEarthObjects() == null) {
			return;
		}
		asteroidFeedResponse.getNearEarthObjects().forEach((key, value) -> {
			value.forEach(asteroid -> {
				boolean isFavorite = favoriteAsteroidService.isFavoriteAsteroid(asteroid.getReferenceId(), username);
				asteroid.setFavorite(isFavorite);
			});
		});
	}

	private AsteroidFeedResponse copy(AsteroidFeedResponse source) {
		AsteroidFeedResponse copy = new AsteroidFeedResponse();
		if (source == null || source.getNearEarthObjects() == null) {
			copy.setNearEarthObjects(new LinkedHashMap<>());
			return copy;
		}
		copy.setNearEarthObjects(source.getNearEarthObjects().entrySet().stream()
				.collect(Collectors.toMap(
						java.util.Map.Entry::getKey,
						entry -> entry.getValue().stream().map(this::copyAsteroid).toList(),
						(left, right) -> left,
						LinkedHashMap::new)));
		return copy;
	}

	private AsteroidResponse copyAsteroid(AsteroidResponse source) {
		AsteroidResponse copy = new AsteroidResponse();
		copy.setReferenceId(source.getReferenceId());
		copy.setName(source.getName());
		copy.setAbsoluteMagnitude(source.getAbsoluteMagnitude());
		copy.setPotentiallyHazardous(source.isPotentiallyHazardous());
		copy.setEstimatedDiameter(copyDiameter(source.getEstimatedDiameter()));
		copy.setCloseApprochData(copyApproaches(source.getCloseApprochData()));
		return copy;
	}

	private EstimatedDiameterResponse copyDiameter(EstimatedDiameterResponse source) {
		if (source == null || source.getKilometers() == null) {
			return null;
		}
		EstimatedDiameterDetailResponse detail = new EstimatedDiameterDetailResponse();
		detail.setEstimatedDiameterMin(source.getKilometers().getEstimatedDiameterMin());
		detail.setEstimatedDiameterMax(source.getKilometers().getEstimatedDiameterMax());
		EstimatedDiameterResponse copy = new EstimatedDiameterResponse();
		copy.setKilometers(detail);
		return copy;
	}

	private List<CloseApprochResponse> copyApproaches(List<CloseApprochResponse> source) {
		if (source == null) {
			return List.of();
		}
		return source.stream().map(approach -> {
			CloseApprochResponse copy = new CloseApprochResponse();
			copy.setCloseApprochDate(approach.getCloseApprochDate());
			copy.setOrbitingBody(approach.getOrbitingBody());
			return copy;
		}).toList();
	}
}
