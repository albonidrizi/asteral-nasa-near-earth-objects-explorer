package com.nasa.asteral;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AsteralApplication {

	public static void main(String[] args) {
		SpringApplication.run(AsteralApplication.class, args);
	}

}
