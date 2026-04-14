package com.melikyan.academy.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.GlobalOperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.method.HandlerMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Configuration
public class OpenApiConfig {

    private static final Pattern HAS_ANY_ROLE =
            Pattern.compile("hasAnyRole\\(([^)]*)\\)");

    private static final Pattern HAS_ROLE =
            Pattern.compile("hasRole\\(([^)]*)\\)");

    private static final Pattern QUOTED_VALUE =
            Pattern.compile("'([^']+)'");

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
                        .license(new License().name("Proprietary")))
                .components(new Components()
                        .addSecuritySchemes(sessionAuthScheme,
                                new SecurityScheme()
                                        .name("JSESSIONID")
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.COOKIE)))
                .addSecurityItem(new SecurityRequirement().addList(sessionAuthScheme));
    }

    @Bean
    public GlobalOperationCustomizer roleOperationCustomizer() {
        return (Operation operation, HandlerMethod handlerMethod) -> {
            PreAuthorize preAuthorize = findPreAuthorize(handlerMethod);

            if (preAuthorize == null) {
                return operation;
            }

            String accessText = toAccessText(preAuthorize.value());
            if (accessText == null || accessText.isBlank()) {
                return operation;
            }

            appendAccessDescription(operation, accessText);
            return operation;
        };
    }

    private PreAuthorize findPreAuthorize(HandlerMethod handlerMethod) {
        PreAuthorize methodAnnotation = handlerMethod.getMethodAnnotation(PreAuthorize.class);
        if (methodAnnotation != null) {
            return methodAnnotation;
        }

        return handlerMethod.getBeanType().getAnnotation(PreAuthorize.class);
    }

    private void appendAccessDescription(Operation operation, String accessText) {
        String currentDescription = operation.getDescription();
        String accessLine = "<br/><br/><b>Access:</b> " + accessText;

        if (currentDescription == null || currentDescription.isBlank()) {
            operation.setDescription("<b>Access:</b> " + accessText);
        } else if (!currentDescription.contains("<b>Access:</b>")) {
            operation.setDescription(currentDescription + accessLine);
        }
    }

    private String toAccessText(String expression) {
        Matcher anyRoleMatcher = HAS_ANY_ROLE.matcher(expression);
        if (anyRoleMatcher.find()) {
            List<String> roles = extractQuotedValues(anyRoleMatcher.group(1));
            if (!roles.isEmpty()) {
                return "Roles: " + String.join(", ", roles);
            }
        }

        Matcher roleMatcher = HAS_ROLE.matcher(expression);
        if (roleMatcher.find()) {
            List<String> roles = extractQuotedValues(roleMatcher.group(1));
            if (!roles.isEmpty()) {
                return "Role: " + roles.get(0);
            }
        }

        return "PreAuthorize: " + expression;
    }

    private List<String> extractQuotedValues(String input) {
        List<String> values = new ArrayList<>();
        Matcher matcher = QUOTED_VALUE.matcher(input);

        while (matcher.find()) {
            values.add(matcher.group(1));
        }

        return values;
    }
}