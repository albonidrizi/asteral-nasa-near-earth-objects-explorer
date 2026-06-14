package com.nasa.asteral.api;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.nasa.asteral.exception.NasaApiUnavailableException;
import com.nasa.asteral.service.AsteroidDetailService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AsteroidDetailService asteroidDetailService;

    @Test
    void returnsStructuredErrorWithCorrelationId() throws Exception {
        when(asteroidDetailService.getAsteroidDetailsById("123"))
                .thenThrow(new NasaApiUnavailableException("NASA unavailable", null));

        mockMvc.perform(get("/api/public/asteroids/123").header("X-Correlation-ID", "api-test"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(header().string("X-Correlation-ID", "api-test"))
                .andExpect(jsonPath("$.code").value("NASA_API_UNAVAILABLE"))
                .andExpect(jsonPath("$.correlationId").value("api-test"))
                .andExpect(jsonPath("$.path").value("/api/public/asteroids/123"));
    }
}
