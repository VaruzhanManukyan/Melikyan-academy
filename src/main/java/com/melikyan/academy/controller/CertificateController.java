package com.melikyan.academy.controller;

import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.melikyan.academy.service.CertificateService;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.Authentication;
import com.melikyan.academy.dto.response.certificate.CertificateResponse;
import com.melikyan.academy.dto.request.certificate.IssueCertificateRequest;
import com.melikyan.academy.dto.request.certificate.UpdateCertificateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/certificates")
public class CertificateController {
    private final CertificateService certificateService;

    @PostMapping(value = "/issue", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<CertificateResponse> issue(
            @Valid @RequestPart("request") IssueCertificateRequest request,
            @RequestPart("certificate") MultipartFile certificate,
            Authentication authentication
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(certificateService.issue(request, certificate, authentication));
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CertificateResponse>> getMyCertificates(
            Authentication authentication
    ) {
        return ResponseEntity.ok(certificateService.getMyCertificates(authentication));
    }

    @GetMapping("/verify/{certificateCode}")
    public ResponseEntity<CertificateResponse> verifyByCode(
            @PathVariable String certificateCode
    ) {
        return ResponseEntity.ok(certificateService.verifyByCode(certificateCode));
    }

    @GetMapping("/{id:[0-9a-fA-F\\-]{36}}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<CertificateResponse> getById(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(certificateService.getById(id));
    }

    @GetMapping("/users/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<List<CertificateResponse>> getAllByUser(
            @PathVariable UUID userId
    ) {
        return ResponseEntity.ok(certificateService.getAllByUser(userId));
    }

    @GetMapping("/content-items/{contentItemId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<List<CertificateResponse>> getAllByContentItem(
            @PathVariable UUID contentItemId
    ) {
        return ResponseEntity.ok(certificateService.getAllByContentItem(contentItemId));
    }

    @PatchMapping(value = "/{id:[0-9a-fA-F\\-]{36}}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<CertificateResponse> update(
            @PathVariable UUID id,
            @RequestPart(value = "request", required = false) UpdateCertificateRequest request,
            @RequestPart(value = "certificate", required = false) MultipartFile certificate
    ) {
        return ResponseEntity.ok(certificateService.update(id, request, certificate));
    }

    @DeleteMapping("/{id:[0-9a-fA-F\\-]{36}}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id
    ) {
        certificateService.delete(id);
        return ResponseEntity.noContent().build();
    }
}