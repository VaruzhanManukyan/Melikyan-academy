package com.melikyan.academy.service;

import com.melikyan.academy.entity.User;
import com.melikyan.academy.entity.enums.Role;
import com.melikyan.academy.mapper.UserMapper;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import com.melikyan.academy.repository.UserRepository;
import com.melikyan.academy.storage.LocalStorageService;
import com.melikyan.academy.exception.BadRequestException;
import org.springframework.security.core.context.SecurityContext;
import com.melikyan.academy.repository.RememberMeTokenRepository;
import com.melikyan.academy.dto.request.user.UpdateProfileRequest;
import com.melikyan.academy.dto.response.user.UserProfileResponse;
import com.melikyan.academy.dto.request.user.ChangePasswordRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock private UserMapper userMapper;
    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private LocalStorageService localStorageService;
    @Mock private RememberMeTokenRepository rememberMeTokenRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setEmail("test@test.com");
        testUser.setPassword("hashed_password");
        testUser.setFirstName("Test");
        testUser.setLastName("Test");
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private void mockAuthenticatedUser() {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "test@test.com",
                null,
                List.of()
        );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        when(userRepository.findByEmail("test@test.com"))
                .thenReturn(Optional.of(testUser));
    }

    @Test
    void getCurrentUserProfile_success() {
        mockAuthenticatedUser();

        UserProfileResponse expectedResponse = new UserProfileResponse(
                testUser.getId(), "test@test.com",
                "Test",
                "Test",
                null,
                null,
                Role.STUDENT,
                null,
                null
        );

        when(userMapper.toProfileResponse(testUser)).thenReturn(expectedResponse);

        UserProfileResponse result = userService.getCurrentUserProfile();

        assertEquals(expectedResponse, result);
    }

    @Test
    void changePassword_whenCurrentPasswordWrong_shouldThrow() {
        mockAuthenticatedUser();

        ChangePasswordRequest request = new ChangePasswordRequest(
                "wrong_password",
                "new_password",
                "new_password"
        );

        when(passwordEncoder.matches("wrong_password", "hashed_password"))
                .thenReturn(false);

        assertThrows(BadRequestException.class,
                () -> userService.changePassword(request));

        verify(userRepository, never()).save(any());
    }

    @Test
    void changePassword_whenNewPasswordSameAsOld_shouldThrow() {
        mockAuthenticatedUser();

        ChangePasswordRequest request = new ChangePasswordRequest(
                "correct_password",
                "correct_password",
                "correct_password"
        );

        when(passwordEncoder.matches("correct_password", "hashed_password"))
                .thenReturn(true);

        assertThrows(BadRequestException.class,
                () -> userService.changePassword(request));

        verify(userRepository, never()).save(any());
    }

    @Test
    void changePassword_success() {
        mockAuthenticatedUser();

        ChangePasswordRequest request = new ChangePasswordRequest(
                "correct_password",
                "new_password",
                "new_password"
        );

        when(passwordEncoder.matches("correct_password", "hashed_password"))
                .thenReturn(true);

        when(passwordEncoder.matches("new_password", "hashed_password"))
                .thenReturn(false);

        when(passwordEncoder.encode("new_password"))
                .thenReturn("new_hashed_password");

        userService.changePassword(request);

        assertEquals("new_hashed_password", testUser.getPassword());

        verify(userRepository, times(1)).save(testUser);

        verify(rememberMeTokenRepository, times(1))
                .deleteByUserId(testUser.getId());
    }

    @Test
    void updateProfile_whenFirstNameBlank_shouldThrow() {
        mockAuthenticatedUser();

        UpdateProfileRequest request = new UpdateProfileRequest(
                "   ",
                null,
                null,
                null
        );

        assertThrows(BadRequestException.class,
                () -> userService.updateCurrentUserProfile(request));
    }

    @Test
    void updateProfile_whenAvatarInvalidContentType_shouldThrow() {
        mockAuthenticatedUser();

        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getContentType()).thenReturn("application/pdf");

        UpdateProfileRequest request = new UpdateProfileRequest(
                null,
                null,
                null,
                mockFile
        );

        assertThrows(BadRequestException.class,
                () -> userService.updateCurrentUserProfile(request));
    }

    @Test
    void updateProfile_whenAvatarTooLarge_shouldThrow() {
        mockAuthenticatedUser();

        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getContentType()).thenReturn("image/jpeg");
        when(mockFile.getSize()).thenReturn(6 * 1024 * 1024L);

        UpdateProfileRequest request = new UpdateProfileRequest(
                null,
                null,
                null,
                mockFile
        );

        assertThrows(BadRequestException.class,
                () -> userService.updateCurrentUserProfile(request));
    }

    @Test
    void updateProfile_withNewAvatar_shouldDeleteOldAvatar() {
        mockAuthenticatedUser();

        testUser.setAvatarUrl("/uploads/avatars/old_avatar.jpg");

        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getContentType()).thenReturn("image/jpeg");
        when(mockFile.getSize()).thenReturn(1024L);

        UpdateProfileRequest request = new UpdateProfileRequest(
                null, null, null, mockFile
        );

        when(localStorageService.saveAvatar(mockFile, testUser.getId()))
                .thenReturn("/uploads/avatars/new_avatar.jpg");

        when(userRepository.save(testUser)).thenReturn(testUser);
        when(userMapper.toProfileResponse(testUser)).thenReturn(null);

        userService.updateCurrentUserProfile(request);

        verify(localStorageService, times(1))
                .delete("/uploads/avatars/old_avatar.jpg");
    }

    @Test
    void updateProfile_whenSaveFails_shouldDeleteNewAvatar() {
        mockAuthenticatedUser();

        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getContentType()).thenReturn("image/png");
        when(mockFile.getSize()).thenReturn(1024L);

        UpdateProfileRequest request = new UpdateProfileRequest(
                null,
                null,
                null,
                mockFile
        );

        when(localStorageService.saveAvatar(mockFile, testUser.getId()))
                .thenReturn("/uploads/avatars/new_avatar.png");

        when(userRepository.save(any())).thenThrow(new RuntimeException("DB error"));

        assertThrows(RuntimeException.class,
                () -> userService.updateCurrentUserProfile(request));

        verify(localStorageService, times(1))
                .delete("/uploads/avatars/new_avatar.png");
    }

    @Test
    void deleteCurrentUser_success() {
        mockAuthenticatedUser();

        userService.deleteCurrentUser();

        verify(rememberMeTokenRepository, times(1))
                .deleteByUserId(testUser.getId());

        verify(userRepository, times(1))
                .deleteById(testUser.getId());
    }
}
