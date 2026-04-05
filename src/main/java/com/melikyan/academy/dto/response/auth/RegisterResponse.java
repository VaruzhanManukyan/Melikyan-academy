package com.melikyan.academy.dto.response.auth;

import com.melikyan.academy.dto.response.user.UserProfileResponse;

public record RegisterResponse(
        String message,
        UserProfileResponse user
) {
}
