package com.melikyan.academy.dto.request.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "{auth.email.notBlank}")
        @Email(message = "{auth.email.invalid}")
        @Size(max = 255, message = "{auth.email.size}")
        String email,

        @NotBlank(message = "{auth.password.notBlank}")
        @Size(min = 8, max = 255, message = "{auth.password.size}")
        String password,

        @NotBlank(message = "{auth.confirmPassword.notBlank}")
        String confirmPassword,

        @NotBlank(message = "{auth.firstName.notBlank}")
        @Size(max = 50, message = "{auth.firstName.size}")
        String firstName,

        @NotBlank(message = "{auth.lastName.notBlank}")
        @Size(max = 50, message = "{auth.lastName.size}")
        String lastName
) {
}
