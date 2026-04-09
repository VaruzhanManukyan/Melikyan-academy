package com.melikyan.academy.security;

import jakarta.servlet.http.Cookie;
import com.melikyan.academy.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.melikyan.academy.entity.RememberMeToken;
import com.melikyan.academy.repository.RememberMeTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
import java.util.Optional;
import java.time.OffsetDateTime;
import java.security.SecureRandom;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class RememberMeCookieService {
    public static final String COOKIE_NAME = "REMEMBER_ME";

    private final RememberMeTokenHasher tokenHasher;
    private final RememberMeTokenRepository rememberMeTokenRepository;

    @Value("${security.remember-me.validity-seconds:2592000}")
    private long validitySeconds;

    private final SecureRandom secureRandom = new SecureRandom();

    private String randomToken(int bytesLength) {
        byte[] bytes = new byte[bytesLength];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private void clearCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(COOKIE_NAME, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    private void writeCookie(
            HttpServletResponse response,
            String selector,
            String token,
            int maxAge) {
        Cookie cookie = new Cookie(COOKIE_NAME, selector + ":" + token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }

    private Cookie readCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }

        for (Cookie cookie : request.getCookies()) {
            if (COOKIE_NAME.equals(cookie.getName())) {
                return cookie;
            }
        }

        return null;
    }

    public void issue(User user, HttpServletResponse response) {
        rememberMeTokenRepository.deleteByUserId(user.getId());
        String selector = randomToken(24);
        String token = randomToken(34);
        String tokenHash = tokenHasher.hash(token);
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime expiresAt = now.plusSeconds(validitySeconds);

        RememberMeToken rememberMeToken = RememberMeToken.builder()
                .selector(selector)
                .tokenHash(tokenHash)
                .user(user)
                .lastUsedAt(now)
                .expiresAt(expiresAt)
                .build();

        rememberMeTokenRepository.save(rememberMeToken);
        writeCookie(response, selector, token, (int)validitySeconds);
    }

    @Transactional(readOnly = true)
    public Optional<RememberMeToken> resolveValidToken(HttpServletRequest request) {
        Cookie cookie = readCookie(request);
        if (cookie == null || cookie.getValue() == null || !cookie.getValue().contains(":")) {
            return Optional.empty();
        }

        String[] parts = cookie.getValue().split(":", 2);
        if (parts.length != 2) {
            return Optional.empty();
        }

        String selector = parts[0];
        String token = parts[1];

        return rememberMeTokenRepository.findBySelectorWithUser(selector)
                .filter(entity -> entity.getExpiresAt().isAfter(OffsetDateTime.now()))
                .filter(entity -> entity.getTokenHash().equals(tokenHasher.hash(token)));
    }

    public void rotate(RememberMeToken tokenEntity, HttpServletResponse response) {
        String newToken = randomToken(32);
        tokenEntity.setTokenHash(tokenHasher.hash(newToken));
        tokenEntity.setLastUsedAt(OffsetDateTime.now());
        tokenEntity.setExpiresAt(OffsetDateTime.now().plusSeconds(validitySeconds));

        rememberMeTokenRepository.save(tokenEntity);
        writeCookie(response, tokenEntity.getSelector(), newToken, (int) validitySeconds);
    }

    public void revokeCurrent(HttpServletRequest request, HttpServletResponse response) {
        Cookie cookie = readCookie(request);

        if (cookie != null && cookie.getValue() != null && cookie.getValue().contains(":")) {
            String selector = cookie.getValue().split(":", 2)[0];
            rememberMeTokenRepository.deleteBySelector(selector);
        }
        clearCookie(response);
    }

    public void deleteAllByUser(UUID userId) {
        rememberMeTokenRepository.deleteByUserId(userId);
    }

    public void cleanupExpired() {
        rememberMeTokenRepository.deleteAllByExpiresAtBefore(OffsetDateTime.now());
    }
}
