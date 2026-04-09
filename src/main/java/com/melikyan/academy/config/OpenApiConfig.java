package com.melikyan.academy.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        final String sessionAuthScheme = "sessionAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("Melikyan Academy API")
                        .description("REST API documentation for Melikyan Academy")
                        .version("v1")
                        .contact(new Contact()
                                .name("Melikyan Academy")
                                .email("support@melikyan-academy.com"))
                        .license(new License()
                                .name("Proprietary")))
                .addSecurityItem(new SecurityRequirement().addList(sessionAuthScheme))
                .schemaRequirement(sessionAuthScheme,
                        new SecurityScheme()
                                .name("JSESSIONID")
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.COOKIE));
    }
}
