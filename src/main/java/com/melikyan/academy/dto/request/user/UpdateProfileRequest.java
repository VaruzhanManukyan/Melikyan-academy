package com.melikyan.academy.dto.request.user;

import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Size(max = 50, message = "{user.updateProfile.firstName.size}")
        String firstName,

        @Size(max = 50, message = "{user.updateProfile.lastName.size}")
        String lastName,

        String bio
) {
}
