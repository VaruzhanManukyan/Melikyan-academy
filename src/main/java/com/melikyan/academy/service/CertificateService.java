package com.melikyan.academy.service;

import com.melikyan.academy.entity.User;
import org.springframework.http.HttpStatus;
import com.melikyan.academy.entity.Certificate;
import com.melikyan.academy.entity.ContentItem;
import com.melikyan.academy.entity.UserProcess;
import com.melikyan.academy.mapper.CertificateMapper;
import com.melikyan.academy.repository.UserRepository;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.Authentication;
import com.melikyan.academy.entity.enums.RegistrationStatus;
import com.melikyan.academy.repository.CertificateRepository;
import com.melikyan.academy.repository.ContentItemRepository;
import com.melikyan.academy.repository.UserProcessRepository;
import com.melikyan.academy.storage.CertificateStorageService;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.dao.DataIntegrityViolationException;
import com.melikyan.academy.repository.ProductRegistrationRepository;
import com.melikyan.academy.dto.response.certificate.CertificateResponse;
import com.melikyan.academy.dto.request.certificate.IssueCertificateRequest;
import com.melikyan.academy.dto.request.certificate.UpdateCertificateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Locale;
import java.time.OffsetDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class CertificateService {
    private final UserRepository userRepository;
    private final CertificateMapper certificateMapper;
    private final CertificateRepository certificateRepository;
    private final ContentItemRepository contentItemRepository;
    private final UserProcessRepository userProcessRepository;
    private final CertificateStorageService certificateStorageService;
    private final ProductRegistrationRepository productRegistrationRepository;

    private User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found with id: " + id
                ));
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found with email: " + email
                ));
    }

    private User getCurrentUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authenticated user not found"
            );
        }

        return getUserByEmail(authentication.getName());
    }

    private ContentItem getContentItemById(UUID id) {
        return contentItemRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Content item not found with id: " + id
                ));
    }

    private Certificate getCertificateEntityById(UUID id) {
        return certificateRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Certificate not found with id: " + id
                ));
    }

    private String normalizeCertificateCode(String certificateCode) {
        if (certificateCode == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Certificate code must not be null"
            );
        }

        String normalized = certificateCode.trim();

        if (normalized.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Certificate code must not be blank"
            );
        }

        return normalized;
    }

    private String generateCertificateCode() {
        for (int i = 0; i < 10; i++) {
            String code = "CERT-" + UUID.randomUUID()
                    .toString()
                    .replace("-", "")
                    .substring(0, 16)
                    .toUpperCase(Locale.ROOT);

            if (!certificateRepository.existsByCertificateCodeIgnoreCase(code)) {
                return code;
            }
        }

        throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Could not generate unique certificate code"
        );
    }

    private void validateUserHasActiveAccessToContentItem(User user, ContentItem contentItem) {
        boolean hasActiveAccess = productRegistrationRepository.existsByUserIdAndContentItemIdAndStatus(
                user.getId(),
                contentItem.getId(),
                RegistrationStatus.ACTIVE
        );

        if (!hasActiveAccess) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "User does not have active access to this content item"
            );
        }
    }

    private void validateUserCompletedContentItem(User user, ContentItem contentItem) {
        Integer totalStepsValue = contentItem.getTotalSteps();
        int totalSteps = totalStepsValue == null ? 0 : totalStepsValue;

        if (totalSteps == 0) {
            return;
        }

        UserProcess userProcess = userProcessRepository
                .findByUserIdAndContentItemId(user.getId(), contentItem.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "User progress was not found for this content item"
                ));

        Integer currentStepValue = userProcess.getCurrentStep();
        int currentStep = currentStepValue == null ? 0 : currentStepValue;

        if (currentStep < totalSteps) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "User has not completed this content item"
            );
        }
    }

    private void validateCanIssueCertificate(User user, ContentItem contentItem) {
        validateUserHasActiveAccessToContentItem(user, contentItem);
        validateUserCompletedContentItem(user, contentItem);

        if (certificateRepository.existsByUserIdAndContentItemId(user.getId(), contentItem.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Certificate already exists for this user and content item"
            );
        }
    }

    public CertificateResponse issue(
            IssueCertificateRequest request,
            MultipartFile certificateFile,
            Authentication authentication
    ) {
        User user = getUserById(request.userId());
        User issuedBy = getCurrentUser(authentication);
        ContentItem contentItem = getContentItemById(request.contentItemId());

        validateCanIssueCertificate(user, contentItem);

        Certificate certificate = new Certificate();
        certificate.setCertificateCode(generateCertificateCode());
        certificate.setIssueDate(OffsetDateTime.now());
        certificate.setExpiryDate(request.expiryDate());
        certificate.setMetadata(request.metadata() == null ? Map.of() : request.metadata());
        certificate.setPdfUrl(null);
        certificate.setUser(user);
        certificate.setContentItem(contentItem);
        certificate.setIssuedBy(issuedBy);

        String newPdfUrl = null;

        try {
            Certificate savedCertificate = certificateRepository.saveAndFlush(certificate);

            newPdfUrl = certificateStorageService.saveCertificatePdf(
                    certificateFile,
                    savedCertificate.getId()
            );

            savedCertificate.setPdfUrl(newPdfUrl);
            savedCertificate = certificateRepository.saveAndFlush(savedCertificate);

            return certificateMapper.toResponse(savedCertificate);
        } catch (DataIntegrityViolationException exception) {
            if (newPdfUrl != null) {
                certificateStorageService.delete(newPdfUrl);
            }

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Certificate already exists or certificate code is duplicated",
                    exception
            );
        } catch (RuntimeException exception) {
            if (newPdfUrl != null) {
                certificateStorageService.delete(newPdfUrl);
            }

            throw exception;
        }
    }

    @Transactional(readOnly = true)
    public CertificateResponse getById(UUID id) {
        Certificate certificate = getCertificateEntityById(id);
        return certificateMapper.toResponse(certificate);
    }

    @Transactional(readOnly = true)
    public CertificateResponse verifyByCode(String certificateCode) {
        String normalizedCode = normalizeCertificateCode(certificateCode);

        Certificate certificate = certificateRepository
                .findByCertificateCodeIgnoreCase(normalizedCode)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Certificate not found with code: " + normalizedCode
                ));

        return certificateMapper.toResponse(certificate);
    }

    @Transactional(readOnly = true)
    public List<CertificateResponse> getMyCertificates(Authentication authentication) {
        User user = getCurrentUser(authentication);

        return certificateMapper.toResponseList(
                certificateRepository.findAllByUserId(user.getId())
        );
    }

    @Transactional(readOnly = true)
    public List<CertificateResponse> getAllByUser(UUID userId) {
        getUserById(userId);

        return certificateMapper.toResponseList(
                certificateRepository.findAllByUserId(userId)
        );
    }

    @Transactional(readOnly = true)
    public List<CertificateResponse> getAllByContentItem(UUID contentItemId) {
        getContentItemById(contentItemId);

        return certificateMapper.toResponseList(
                certificateRepository.findAllByContentItemId(contentItemId)
        );
    }

    public CertificateResponse update(
            UUID id,
            UpdateCertificateRequest request,
            MultipartFile certificateFile
    ) {
        Certificate certificate = getCertificateEntityById(id);

        boolean hasRequest = request != null;
        boolean hasCertificateFile = certificateFile != null && !certificateFile.isEmpty();

        if (!hasRequest && !hasCertificateFile) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Nothing to update"
            );
        }

        if (hasRequest && request.expiryDate() != null) {
            if (!request.expiryDate().isAfter(certificate.getIssueDate())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Expiry date must be after issue date"
                );
            }

            certificate.setExpiryDate(request.expiryDate());
        }

        if (hasRequest && request.metadata() != null) {
            certificate.setMetadata(request.metadata());
        }

        String oldPdfUrl = certificate.getPdfUrl();
        String newPdfUrl = null;

        try {
            if (hasCertificateFile) {
                newPdfUrl = certificateStorageService.saveCertificatePdf(
                        certificateFile,
                        certificate.getId()
                );

                certificate.setPdfUrl(newPdfUrl);
            }

            Certificate savedCertificate = certificateRepository.saveAndFlush(certificate);

            if (newPdfUrl != null && oldPdfUrl != null && !oldPdfUrl.isBlank()) {
                certificateStorageService.delete(oldPdfUrl);
            }

            return certificateMapper.toResponse(savedCertificate);
        } catch (DataIntegrityViolationException exception) {
            if (newPdfUrl != null) {
                certificateStorageService.delete(newPdfUrl);
            }

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Unable to update certificate",
                    exception
            );
        } catch (RuntimeException exception) {
            if (newPdfUrl != null) {
                certificateStorageService.delete(newPdfUrl);
            }

            throw exception;
        }
    }

    public CertificateResponse update(UUID id, UpdateCertificateRequest request) {
        return update(id, request, null);
    }

    public void delete(UUID id) {
        Certificate certificate = getCertificateEntityById(id);

        String pdfUrl = certificate.getPdfUrl();

        certificateRepository.delete(certificate);
        certificateRepository.flush();

        if (pdfUrl != null && !pdfUrl.isBlank()) {
            certificateStorageService.delete(pdfUrl);
        }
    }
}