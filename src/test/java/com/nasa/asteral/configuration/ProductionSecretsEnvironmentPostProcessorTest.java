package com.nasa.asteral.configuration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

class ProductionSecretsEnvironmentPostProcessorTest {

    private final ProductionSecretsEnvironmentPostProcessor validator =
            new ProductionSecretsEnvironmentPostProcessor();

    @Test
    void productionProfileFailsWhenRequiredSecretsAreMissing() {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("prod");

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> validator.postProcessEnvironment(environment, null));

        assertTrue(exception.getMessage().contains("NASA_API_KEY"));
        assertTrue(exception.getMessage().contains("DB_PASSWORD"));
    }

    @Test
    void demoProfileDoesNotRequireProductionSecrets() {
        assertDoesNotThrow(() -> validator.postProcessEnvironment(new MockEnvironment(), null));
    }
}
