package com.melikyan.academy.dto.response.auth;

public record CsrfTokenResponse(
        String headerName,
        String parameterName,
        String token
) {
}
