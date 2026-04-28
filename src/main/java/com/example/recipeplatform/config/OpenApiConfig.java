package com.example.recipeplatform.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Recipe Platform API",
                version = "1.0",
                description = "REST API for the recipe sharing platform used in laboratory work 2.",
                contact = @Contact(name = "Recipe Platform Team"),
                license = @License(name = "MIT")
        ),
        servers = @Server(url = "http://localhost:8080", description = "Local development server")
)
public class OpenApiConfig {
}
