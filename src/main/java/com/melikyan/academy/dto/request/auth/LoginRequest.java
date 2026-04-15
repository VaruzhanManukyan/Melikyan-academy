package com.melikyan.academy.dto.request.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "{auth.email.notBlank}")
        @Email(message = "[auth.email.invalid}")
        @Size(max = 255, message = "{auth.email.size}")
        String email,

        @NotBlank(message = "{auth.password.notBlank}")
        @Size(max = 255, message = "{auth.password.size}")
        String password,

        Boolean rememberMe
) {
}
