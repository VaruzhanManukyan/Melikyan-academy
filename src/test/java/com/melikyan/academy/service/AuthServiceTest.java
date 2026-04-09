package com.melikyan.academy.service;

import org.mockito.*;
import com.melikyan.academy.entity.User;
import com.melikyan.academy.entity.enums.Role;
import com.melikyan.academy.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.mockito.junit.jupiter.MockitoExtension;
import com.melikyan.academy.repository.UserRepository;
import com.melikyan.academy.exception.ConflictException;
import org.springframework.security.core.Authentication;
import com.melikyan.academy.exception.BadRequestException;
import com.melikyan.academy.dto.request.auth.LoginRequest;
import org.springframework.mock.web.MockHttpServletRequest;
import com.melikyan.academy.dto.response.auth.LoginResponse;
import org.springframework.mock.web.MockHttpServletResponse;
import com.melikyan.academy.security.RememberMeCookieService;
import com.melikyan.academy.dto.request.auth.RegisterRequest;
import com.melikyan.academy.dto.response.auth.RegisterResponse;
import com.melikyan.academy.dto.response.user.UserProfileResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.UUID;
import java.util.Optional;
import java.time.OffsetDateTime;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private UserMapper userMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private RememberMeCookieService rememberMeCookieService;

    @Mock
    private SecurityContextRepository securityContextRepository;

    @Mock
    private SessionAuthenticationStrategy sessionAuthenticationStrategy;

    @InjectMocks
    private AuthService authService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void register_shouldSaveUserAndReturnResponse() {
        RegisterRequest request = new RegisterRequest(
                "TEST@TEST.COM",
                "StrongPass123",
                "StrongPass123",
                "  Test  ",
                "  User  "
        );

        User mappedUser = new User();
        User savedUser = buildUser();

        when(userRepository.existsByEmail("test@test.com")).thenReturn(false);
        when(userMapper.toEntity(request)).thenReturn(mappedUser);
        when(passwordEncoder.encode("StrongPass123")).thenReturn("encoded-password");

        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(savedUser.getId());
            user.setCreatedAt(savedUser.getCreatedAt());
            user.setUpdatedAt(savedUser.getUpdatedAt());
            return user;
        });

        when(userMapper.toRegisterResponse(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            return new RegisterResponse(
                    "Registration successful",
                    new UserProfileResponse(
                            user.getId(),
                            user.getEmail(),
                            user.getFirstName(),
                            user.getLastName(),
                            user.getAvatarUrl(),
                            user.getBio(),
                            user.getRole(),
                            user.getCreatedAt(),
                            user.getUpdatedAt()
                    )
            );
        });

        RegisterResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("Registration successful", response.message());
        assertEquals("test@test.com", response.user().email());
        assertEquals("Test", response.user().firstName());
        assertEquals("User", response.user().lastName());
        assertEquals(Role.STUDENT, response.user().role());
        assertNotNull(response.user().createdAt());
        assertNotNull(response.user().updatedAt());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).saveAndFlush(userCaptor.capture());

        User captured = userCaptor.getValue();
        assertEquals("test@test.com", captured.getEmail());
        assertEquals("Test", captured.getFirstName());
        assertEquals("User", captured.getLastName());
        assertEquals("encoded-password", captured.getPassword());
        assertEquals(Role.STUDENT, captured.getRole());
    }

    @Test
    void register_shouldThrowWhenPasswordsDoNotMatch() {
        RegisterRequest request = new RegisterRequest(
                "test@test.com",
                "StrongPass123",
                "WrongPass123",
                "Test",
                "User"
        );

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> authService.register(request)
        );

        assertEquals("Passwords do not match", exception.getMessage());
        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    void register_shouldThrowWhenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest(
                "test@test.com",
                "StrongPass123",
                "StrongPass123",
                "Test",
                "User"
        );

        when(userRepository.existsByEmail("test@test.com")).thenReturn(true);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> authService.register(request)
        );

        assertEquals("Email is already in use", exception.getMessage());
        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    void login_shouldAuthenticateAndReturnResponse() {
        LoginRequest request = new LoginRequest(
                "test@test.com",
                "StrongPass123",
                false
        );

        Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(
                "test@test.com",
                null,
                List.of()
        );

        User user = buildUser();

        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(authentication);
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(userMapper.toLoginResponse(user)).thenReturn(
                new LoginResponse(
                        true,
                        "Login successful",
                        new UserProfileResponse(
                                user.getId(),
                                user.getEmail(),
                                user.getFirstName(),
                                user.getLastName(),
                                user.getAvatarUrl(),
                                user.getBio(),
                                user.getRole(),
                                user.getCreatedAt(),
                                user.getUpdatedAt()
                        )
                )
        );

        HttpServletRequest httpRequest = new MockHttpServletRequest();
        HttpServletResponse httpResponse = new MockHttpServletResponse();

        LoginResponse response = authService.login(request, httpRequest, httpResponse);

        assertNotNull(response);
        assertTrue(response.authenticated());
        assertEquals("Login successful", response.message());
        assertEquals("test@test.com", response.user().email());

        verify(authenticationManager).authenticate(any(Authentication.class));
        verify(sessionAuthenticationStrategy).onAuthentication(authentication, httpRequest, httpResponse);
        verify(securityContextRepository).saveContext(any(), eq(httpRequest), eq(httpResponse));
        verify(rememberMeCookieService, never()).issue(any(), any());
    }

    @Test
    void login_shouldIssueRememberMeCookieWhenRequested() {
        LoginRequest request = new LoginRequest(
                "test@test.com",
                "StrongPass123",
                true
        );

        Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(
                "test@test.com",
                null,
                List.of()
        );

        User user = buildUser();

        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(authentication);
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(userMapper.toLoginResponse(user)).thenReturn(
                new LoginResponse(
                        true,
                        "Login successful",
                        new UserProfileResponse(
                                user.getId(),
                                user.getEmail(),
                                user.getFirstName(),
                                user.getLastName(),
                                user.getAvatarUrl(),
                                user.getBio(),
                                user.getRole(),
                                user.getCreatedAt(),
                                user.getUpdatedAt()
                        )
                )
        );

        HttpServletRequest httpRequest = new MockHttpServletRequest();
        HttpServletResponse httpResponse = new MockHttpServletResponse();

        authService.login(request, httpRequest, httpResponse);

        verify(rememberMeCookieService).issue(user, httpResponse);
    }

    private User buildUser() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@test.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setRole(Role.STUDENT);
        user.setCreatedAt(OffsetDateTime.now());
        user.setUpdatedAt(OffsetDateTime.now());
        return user;
    }
}
