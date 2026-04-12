package com.melikyan.academy.service;

import com.melikyan.academy.entity.User;
import com.melikyan.academy.mapper.UserMapper;
import com.melikyan.academy.repository.UserRepository;
import com.melikyan.academy.exception.NotFoundException;
import com.melikyan.academy.storage.LocalStorageService;
import org.springframework.security.core.Authentication;
import com.melikyan.academy.exception.BadRequestException;
import com.melikyan.academy.repository.RememberMeTokenRepository;
import com.melikyan.academy.dto.request.user.UpdateProfileRequest;
import com.melikyan.academy.dto.response.user.UserProfileResponse;
import com.melikyan.academy.dto.request.user.ChangePasswordRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final LocalStorageService localStorageService;
    private final RememberMeTokenRepository rememberMeTokenRepository;

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private static final long MAX_AVATAR_SIZE = 5 * 1024 * 1024;

    private User getCurrentUser() {
        Authentication authorization = SecurityContextHolder.getContext().getAuthentication();

        if (authorization == null || !authorization.isAuthenticated() || authorization instanceof AnonymousAuthenticationToken) {
            throw new BadRequestException("Authenticated user not found");
        }

        String email = authorization.getName();

        return userRepository.findByEmail(email).orElseThrow(() -> new NotFoundException("User not found"));
    }

    private void validateAvatar(UpdateProfileRequest request) {
        String contentType = request.avatar().getContentType();

        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BadRequestException("Only JPG, PNG and WEBP images are allowed");
        }

        if (request.avatar().getSize() > MAX_AVATAR_SIZE) {
            throw new BadRequestException("Avatar size must be less than 5 MB");
        }
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUserProfile() {
        User user = getCurrentUser();
        return userMapper.toProfileResponse(user);
    }

    public UserProfileResponse updateCurrentUserProfile(UpdateProfileRequest request) {
        User user = getCurrentUser();
        String oldAvatarPath = user.getAvatarUrl();
        String newAvatarPath = null;

        if (request.firstName() != null) {
            String firstName = request.firstName().trim();
            if (firstName.isEmpty()) {
                throw new BadRequestException("First name cannot be blank");
            }
            user.setFirstName(firstName);
        }

        if (request.lastName() != null) {
            String lastName = request.lastName().trim();
            if (lastName.isEmpty()) {
                throw new BadRequestException("Last name cannot be blank");
            }
            user.setLastName(lastName);
        }

        if (request.bio() != null) {
            user.setBio(request.bio().trim());
        }

        if (request.avatar() != null && !request.avatar().isEmpty()) {
            validateAvatar(request);
            newAvatarPath = localStorageService.saveAvatar(request.avatar(), user.getId());
            user.setAvatarUrl(newAvatarPath);
        }

        try {
            User savedUser = userRepository.save(user);

            if (newAvatarPath != null && oldAvatarPath != null && !oldAvatarPath.isBlank()) {
                localStorageService.delete(oldAvatarPath);
            }

            return userMapper.toProfileResponse(savedUser);
        } catch (RuntimeException exception) {
            if (newAvatarPath != null) {
                localStorageService.delete(newAvatarPath);
            }
            throw exception;
        }
    }

    public void changePassword(ChangePasswordRequest request) {
        User user = getCurrentUser();

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new BadRequestException("New password must be different from current password");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        rememberMeTokenRepository.deleteByUserId(user.getId());
    }

    public void deleteCurrentUser() {
        User user = getCurrentUser();

        rememberMeTokenRepository.deleteByUserId(user.getId());
        userRepository.deleteById(user.getId());
    }
}
