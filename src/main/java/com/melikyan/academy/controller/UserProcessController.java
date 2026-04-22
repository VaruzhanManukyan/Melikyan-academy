package com.melikyan.academy.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.melikyan.academy.service.UserProcessService;
import org.springframework.security.core.Authentication;
import com.melikyan.academy.dto.response.userProcess.UserProcessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class UserProcessController {
    private final UserProcessService userProcessService;

    @GetMapping("/me/progress")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<UserProcessResponse>> getMyProgress(Authentication authentication) {
        return ResponseEntity.ok(userProcessService.getMyProgress(authentication.getName()));
    }

    @GetMapping("/me/progress/content-items/{contentItemId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserProcessResponse> getMyProgressByContentItemId(
            @PathVariable UUID contentItemId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                userProcessService.getMyProgressByContentItemId(contentItemId, authentication.getName())
        );
    }

    @GetMapping("/users/{userId}/progress")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<List<UserProcessResponse>> getUserProgress(@PathVariable UUID userId) {
        return ResponseEntity.ok(userProcessService.getUserProgress(userId));
    }

    @GetMapping("/users/{userId}/progress/content-items/{contentItemId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<UserProcessResponse> getUserProgressByContentItemId(
            @PathVariable UUID userId,
            @PathVariable UUID contentItemId
    ) {
        return ResponseEntity.ok(
                userProcessService.getUserProgressByContentItemId(userId, contentItemId)
        );
    }
}