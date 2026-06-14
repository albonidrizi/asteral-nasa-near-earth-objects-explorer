package com.nasa.asteral.configuration;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfiguration {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}
