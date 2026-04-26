package com.melikyan.academy.dto.request.professor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;

public record CreateProfessorUserRequest(
        @NotBlank(message = "{user.firstName.notBlank}")
        String firstName,

        @NotBlank(message = "{user.lastName.notBlank}")
        String lastName,

        @Email(message = "{user.email.invalid}")
        @NotBlank(message = "{user.email.notBlank}")
        String email,

        @Size(min = 8, message = "{user.password.size}")
        @NotBlank(message = "{user.password.notBlank}")
        String password,

        @NotBlank(message = "{user.confirmPassword.notBlank}")
        String confirmPassword
) {
}