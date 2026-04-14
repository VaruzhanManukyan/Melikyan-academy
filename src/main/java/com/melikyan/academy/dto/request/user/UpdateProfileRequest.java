package com.melikyan.academy.dto.request.user;

import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

public record UpdateProfileRequest(
        @Size(max = 50, message = "user.updateProfile.firstName.size")
        String firstName,

        @Size(max = 50, message = "user.updateProfile.lastName.size")
        String lastName,

        String bio,

        MultipartFile avatar
) {
}
