package com.melikyan.academy.controller;

import org.springframework.http.HttpStatus;
import jakarta.servlet.http.HttpServletRequest;
import com.melikyan.academy.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.core.Authentication;
import com.melikyan.academy.dto.request.auth.LoginRequest;
import com.melikyan.academy.dto.response.auth.LoginResponse;
import com.melikyan.academy.dto.request.auth.RegisterRequest;
import com.melikyan.academy.dto.response.auth.RegisterResponse;
import com.melikyan.academy.dto.response.auth.CsrfTokenResponse;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;

    @GetMapping("/csrf")
    public CsrfTokenResponse getCsrfToken(HttpServletRequest request) {
        CsrfToken token = (CsrfToken) request.getAttribute("_csrf");

        if (token == null) {
            throw new IllegalStateException("CSRF token not found");
        }

        return new CsrfTokenResponse(
                token.getHeaderName(),
                token.getParameterName(),
                token.getToken()
        );
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public LoginResponse login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        return authService.login(request, httpRequest, httpResponse);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/logout")
    public void logout(
            Authentication authentication,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        authService.logout(authentication, request, response);

        SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
        logoutHandler.setClearAuthentication(true);
        logoutHandler.setInvalidateHttpSession(true);
        logoutHandler.logout(request, response, authentication);
    }
}
