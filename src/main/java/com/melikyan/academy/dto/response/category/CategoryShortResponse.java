package com.melikyan.academy.dto.response.category;

import java.util.UUID;

public record CategoryShortResponse(
        UUID id,
        String title,
        String description
) {
}
