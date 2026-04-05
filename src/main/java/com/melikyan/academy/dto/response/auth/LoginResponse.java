package com.melikyan.academy.dto.response.auth;

import com.melikyan.academy.dto.response.user.UserProfileResponse;

public record LoginResponse(
        boolean authenticated,
        String message,
        UserProfileResponse user
) {
}
