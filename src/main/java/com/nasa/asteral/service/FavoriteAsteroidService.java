package com.nasa.asteral.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import com.nasa.asteral.model.db.FavoriteAsteroid;
import com.nasa.asteral.model.response.dto.AsteroidDetailResponse;
import com.nasa.asteral.repository.FavoriteAsteroidRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FavoriteAsteroidService {
	
	private final FavoriteAsteroidRepository favoriteAsteroidRepository;
	
	private final AsteroidDetailService asteroidDetailService;
	
	@Transactional
	public void addAsteroidToFavorite(String asteroidReferenceId, String username) {
		Optional<FavoriteAsteroid>  optionalFavoriteAsteroid = favoriteAsteroidRepository
				.findByAsteroidReferenceIdAndUsername(asteroidReferenceId, username);
		
		if (optionalFavoriteAsteroid.isPresent()) {
			return;
		}
		
		FavoriteAsteroid favoriteAsteroid = FavoriteAsteroid
				.builder()
				.asteroidReferenceId(asteroidReferenceId)
				.username(username)
				.build();
		
		try {
			favoriteAsteroidRepository.saveAndFlush(favoriteAsteroid);
		} catch (DataIntegrityViolationException ignored) {
			// The database uniqueness constraint makes concurrent duplicate adds idempotent.
		}
	}
	
	public List<AsteroidDetailResponse> getFavoriteAsteroids(String username) {
		List<AsteroidDetailResponse> favoriteAsteroidDetails = favoriteAsteroidRepository.findByUsername(username)
			.stream()
			.map(favoriteAsteroid -> asteroidDetailService.getAsteroidDetailsById(favoriteAsteroid.getAsteroidReferenceId()))
			.collect(Collectors.toList());
		
		return favoriteAsteroidDetails;
	}
	
	@Transactional
	public void removeAsteroidFromFavorite(String referenceId, String username) {
		Optional<FavoriteAsteroid> optionalFavoriteAsteroid = favoriteAsteroidRepository
				.findByAsteroidReferenceIdAndUsername(referenceId, username);
		
		if (optionalFavoriteAsteroid.isEmpty()) {
			return;
		}
		
		FavoriteAsteroid favoriteAsteroid = optionalFavoriteAsteroid.get();
		if (favoriteAsteroid != null) {
			favoriteAsteroidRepository.delete(favoriteAsteroid);
		}
	}
	
	public boolean isFavoriteAsteroid(String asteroidReferenceId, String username) {
		return favoriteAsteroidRepository.findByAsteroidReferenceIdAndUsername(asteroidReferenceId, username)
				.isPresent();
	}
	
	public long countFavoriteAsteroids(String username) {
		return favoriteAsteroidRepository.countByUsername(username);
	}

}
