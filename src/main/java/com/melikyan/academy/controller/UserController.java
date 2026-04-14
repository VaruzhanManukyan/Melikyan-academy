package com.melikyan.academy.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import com.melikyan.academy.service.UserService;
import org.springframework.web.bind.annotation.*;
import com.melikyan.academy.dto.request.user.UpdateProfileRequest;
import com.melikyan.academy.dto.response.user.UserProfileResponse;
import com.melikyan.academy.dto.request.user.ChangePasswordRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getCurrentUser(){
        return ResponseEntity.ok(userService.getCurrentUserProfile());
    }

    @PatchMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserProfileResponse> updateCurrentUserProfile(
            @ModelAttribute UpdateProfileRequest request
    ) {
        return ResponseEntity.ok(userService.updateCurrentUserProfile(request));
    }

    @PatchMapping("/me/password")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        userService.changePassword(request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteCurrentUser() {
        userService.deleteCurrentUser();
        return ResponseEntity.noContent().build();
    }
}
