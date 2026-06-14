package com.nasa.asteral.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nasa.asteral.model.db.FavoriteAsteroid;
import com.nasa.asteral.model.response.dto.AsteroidDetailResponse;
import com.nasa.asteral.repository.FavoriteAsteroidRepository;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class FavoriteAsteroidServiceTest {

    @Mock
    private FavoriteAsteroidRepository favoriteAsteroidRepository;

    @Mock
    private AsteroidDetailService asteroidDetailService;

    @InjectMocks
    private FavoriteAsteroidService favoriteAsteroidService;

    private FavoriteAsteroid testFavorite;
    private AsteroidDetailResponse testAsteroidDetail;

    @BeforeEach
    void setUp() {
        testFavorite = FavoriteAsteroid.builder()
                .favoriteId(1L)
                .asteroidReferenceId("12345")
                .username("testuser")
                .build();

        testAsteroidDetail = AsteroidDetailResponse.builder()
                .referenceId("12345")
                .name("Test Asteroid")
                .absoluteMagnitude(21.0)
                .estimatedDiameterMinKm(0.1)
                .estimatedDiameterMaxKm(0.2)
                .potentiallyHazardous(false)
                .orbitingBody("Earth")
                .lastCloseApproachingDate("2025-01-01")
                .build();
    }

    @Test
    void addAsteroidToFavorite_whenNotExists_shouldSave() {
        // Arrange
        when(favoriteAsteroidRepository.findByAsteroidReferenceIdAndUsername("12345", "testuser"))
                .thenReturn(Optional.empty());

        // Act
        favoriteAsteroidService.addAsteroidToFavorite("12345", "testuser");

        // Assert
        verify(favoriteAsteroidRepository, times(1)).saveAndFlush(any(FavoriteAsteroid.class));
    }

    @Test
    void addAsteroidToFavorite_whenAlreadyExists_shouldNotSave() {
        // Arrange
        when(favoriteAsteroidRepository.findByAsteroidReferenceIdAndUsername("12345", "testuser"))
                .thenReturn(Optional.of(testFavorite));

        // Act
        favoriteAsteroidService.addAsteroidToFavorite("12345", "testuser");

        // Assert
        verify(favoriteAsteroidRepository, never()).save(any(FavoriteAsteroid.class));
    }

    @Test
    void getFavoriteAsteroids_shouldReturnList() {
        // Arrange
        List<FavoriteAsteroid> favorites = Arrays.asList(testFavorite);
        when(favoriteAsteroidRepository.findByUsername("testuser")).thenReturn(favorites);
        when(asteroidDetailService.getAsteroidDetailsById("12345")).thenReturn(testAsteroidDetail);

        // Act
        List<AsteroidDetailResponse> result = favoriteAsteroidService.getFavoriteAsteroids("testuser");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("12345", result.get(0).getReferenceId());
        verify(asteroidDetailService, times(1)).getAsteroidDetailsById("12345");
    }

    @Test
    void removeAsteroidFromFavorite_whenExists_shouldDelete() {
        // Arrange
        when(favoriteAsteroidRepository.findByAsteroidReferenceIdAndUsername("12345", "testuser"))
                .thenReturn(Optional.of(testFavorite));

        // Act
        favoriteAsteroidService.removeAsteroidFromFavorite("12345", "testuser");

        // Assert
        verify(favoriteAsteroidRepository, times(1)).delete(testFavorite);
    }

    @Test
    void removeAsteroidFromFavorite_whenNotExists_shouldNotDelete() {
        // Arrange
        when(favoriteAsteroidRepository.findByAsteroidReferenceIdAndUsername("12345", "testuser"))
                .thenReturn(Optional.empty());

        // Act
        favoriteAsteroidService.removeAsteroidFromFavorite("12345", "testuser");

        // Assert
        verify(favoriteAsteroidRepository, never()).delete(any(FavoriteAsteroid.class));
    }

    @Test
    void isFavoriteAsteroid_whenExists_shouldReturnTrue() {
        // Arrange
        when(favoriteAsteroidRepository.findByAsteroidReferenceIdAndUsername("12345", "testuser"))
                .thenReturn(Optional.of(testFavorite));

        // Act
        boolean result = favoriteAsteroidService.isFavoriteAsteroid("12345", "testuser");

        // Assert
        assertTrue(result);
    }

    @Test
    void isFavoriteAsteroid_whenNotExists_shouldReturnFalse() {
        // Arrange
        when(favoriteAsteroidRepository.findByAsteroidReferenceIdAndUsername("12345", "testuser"))
                .thenReturn(Optional.empty());

        // Act
        boolean result = favoriteAsteroidService.isFavoriteAsteroid("12345", "testuser");

        // Assert
        assertFalse(result);
    }

    @Test
    void countFavoriteAsteroids_shouldReturnCount() {
        // Arrange
        when(favoriteAsteroidRepository.countByUsername("testuser")).thenReturn(5L);

        // Act
        long result = favoriteAsteroidService.countFavoriteAsteroids("testuser");

        // Assert
        assertEquals(5L, result);
        verify(favoriteAsteroidRepository, times(1)).countByUsername("testuser");
    }
}
