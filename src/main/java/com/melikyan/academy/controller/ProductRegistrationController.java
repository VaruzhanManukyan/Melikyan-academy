package com.melikyan.academy.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import com.melikyan.academy.service.ProductRegistrationService;
import com.melikyan.academy.dto.response.productRegistration.ProductRegistrationResponse;
import com.melikyan.academy.dto.request.productRegistration.CreateProductRegistrationRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/product-registrations")
public class ProductRegistrationController {
    private final ProductRegistrationService productRegistrationService;

    @PostMapping("/grant")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<ProductRegistrationResponse> grantAccess(
            @Valid @RequestBody CreateProductRegistrationRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(productRegistrationService.grantAccess(request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<ProductRegistrationResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(productRegistrationService.getById(id));
    }

    @GetMapping("/users/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<List<ProductRegistrationResponse>> getAllByUserId(@PathVariable UUID userId) {
        return ResponseEntity.ok(productRegistrationService.getByUserId(userId));
    }

    @GetMapping("/products/{productId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<List<ProductRegistrationResponse>> getAllByProductId(@PathVariable UUID productId) {
        return ResponseEntity.ok(productRegistrationService.getByProductId(productId));
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ProductRegistrationResponse>> getMyRegistrations(Authentication authentication) {
        return ResponseEntity.ok(productRegistrationService.getMyRegistrations(authentication.getName()));
    }

    @GetMapping("/me/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProductRegistrationResponse> getMyRegistrationById(
            @PathVariable UUID id,
            Authentication authentication
    ) {
        return ResponseEntity.ok(productRegistrationService.getMyRegistrationById(id, authentication.getName()));
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<ProductRegistrationResponse> activate(@PathVariable UUID id) {
        return ResponseEntity.ok(productRegistrationService.activate(id));
    }

    @PatchMapping("/{id}/suspend")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<ProductRegistrationResponse> suspend(@PathVariable UUID id) {
        return ResponseEntity.ok(productRegistrationService.suspend(id));
    }

    @PatchMapping("/{id}/expire")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<ProductRegistrationResponse> expire(@PathVariable UUID id) {
        return ResponseEntity.ok(productRegistrationService.expire(id));
    }
}