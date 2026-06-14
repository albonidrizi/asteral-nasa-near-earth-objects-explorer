package com.nasa.asteral.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nasa.asteral.integration.nasa.NasaClient;
import com.nasa.asteral.model.response.nasa.api.AsteroidFeedResponse;
import com.nasa.asteral.model.response.nasa.api.AsteroidResponse;

@ExtendWith(MockitoExtension.class)
class AsteroidFeedServiceTest {

    @Mock
    private NasaClient nasaClient;
    @Mock
    private FavoriteAsteroidService favoriteService;

    private AsteroidFeedService service;
    private AsteroidFeedResponse cachedFeed;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-06-14T00:00:00Z"), ZoneOffset.UTC);
        service = new AsteroidFeedService(nasaClient, favoriteService, clock);
        cachedFeed = feed();
        when(nasaClient.fetchFeed(LocalDate.parse("2026-06-13"), LocalDate.parse("2026-06-14"))).thenReturn(cachedFeed);
    }

    @Test
    void personalizesDefensiveCopyWithoutMutatingCachedFeed() {
        when(favoriteService.isFavoriteAsteroid("123", "alice")).thenReturn(true);

        AsteroidFeedResponse result = service.getAsteroidFeed("alice");

        assertNotSame(cachedFeed, result);
        assertTrue(result.getNearEarthObjects().get("2026-06-14").get(0).isFavorite());
        assertFalse(cachedFeed.getNearEarthObjects().get("2026-06-14").get(0).isFavorite());
    }

    @Test
    void keepsFavoriteStateIsolatedBetweenUsers() {
        when(favoriteService.isFavoriteAsteroid("123", "alice")).thenReturn(true);
        when(favoriteService.isFavoriteAsteroid("123", "bob")).thenReturn(false);

        AsteroidFeedResponse alice = service.getAsteroidFeed("alice");
        AsteroidFeedResponse bob = service.getAsteroidFeed("bob");

        assertTrue(alice.getNearEarthObjects().get("2026-06-14").get(0).isFavorite());
        assertFalse(bob.getNearEarthObjects().get("2026-06-14").get(0).isFavorite());
    }

    @Test
    void anonymousFeedDoesNotQueryFavorites() {
        service.getAsteroidFeed("");
        verify(favoriteService, never()).isFavoriteAsteroid("123", "");
    }

    private AsteroidFeedResponse feed() {
        AsteroidResponse asteroid = new AsteroidResponse();
        asteroid.setReferenceId("123");
        AsteroidFeedResponse feed = new AsteroidFeedResponse();
        LinkedHashMap<String, List<AsteroidResponse>> objects = new LinkedHashMap<>();
        objects.put("2026-06-14", List.of(asteroid));
        feed.setNearEarthObjects(objects);
        return feed;
    }
}
