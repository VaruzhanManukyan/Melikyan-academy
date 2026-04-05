package com.melikyan.academy.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank(message = "{user.changePassword.currentPassword.notBlank}")
        String currentPassword,

        @NotBlank(message = "{user.changePassword.newPassword.notBlank}")
        @Size(min = 8, max = 255, message = "{user.changePassword.newPassword.size}")
        String newPassword,

        @NotBlank(message = "{user.changePassword.confirmNewPassword.notBlank}")
        String confirmNewPassword
) {
}
