package com.melikyan.academy.service;

import org.mockito.ArgumentCaptor;
import com.melikyan.academy.entity.User;
import com.melikyan.academy.entity.Certificate;
import com.melikyan.academy.entity.ContentItem;
import com.melikyan.academy.entity.UserProcess;
import org.mockito.junit.jupiter.MockitoExtension;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Map;
import java.util.List;
import java.util.UUID;
import java.util.Optional;
import java.time.OffsetDateTime;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class CertificateServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private CertificateMapper certificateMapper;

    @Mock
    private CertificateRepository certificateRepository;

    @Mock
    private ContentItemRepository contentItemRepository;

    @Mock
    private UserProcessRepository userProcessRepository;

    @Mock
    private CertificateStorageService certificateStorageService;

    @Mock
    private ProductRegistrationRepository productRegistrationRepository;

    @InjectMocks
    private CertificateService certificateService;

    private UUID userId;
    private UUID adminId;
    private UUID contentItemId;
    private UUID certificateId;

    private User user;
    private User admin;
    private ContentItem contentItem;
    private UserProcess userProcess;
    private Certificate certificate;
    private MultipartFile certificateFile;
    private Authentication authentication;

    private String adminEmail;
    private String pdfUrl;
    private String oldPdfUrl;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        adminId = UUID.randomUUID();
        contentItemId = UUID.randomUUID();
        certificateId = UUID.randomUUID();

        adminEmail = "admin@test.com";
        pdfUrl = "/uploads/certificates/new-certificate.pdf";
        oldPdfUrl = "/uploads/certificates/old-certificate.pdf";

        authentication = new UsernamePasswordAuthenticationToken(adminEmail, null);

        user = new User();
        user.setId(userId);
        user.setEmail("student@test.com");

        admin = new User();
        admin.setId(adminId);
        admin.setEmail(adminEmail);

        contentItem = new ContentItem();
        contentItem.setId(contentItemId);
        contentItem.setTotalSteps(0);

        userProcess = new UserProcess();
        userProcess.setId(UUID.randomUUID());
        userProcess.setCurrentStep(10);
        userProcess.setUser(user);
        userProcess.setContentItem(contentItem);

        certificate = new Certificate();
        certificate.setId(certificateId);
        certificate.setCertificateCode("CERT-TEST1234567890");
        certificate.setIssueDate(OffsetDateTime.now().minusDays(1));
        certificate.setExpiryDate(null);
        certificate.setMetadata(Map.of());
        certificate.setPdfUrl(oldPdfUrl);
        certificate.setUser(user);
        certificate.setContentItem(contentItem);
        certificate.setIssuedBy(admin);

        certificateFile = mock(MultipartFile.class);
    }

    @Test
    @DisplayName("issue -> creates certificate when content item has no progress steps")
    void issue_ShouldCreateCertificate_WhenTotalStepsIsZero() {
        IssueCertificateRequest request = new IssueCertificateRequest(
                userId,
                contentItemId,
                null,
                Map.of("finalScore", 92)
        );

        CertificateResponse response = mock(CertificateResponse.class);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail(adminEmail)).thenReturn(Optional.of(admin));
        when(contentItemRepository.findById(contentItemId)).thenReturn(Optional.of(contentItem));
        when(productRegistrationRepository.existsByUserIdAndContentItemIdAndStatus(
                userId,
                contentItemId,
                RegistrationStatus.ACTIVE
        )).thenReturn(true);
        when(certificateRepository.existsByUserIdAndContentItemId(userId, contentItemId)).thenReturn(false);
        when(certificateRepository.existsByCertificateCodeIgnoreCase(anyString())).thenReturn(false);
        when(certificateStorageService.saveCertificatePdf(eq(certificateFile), eq(certificateId))).thenReturn(pdfUrl);

        when(certificateRepository.saveAndFlush(any(Certificate.class))).thenAnswer(invocation -> {
            Certificate saved = invocation.getArgument(0);
            if (saved.getId() == null) {
                saved.setId(certificateId);
            }
            return saved;
        });

        when(certificateMapper.toResponse(any(Certificate.class))).thenReturn(response);

        CertificateResponse result = certificateService.issue(request, certificateFile, authentication);

        assertNotNull(result);
        assertEquals(response, result);

        ArgumentCaptor<Certificate> captor = ArgumentCaptor.forClass(Certificate.class);
        verify(certificateRepository, times(2)).saveAndFlush(captor.capture());

        Certificate savedCertificate = captor.getAllValues().get(1);
        assertEquals(user, savedCertificate.getUser());
        assertEquals(admin, savedCertificate.getIssuedBy());
        assertEquals(contentItem, savedCertificate.getContentItem());
        assertEquals(pdfUrl, savedCertificate.getPdfUrl());
        assertEquals(Map.of("finalScore", 92), savedCertificate.getMetadata());
    }

    @Test
    @DisplayName("issue -> creates certificate when progress is completed")
    void issue_ShouldCreateCertificate_WhenProgressIsCompleted() {
        contentItem.setTotalSteps(10);

        IssueCertificateRequest request = new IssueCertificateRequest(
                userId,
                contentItemId,
                null,
                Map.of()
        );

        CertificateResponse response = mock(CertificateResponse.class);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail(adminEmail)).thenReturn(Optional.of(admin));
        when(contentItemRepository.findById(contentItemId)).thenReturn(Optional.of(contentItem));
        when(productRegistrationRepository.existsByUserIdAndContentItemIdAndStatus(
                userId,
                contentItemId,
                RegistrationStatus.ACTIVE
        )).thenReturn(true);
        when(userProcessRepository.findByUserIdAndContentItemId(userId, contentItemId))
                .thenReturn(Optional.of(userProcess));
        when(certificateRepository.existsByUserIdAndContentItemId(userId, contentItemId)).thenReturn(false);
        when(certificateRepository.existsByCertificateCodeIgnoreCase(anyString())).thenReturn(false);
        when(certificateStorageService.saveCertificatePdf(eq(certificateFile), eq(certificateId))).thenReturn(pdfUrl);

        when(certificateRepository.saveAndFlush(any(Certificate.class))).thenAnswer(invocation -> {
            Certificate saved = invocation.getArgument(0);
            if (saved.getId() == null) {
                saved.setId(certificateId);
            }
            return saved;
        });

        when(certificateMapper.toResponse(any(Certificate.class))).thenReturn(response);

        CertificateResponse result = certificateService.issue(request, certificateFile, authentication);

        assertNotNull(result);
        assertEquals(response, result);
    }

    @Test
    @DisplayName("issue -> throws forbidden when user has no active access")
    void issue_ShouldThrowForbidden_WhenUserHasNoActiveAccess() {
        IssueCertificateRequest request = new IssueCertificateRequest(
                userId,
                contentItemId,
                null,
                Map.of()
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail(adminEmail)).thenReturn(Optional.of(admin));
        when(contentItemRepository.findById(contentItemId)).thenReturn(Optional.of(contentItem));
        when(productRegistrationRepository.existsByUserIdAndContentItemIdAndStatus(
                userId,
                contentItemId,
                RegistrationStatus.ACTIVE
        )).thenReturn(false);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> certificateService.issue(request, certificateFile, authentication)
        );

        assertEquals(403, ex.getStatusCode().value());
        assertEquals("User does not have active access to this content item", ex.getReason());

        verify(certificateRepository, never()).saveAndFlush(any());
        verify(certificateStorageService, never()).saveCertificatePdf(any(), any());
    }

    @Test
    @DisplayName("issue -> throws bad request when progress is not found")
    void issue_ShouldThrowBadRequest_WhenProgressIsNotFound() {
        contentItem.setTotalSteps(10);

        IssueCertificateRequest request = new IssueCertificateRequest(
                userId,
                contentItemId,
                null,
                Map.of()
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail(adminEmail)).thenReturn(Optional.of(admin));
        when(contentItemRepository.findById(contentItemId)).thenReturn(Optional.of(contentItem));
        when(productRegistrationRepository.existsByUserIdAndContentItemIdAndStatus(
                userId,
                contentItemId,
                RegistrationStatus.ACTIVE
        )).thenReturn(true);
        when(userProcessRepository.findByUserIdAndContentItemId(userId, contentItemId))
                .thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> certificateService.issue(request, certificateFile, authentication)
        );

        assertEquals(400, ex.getStatusCode().value());
        assertEquals("User progress was not found for this content item", ex.getReason());

        verify(certificateRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("issue -> throws bad request when progress is not completed")
    void issue_ShouldThrowBadRequest_WhenProgressIsNotCompleted() {
        contentItem.setTotalSteps(10);
        userProcess.setCurrentStep(5);

        IssueCertificateRequest request = new IssueCertificateRequest(
                userId,
                contentItemId,
                null,
                Map.of()
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail(adminEmail)).thenReturn(Optional.of(admin));
        when(contentItemRepository.findById(contentItemId)).thenReturn(Optional.of(contentItem));
        when(productRegistrationRepository.existsByUserIdAndContentItemIdAndStatus(
                userId,
                contentItemId,
                RegistrationStatus.ACTIVE
        )).thenReturn(true);
        when(userProcessRepository.findByUserIdAndContentItemId(userId, contentItemId))
                .thenReturn(Optional.of(userProcess));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> certificateService.issue(request, certificateFile, authentication)
        );

        assertEquals(400, ex.getStatusCode().value());
        assertEquals("User has not completed this content item", ex.getReason());

        verify(certificateRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("issue -> throws conflict when certificate already exists")
    void issue_ShouldThrowConflict_WhenCertificateAlreadyExists() {
        IssueCertificateRequest request = new IssueCertificateRequest(
                userId,
                contentItemId,
                null,
                Map.of()
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail(adminEmail)).thenReturn(Optional.of(admin));
        when(contentItemRepository.findById(contentItemId)).thenReturn(Optional.of(contentItem));
        when(productRegistrationRepository.existsByUserIdAndContentItemIdAndStatus(
                userId,
                contentItemId,
                RegistrationStatus.ACTIVE
        )).thenReturn(true);
        when(certificateRepository.existsByUserIdAndContentItemId(userId, contentItemId)).thenReturn(true);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> certificateService.issue(request, certificateFile, authentication)
        );

        assertEquals(409, ex.getStatusCode().value());
        assertEquals("Certificate already exists for this user and content item", ex.getReason());

        verify(certificateRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("issue -> deletes new PDF when second database save fails")
    void issue_ShouldDeleteNewPdf_WhenSecondSaveFails() {
        IssueCertificateRequest request = new IssueCertificateRequest(
                userId,
                contentItemId,
                null,
                Map.of()
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail(adminEmail)).thenReturn(Optional.of(admin));
        when(contentItemRepository.findById(contentItemId)).thenReturn(Optional.of(contentItem));
        when(productRegistrationRepository.existsByUserIdAndContentItemIdAndStatus(
                userId,
                contentItemId,
                RegistrationStatus.ACTIVE
        )).thenReturn(true);
        when(certificateRepository.existsByUserIdAndContentItemId(userId, contentItemId)).thenReturn(false);
        when(certificateRepository.existsByCertificateCodeIgnoreCase(anyString())).thenReturn(false);
        when(certificateStorageService.saveCertificatePdf(eq(certificateFile), eq(certificateId))).thenReturn(pdfUrl);

        when(certificateRepository.saveAndFlush(any(Certificate.class)))
                .thenAnswer(invocation -> {
                    Certificate saved = invocation.getArgument(0);
                    saved.setId(certificateId);
                    return saved;
                })
                .thenThrow(new DataIntegrityViolationException("Certificate conflict"));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> certificateService.issue(request, certificateFile, authentication)
        );

        assertEquals(409, ex.getStatusCode().value());
        assertEquals("Certificate already exists or certificate code is duplicated", ex.getReason());

        verify(certificateStorageService).delete(pdfUrl);
    }

    @Test
    @DisplayName("getById -> returns mapped certificate")
    void getById_ShouldReturnMappedCertificate() {
        CertificateResponse response = mock(CertificateResponse.class);

        when(certificateRepository.findById(certificateId)).thenReturn(Optional.of(certificate));
        when(certificateMapper.toResponse(certificate)).thenReturn(response);

        CertificateResponse result = certificateService.getById(certificateId);

        assertNotNull(result);
        assertEquals(response, result);
    }

    @Test
    @DisplayName("verifyByCode -> returns mapped certificate")
    void verifyByCode_ShouldReturnMappedCertificate() {
        CertificateResponse response = mock(CertificateResponse.class);

        when(certificateRepository.findByCertificateCodeIgnoreCase("CERT-TEST1234567890"))
                .thenReturn(Optional.of(certificate));
        when(certificateMapper.toResponse(certificate)).thenReturn(response);

        CertificateResponse result = certificateService.verifyByCode(" CERT-TEST1234567890 ");

        assertNotNull(result);
        assertEquals(response, result);
    }

    @Test
    @DisplayName("verifyByCode -> throws bad request when code is blank")
    void verifyByCode_ShouldThrowBadRequest_WhenCodeIsBlank() {
        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> certificateService.verifyByCode("   ")
        );

        assertEquals(400, ex.getStatusCode().value());
        assertEquals("Certificate code must not be blank", ex.getReason());
    }

    @Test
    @DisplayName("getMyCertificates -> returns current user's certificates")
    void getMyCertificates_ShouldReturnCurrentUserCertificates() {
        List<Certificate> certificates = List.of(certificate);
        List<CertificateResponse> responses = List.of(mock(CertificateResponse.class));

        when(userRepository.findByEmail(adminEmail)).thenReturn(Optional.of(admin));
        when(certificateRepository.findAllByUserId(adminId)).thenReturn(certificates);
        when(certificateMapper.toResponseList(certificates)).thenReturn(responses);

        List<CertificateResponse> result = certificateService.getMyCertificates(authentication);

        assertNotNull(result);
        assertEquals(responses, result);
    }

    @Test
    @DisplayName("getAllByUser -> returns user's certificates")
    void getAllByUser_ShouldReturnUserCertificates() {
        List<Certificate> certificates = List.of(certificate);
        List<CertificateResponse> responses = List.of(mock(CertificateResponse.class));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(certificateRepository.findAllByUserId(userId)).thenReturn(certificates);
        when(certificateMapper.toResponseList(certificates)).thenReturn(responses);

        List<CertificateResponse> result = certificateService.getAllByUser(userId);

        assertNotNull(result);
        assertEquals(responses, result);
    }

    @Test
    @DisplayName("getAllByContentItem -> returns content item certificates")
    void getAllByContentItem_ShouldReturnContentItemCertificates() {
        List<Certificate> certificates = List.of(certificate);
        List<CertificateResponse> responses = List.of(mock(CertificateResponse.class));

        when(contentItemRepository.findById(contentItemId)).thenReturn(Optional.of(contentItem));
        when(certificateRepository.findAllByContentItemId(contentItemId)).thenReturn(certificates);
        when(certificateMapper.toResponseList(certificates)).thenReturn(responses);

        List<CertificateResponse> result = certificateService.getAllByContentItem(contentItemId);

        assertNotNull(result);
        assertEquals(responses, result);
    }

    @Test
    @DisplayName("update -> updates metadata and expiry date")
    void update_ShouldUpdateMetadataAndExpiryDate() {
        OffsetDateTime expiryDate = certificate.getIssueDate().plusYears(1);

        UpdateCertificateRequest request = new UpdateCertificateRequest(
                expiryDate,
                Map.of("finalScore", 95)
        );

        CertificateResponse response = mock(CertificateResponse.class);

        when(certificateRepository.findById(certificateId)).thenReturn(Optional.of(certificate));
        when(certificateRepository.saveAndFlush(certificate)).thenReturn(certificate);
        when(certificateMapper.toResponse(certificate)).thenReturn(response);

        CertificateResponse result = certificateService.update(certificateId, request, null);

        assertNotNull(result);
        assertEquals(response, result);
        assertEquals(expiryDate, certificate.getExpiryDate());
        assertEquals(Map.of("finalScore", 95), certificate.getMetadata());

        verify(certificateStorageService, never()).saveCertificatePdf(any(), any());
    }

    @Test
    @DisplayName("update -> updates certificate file and deletes old file")
    void update_ShouldUpdateCertificateFileAndDeleteOldFile() {
        CertificateResponse response = mock(CertificateResponse.class);

        when(certificateFile.isEmpty()).thenReturn(false);
        when(certificateRepository.findById(certificateId)).thenReturn(Optional.of(certificate));
        when(certificateStorageService.saveCertificatePdf(certificateFile, certificateId)).thenReturn(pdfUrl);
        when(certificateRepository.saveAndFlush(certificate)).thenReturn(certificate);
        when(certificateMapper.toResponse(certificate)).thenReturn(response);

        CertificateResponse result = certificateService.update(certificateId, null, certificateFile);

        assertNotNull(result);
        assertEquals(response, result);
        assertEquals(pdfUrl, certificate.getPdfUrl());

        verify(certificateStorageService).delete(oldPdfUrl);
    }

    @Test
    @DisplayName("update -> throws bad request when nothing to update")
    void update_ShouldThrowBadRequest_WhenNothingToUpdate() {
        when(certificateRepository.findById(certificateId)).thenReturn(Optional.of(certificate));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> certificateService.update(certificateId, null, null)
        );

        assertEquals(400, ex.getStatusCode().value());
        assertEquals("Nothing to update", ex.getReason());

        verify(certificateRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("update -> throws bad request when expiry date is before issue date")
    void update_ShouldThrowBadRequest_WhenExpiryDateIsBeforeIssueDate() {
        UpdateCertificateRequest request = new UpdateCertificateRequest(
                certificate.getIssueDate().minusDays(1),
                null
        );

        when(certificateRepository.findById(certificateId)).thenReturn(Optional.of(certificate));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> certificateService.update(certificateId, request, null)
        );

        assertEquals(400, ex.getStatusCode().value());
        assertEquals("Expiry date must be after issue date", ex.getReason());

        verify(certificateRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("update -> deletes new PDF when database save fails")
    void update_ShouldDeleteNewPdf_WhenDatabaseSaveFails() {
        when(certificateFile.isEmpty()).thenReturn(false);
        when(certificateRepository.findById(certificateId)).thenReturn(Optional.of(certificate));
        when(certificateStorageService.saveCertificatePdf(certificateFile, certificateId)).thenReturn(pdfUrl);
        when(certificateRepository.saveAndFlush(certificate))
                .thenThrow(new DataIntegrityViolationException("Update conflict"));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> certificateService.update(certificateId, null, certificateFile)
        );

        assertEquals(409, ex.getStatusCode().value());
        assertEquals("Unable to update certificate", ex.getReason());

        verify(certificateStorageService).delete(pdfUrl);
        verify(certificateStorageService, never()).delete(oldPdfUrl);
    }

    @Test
    @DisplayName("delete -> deletes certificate and certificate PDF")
    void delete_ShouldDeleteCertificateAndPdf() {
        when(certificateRepository.findById(certificateId)).thenReturn(Optional.of(certificate));

        certificateService.delete(certificateId);

        verify(certificateRepository).delete(certificate);
        verify(certificateRepository).flush();
        verify(certificateStorageService).delete(oldPdfUrl);
    }

    @Test
    @DisplayName("delete -> throws not found when certificate does not exist")
    void delete_ShouldThrowNotFound_WhenCertificateDoesNotExist() {
        when(certificateRepository.findById(certificateId)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> certificateService.delete(certificateId)
        );

        assertEquals(404, ex.getStatusCode().value());
        assertEquals("Certificate not found with id: " + certificateId, ex.getReason());

        verify(certificateRepository, never()).delete(any());
        verify(certificateStorageService, never()).delete(anyString());
    }
}