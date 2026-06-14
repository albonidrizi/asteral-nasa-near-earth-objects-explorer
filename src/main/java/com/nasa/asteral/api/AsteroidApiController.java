package com.nasa.asteral.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nasa.asteral.model.response.dto.AsteroidDetailResponse;
import com.nasa.asteral.service.AsteroidDetailService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/public/asteroids")
@RequiredArgsConstructor
public class AsteroidApiController {

    private final AsteroidDetailService asteroidDetailService;

    @GetMapping("/{referenceId}")
    AsteroidDetailResponse getAsteroid(@PathVariable String referenceId) {
        return asteroidDetailService.getAsteroidDetailsById(referenceId);
    }
}
