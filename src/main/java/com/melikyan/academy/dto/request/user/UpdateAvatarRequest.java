package com.melikyan.academy.dto.request.user;

import org.springframework.web.multipart.MultipartFile;

public record UpdateAvatarRequest(
        MultipartFile avatar
) {
}
