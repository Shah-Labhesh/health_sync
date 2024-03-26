package com.fyp.health_sync;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@OpenAPIDefinition
@SecurityScheme(
		name = "BearerAuth",
		type = SecuritySchemeType.HTTP,
		scheme = "bearer",
		bearerFormat = "JWT"
)

public class HealthSyncApplication {

	public static void main(String[] args) {
		SpringApplication.run(HealthSyncApplication.class, args);
	}


	@Bean
	public OpenAPI customOpenAPI() {
		Info info = new Info()
				.title("Health Sync")
				.version("1.0.0")
				.description("Health Sync API");

		return new OpenAPI().info(info);
	}



}
