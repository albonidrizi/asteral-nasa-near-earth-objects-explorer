package com.nasa.asteral.configuration;

import java.util.Arrays;
import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.util.StringUtils;

public class ProductionSecretsEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final List<String> REQUIRED_SECRETS = List.of("NASA_API_KEY", "DB_USERNAME", "DB_PASSWORD");

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (!Arrays.asList(environment.getActiveProfiles()).contains("prod")) {
            return;
        }

        List<String> missing = REQUIRED_SECRETS.stream()
                .filter(name -> !StringUtils.hasText(environment.getProperty(name)))
                .toList();
        if (!missing.isEmpty()) {
            throw new IllegalStateException("Missing required production secrets: " + String.join(", ", missing));
        }
    }
}
