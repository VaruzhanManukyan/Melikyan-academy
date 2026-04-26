package com.melikyan.academy.controller;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockMultipartFile;
import com.melikyan.academy.service.CertificateService;
import org.springframework.context.annotation.FilterType;
import com.melikyan.academy.security.RememberMeSecurityFilter;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;

@WebMvcTest(
        controllers = CertificateController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class,
                ServletWebSecurityAutoConfiguration.class,
                UserDetailsServiceAutoConfiguration.class
        },
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = RememberMeSecurityFilter.class
                )
        }
)
@AutoConfigureMockMvc(addFilters = false)
class CertificateControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CertificateService certificateService;

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("POST /api/v1/certificates/issue -> issues certificate")
    void issue_ShouldReturnCreatedCertificate() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID contentItemId = UUID.randomUUID();

        String requestJson = """
                {
                  "userId": "%s",
                  "contentItemId": "%s",
                  "expiryDate": null,
                  "metadata": {
                    "courseTitle": "Java Backend Development",
                    "finalScore": 92
                  }
                }
                """.formatted(userId, contentItemId);

        MockMultipartFile requestPart = new MockMultipartFile(
                "request",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                requestJson.getBytes()
        );

        MockMultipartFile certificatePart = new MockMultipartFile(
                "certificate",
                "certificate.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "%PDF-1.4 test pdf".getBytes()
        );

        when(certificateService.issue(any(), any(), any())).thenReturn(null);

        mockMvc.perform(multipart("/api/v1/certificates/issue")
                        .file(requestPart)
                        .file(certificatePart)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated());

        verify(certificateService).issue(any(), any(), any());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("GET /api/v1/certificates/me -> returns my certificates")
    void getMyCertificates_ShouldReturnMyCertificates() throws Exception {
        when(certificateService.getMyCertificates(any())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/certificates/me"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(certificateService).getMyCertificates(any());
    }

    @Test
    @DisplayName("GET /api/v1/certificates/verify/{certificateCode} -> verifies certificate by code")
    void verifyByCode_ShouldReturnCertificate() throws Exception {
        String certificateCode = "CERT-ABC123";

        when(certificateService.verifyByCode(certificateCode)).thenReturn(null);

        mockMvc.perform(get("/api/v1/certificates/verify/{certificateCode}", certificateCode))
                .andExpect(status().isOk());

        verify(certificateService).verifyByCode(certificateCode);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("GET /api/v1/certificates/{id} -> returns certificate by id")
    void getById_ShouldReturnCertificate() throws Exception {
        UUID certificateId = UUID.randomUUID();

        when(certificateService.getById(certificateId)).thenReturn(null);

        mockMvc.perform(get("/api/v1/certificates/{id}", certificateId))
                .andExpect(status().isOk());

        verify(certificateService).getById(certificateId);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("GET /api/v1/certificates/users/{userId} -> returns certificates by user")
    void getAllByUser_ShouldReturnCertificatesByUser() throws Exception {
        UUID userId = UUID.randomUUID();

        when(certificateService.getAllByUser(userId)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/certificates/users/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(certificateService).getAllByUser(userId);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("GET /api/v1/certificates/content-items/{contentItemId} -> returns certificates by content item")
    void getAllByContentItem_ShouldReturnCertificatesByContentItem() throws Exception {
        UUID contentItemId = UUID.randomUUID();

        when(certificateService.getAllByContentItem(contentItemId)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/certificates/content-items/{contentItemId}", contentItemId))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(certificateService).getAllByContentItem(contentItemId);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("PATCH /api/v1/certificates/{id} -> updates certificate with request and file")
    void update_ShouldReturnUpdatedCertificate_WhenRequestAndFileProvided() throws Exception {
        UUID certificateId = UUID.randomUUID();

        String requestJson = """
                {
                  "expiryDate": "2027-04-26T23:59:59+04:00",
                  "metadata": {
                    "finalScore": 95
                  }
                }
                """;

        MockMultipartFile requestPart = new MockMultipartFile(
                "request",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                requestJson.getBytes()
        );

        MockMultipartFile certificatePart = new MockMultipartFile(
                "certificate",
                "updated-certificate.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "%PDF-1.4 updated test pdf".getBytes()
        );

        when(certificateService.update(eq(certificateId), any(), any())).thenReturn(null);

        mockMvc.perform(multipart("/api/v1/certificates/{id}", certificateId)
                        .file(requestPart)
                        .file(certificatePart)
                        .with(request -> {
                            request.setMethod("PATCH");
                            return request;
                        })
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk());

        verify(certificateService).update(eq(certificateId), any(), any());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("PATCH /api/v1/certificates/{id} -> updates only certificate file")
    void update_ShouldReturnUpdatedCertificate_WhenOnlyFileProvided() throws Exception {
        UUID certificateId = UUID.randomUUID();

        MockMultipartFile certificatePart = new MockMultipartFile(
                "certificate",
                "updated-certificate.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "%PDF-1.4 updated test pdf".getBytes()
        );

        when(certificateService.update(eq(certificateId), isNull(), any())).thenReturn(null);

        mockMvc.perform(multipart("/api/v1/certificates/{id}", certificateId)
                        .file(certificatePart)
                        .with(request -> {
                            request.setMethod("PATCH");
                            return request;
                        })
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk());

        verify(certificateService).update(eq(certificateId), isNull(), any());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("PATCH /api/v1/certificates/{id} -> updates only request data")
    void update_ShouldReturnUpdatedCertificate_WhenOnlyRequestProvided() throws Exception {
        UUID certificateId = UUID.randomUUID();

        String requestJson = """
                {
                  "metadata": {
                    "finalScore": 99
                  }
                }
                """;

        MockMultipartFile requestPart = new MockMultipartFile(
                "request",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                requestJson.getBytes()
        );

        when(certificateService.update(eq(certificateId), any(), isNull())).thenReturn(null);

        mockMvc.perform(multipart("/api/v1/certificates/{id}", certificateId)
                        .file(requestPart)
                        .with(request -> {
                            request.setMethod("PATCH");
                            return request;
                        })
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk());

        verify(certificateService).update(eq(certificateId), any(), isNull());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("DELETE /api/v1/certificates/{id} -> deletes certificate")
    void delete_ShouldReturnNoContent() throws Exception {
        UUID certificateId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/certificates/{id}", certificateId))
                .andExpect(status().isNoContent());

        verify(certificateService).delete(certificateId);
    }
}