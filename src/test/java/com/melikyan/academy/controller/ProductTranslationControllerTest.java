package com.melikyan.academy.controller;

import com.melikyan.academy.dto.request.productTranslation.CreateProductTranslationRequest;
import com.melikyan.academy.dto.request.productTranslation.UpdateProductTranslationRequest;
import com.melikyan.academy.dto.response.productTranslation.ProductTranslationResponse;
import com.melikyan.academy.security.RememberMeSecurityFilter;
import com.melikyan.academy.service.ProductTranslationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = ProductTranslationController.class,
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
class ProductTranslationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductTranslationService productTranslationService;

    @Test
    void createProductTranslation_shouldReturnCreated() throws Exception {
        UUID productId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID translationId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

        ProductTranslationResponse response = new ProductTranslationResponse(
                translationId,
                "Product title",
                "Product description",
                "en",
                productId,
                userId,
                now,
                now
        );

        when(productTranslationService.create(any(CreateProductTranslationRequest.class)))
                .thenReturn(response);

        String requestJson = """
                {
                  "code": "en",
                  "title": "Product title",
                  "description": "Product description",
                  "productId": "%s",
                  "createdById": "%s"
                }
                """.formatted(productId, userId);

        mockMvc.perform(post("/api/v1/product-translations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated());

        verify(productTranslationService).create(any(CreateProductTranslationRequest.class));
    }

    @Test
    void getById_shouldReturnOk() throws Exception {
        UUID productId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID translationId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

        ProductTranslationResponse response = new ProductTranslationResponse(
                translationId,
                "Product title",
                "Product description",
                "en",
                productId,
                userId,
                now,
                now
        );

        when(productTranslationService.getById(translationId))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/product-translations/{id}", translationId))
                .andExpect(status().isOk());

        verify(productTranslationService).getById(translationId);
    }

    @Test
    void getAll_shouldReturnOk() throws Exception {
        UUID productId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

        List<ProductTranslationResponse> responses = List.of(
                new ProductTranslationResponse(
                        UUID.randomUUID(),
                        "Product title",
                        "Product description",
                        "en",
                        productId,
                        userId,
                        now,
                        now
                ),
                new ProductTranslationResponse(
                        UUID.randomUUID(),
                        "Ապրանք",
                        "Ապրանքի նկարագրություն",
                        "hy",
                        productId,
                        userId,
                        now,
                        now
                )
        );

        when(productTranslationService.getAll())
                .thenReturn(responses);

        mockMvc.perform(get("/api/v1/product-translations"))
                .andExpect(status().isOk());

        verify(productTranslationService).getAll();
    }

    @Test
    void getByCode_shouldReturnOk() throws Exception {
        UUID productId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

        List<ProductTranslationResponse> responses = List.of(
                new ProductTranslationResponse(
                        UUID.randomUUID(),
                        "Product title",
                        "Product description",
                        "en",
                        productId,
                        userId,
                        now,
                        now
                )
        );

        when(productTranslationService.getByCode("en"))
                .thenReturn(responses);

        mockMvc.perform(get("/api/v1/product-translations/code/{code}", "en"))
                .andExpect(status().isOk());

        verify(productTranslationService).getByCode("en");
    }

    @Test
    void getByProductId_shouldReturnOk() throws Exception {
        UUID productId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

        List<ProductTranslationResponse> responses = List.of(
                new ProductTranslationResponse(
                        UUID.randomUUID(),
                        "Product title",
                        "Product description",
                        "en",
                        productId,
                        userId,
                        now,
                        now
                )
        );

        when(productTranslationService.getByProductId(productId))
                .thenReturn(responses);

        mockMvc.perform(get("/api/v1/product-translations/product/{productId}", productId))
                .andExpect(status().isOk());

        verify(productTranslationService).getByProductId(productId);
    }

    @Test
    void getByProductIdAndCode_shouldReturnOk() throws Exception {
        UUID productId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID translationId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

        ProductTranslationResponse response = new ProductTranslationResponse(
                translationId,
                "Product title",
                "Product description",
                "en",
                productId,
                userId,
                now,
                now
        );

        when(productTranslationService.getByProductIdAndCode(productId, "en"))
                .thenReturn(response);

        mockMvc.perform(get(
                        "/api/v1/product-translations/product/{productId}/code/{code}",
                        productId,
                        "en"
                ))
                .andExpect(status().isOk());

        verify(productTranslationService).getByProductIdAndCode(productId, "en");
    }

    @Test
    void update_shouldReturnOk() throws Exception {
        UUID productId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID translationId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

        ProductTranslationResponse response = new ProductTranslationResponse(
                translationId,
                "Ապրանք",
                "Թարմացված նկարագրություն",
                "hy",
                productId,
                userId,
                now,
                now
        );

        when(productTranslationService.update(eq(translationId), any(UpdateProductTranslationRequest.class)))
                .thenReturn(response);

        String requestJson = """
                {
                  "code": "hy",
                  "title": "Ապրանք",
                  "description": "Թարմացված նկարագրություն"
                }
                """;

        mockMvc.perform(patch("/api/v1/product-translations/{id}", translationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk());

        verify(productTranslationService).update(eq(translationId), any(UpdateProductTranslationRequest.class));
    }

    @Test
    void delete_shouldReturnNoContent() throws Exception {
        UUID translationId = UUID.randomUUID();

        doNothing().when(productTranslationService).delete(translationId);

        mockMvc.perform(delete("/api/v1/product-translations/{id}", translationId))
                .andExpect(status().isNoContent());

        verify(productTranslationService).delete(translationId);
    }
}
