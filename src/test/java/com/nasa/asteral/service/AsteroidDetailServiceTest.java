package com.nasa.asteral.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nasa.asteral.integration.nasa.NasaClient;
import com.nasa.asteral.model.response.dto.AsteroidDetailResponse;
import com.nasa.asteral.model.response.nasa.api.AsteroidResponse;
import com.nasa.asteral.model.response.nasa.api.CloseApprochResponse;
import com.nasa.asteral.model.response.nasa.api.EstimatedDiameterDetailResponse;
import com.nasa.asteral.model.response.nasa.api.EstimatedDiameterResponse;

@ExtendWith(MockitoExtension.class)
class AsteroidDetailServiceTest {

    @Mock
    private NasaClient nasaClient;

    private AsteroidDetailService service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-06-14T00:00:00Z"), ZoneOffset.UTC);
        service = new AsteroidDetailService(nasaClient, clock);
    }

    @Test
    void mapsDetailsAndSelectsNearestFutureApproach() {
        AsteroidResponse asteroid = asteroid();
        asteroid.setCloseApprochData(List.of(approach("2026-06-20"), approach("2026-06-16"), approach("2026-01-01")));
        when(nasaClient.fetchAsteroid("12345")).thenReturn(asteroid);

        AsteroidDetailResponse result = service.getAsteroidDetailsById("12345");

        assertEquals("12345", result.getReferenceId());
        assertEquals("Test Asteroid", result.getName());
        assertEquals(1.0, result.getEstimatedDiameterMinKm());
        assertEquals(2.0, result.getEstimatedDiameterMaxKm());
        assertEquals(true, result.isPotentiallyHazardous());
        assertEquals("2026-06-16", result.getLastCloseApproachingDate());
    }

    private AsteroidResponse asteroid() {
        AsteroidResponse asteroid = new AsteroidResponse();
        asteroid.setReferenceId("12345");
        asteroid.setName("Test Asteroid");
        asteroid.setPotentiallyHazardous(true);
        EstimatedDiameterDetailResponse detail = new EstimatedDiameterDetailResponse();
        detail.setEstimatedDiameterMin(1.0);
        detail.setEstimatedDiameterMax(2.0);
        EstimatedDiameterResponse diameter = new EstimatedDiameterResponse();
        diameter.setKilometers(detail);
        asteroid.setEstimatedDiameter(diameter);
        return asteroid;
    }

    private CloseApprochResponse approach(String date) {
        CloseApprochResponse approach = new CloseApprochResponse();
        approach.setCloseApprochDate(date);
        approach.setOrbitingBody("Earth");
        return approach;
    }
}
