package com.melikyan.academy.dto.response.user;

import java.util.UUID;

public record UserShortResponse(
        UUID id,
        String firstName,
        String lastName,
        String email
) {
}
