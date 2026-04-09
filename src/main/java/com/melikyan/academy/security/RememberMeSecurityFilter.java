package com.melikyan.academy.security;

import jakarta.servlet.FilterChain;
import com.melikyan.academy.entity.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.*;
import com.melikyan.academy.entity.RememberMeToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;


@Component
@RequiredArgsConstructor
public class RememberMeSecurityFilter extends OncePerRequestFilter {
    private final AppUserDetailsService appUserDetailsService;
    private final RememberMeCookieService rememberMeCookieService;
    private final SecurityContextRepository securityContextRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        Authentication current = SecurityContextHolder.getContext().getAuthentication();

        if (current == null || !current.isAuthenticated()) {
            Optional<RememberMeToken> optionalToken = rememberMeCookieService.resolveValidToken(request);

            if (optionalToken.isPresent()) {
                RememberMeToken rememberMeToken = optionalToken.get();
                User user = rememberMeToken.getUser();

                UserDetails userDetails = appUserDetailsService.loadUserByUsername(user.getEmail());

                UsernamePasswordAuthenticationToken authentication =
                        UsernamePasswordAuthenticationToken.authenticated(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                SecurityContext context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(authentication);
                SecurityContextHolder.setContext(context);
                securityContextRepository.saveContext(context, request, response);

                rememberMeCookieService.rotate(rememberMeToken, response);
            }
        }
        filterChain.doFilter(request, response);
    }
}
