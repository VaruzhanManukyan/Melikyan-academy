package com.melikyan.academy.service;

import com.melikyan.academy.entity.User;
import com.melikyan.academy.entity.enums.Role;
import com.melikyan.academy.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.melikyan.academy.repository.UserRepository;
import com.melikyan.academy.exception.ConflictException;
import org.springframework.security.core.Authentication;
import com.melikyan.academy.dto.request.auth.LoginRequest;
import com.melikyan.academy.exception.BadRequestException;
import com.melikyan.academy.dto.response.auth.LoginResponse;
import com.melikyan.academy.security.RememberMeCookieService;
import com.melikyan.academy.dto.request.auth.RegisterRequest;
import com.melikyan.academy.dto.response.auth.RegisterResponse;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final RememberMeCookieService rememberMeCookieService;
    private final SecurityContextRepository securityContextRepository;
    private final SessionAuthenticationStrategy sessionAuthenticationStrategy;

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeText(String text) {
        return text == null ? null : text.trim();
    }

    private Role defaultUserRole() {
        for (Role role : Role.values()) {
            if (role.name().equalsIgnoreCase("STUDENT")) {
                return role;
            }
        }
        throw new IllegalStateException("Default role 'STUDENT' was not found");
    }

    public RegisterResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());

        if (!request.password().equals(request.confirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }

        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Email is already in use");
        }

        User user = userMapper.toEntity(request);
        user.setEmail(email);
        user.setFirstName(normalizeText(request.firstName()));
        user.setLastName(normalizeText(request.lastName()));
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(defaultUserRole());

        user = userRepository.saveAndFlush(user);

        return userMapper.toRegisterResponse(user);
    }

    public LoginResponse login(
            LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        String email = normalizeEmail(request.email());

        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(email, request.password())
        );

        sessionAuthenticationStrategy.onAuthentication(authentication, httpRequest, httpResponse);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, httpRequest, httpResponse);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (Boolean.TRUE.equals(request.rememberMe())) {
            rememberMeCookieService.issue(user, httpResponse);
        }

        return userMapper.toLoginResponse(user);
    }

    public void logout(
            Authentication authentication,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        rememberMeCookieService.revokeCurrent(request, response);

        if (authentication != null && authentication.isAuthenticated()) {
            userRepository.findByEmail(authentication.getName())
                    .ifPresent(user -> rememberMeCookieService.deleteAllByUser(user.getId()));
        }
    }
}
