package com.nasa.asteral.integration.nasa;

import java.time.LocalDate;

import com.nasa.asteral.model.response.nasa.api.AsteroidFeedResponse;
import com.nasa.asteral.model.response.nasa.api.AsteroidResponse;

public interface NasaClient {

    AsteroidFeedResponse fetchFeed(LocalDate startDate, LocalDate endDate);

    AsteroidResponse fetchAsteroid(String referenceId);
}
