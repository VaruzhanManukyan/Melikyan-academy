package com.melikyan.academy.controller;

import org.springframework.http.MediaType;
import com.melikyan.academy.service.ProductService;
import org.springframework.test.web.servlet.MockMvc;
import com.melikyan.academy.entity.enums.ProductType;
import com.melikyan.academy.entity.enums.ContentItemType;
import org.springframework.context.annotation.FilterType;
import com.melikyan.academy.security.RememberMeSecurityFilter;
import com.melikyan.academy.dto.response.product.ProductResponse;
import com.melikyan.academy.dto.response.category.CategoryShortResponse;
import com.melikyan.academy.dto.response.contentItem.ContentItemShortResponse;
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

import java.util.UUID;
import java.util.List;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doNothing;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(
        controllers = ProductController.class,
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
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @Test
    @DisplayName("GET /api/v1/products/{id} -> returns product by id")
    void getById_ShouldReturnProduct() throws Exception {
        UUID productId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        UUID createdById = UUID.randomUUID();
        UUID contentItemId = UUID.randomUUID();

        ProductResponse response = new ProductResponse(
                productId,
                "Java Backend Package",
                "Spring Boot package",
                ProductType.PACKAGE,
                new BigDecimal("149.99"),
                false,
                List.of(
                        new ContentItemShortResponse(
                                contentItemId,
                                "Spring Boot Course",
                                "Backend",
                                ContentItemType.COURSE
                        )
                ),
                new CategoryShortResponse(
                        categoryId,
                        "Backend",
                        "Backend"
                ),
                createdById,
                OffsetDateTime.parse("2026-04-15T12:00:00+04:00"),
                OffsetDateTime.parse("2026-04-15T12:30:00+04:00")
        );

        when(productService.getById(productId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId.toString()))
                .andExpect(jsonPath("$.title").value("Java Backend Package"))
                .andExpect(jsonPath("$.description").value("Spring Boot package"))
                .andExpect(jsonPath("$.type").value("PACKAGE"))
                .andExpect(jsonPath("$.price").value(149.99))
                .andExpect(jsonPath("$.isPrivate").value(false))
                .andExpect(jsonPath("$.category.id").value(categoryId.toString()))
                .andExpect(jsonPath("$.category.title").value("Backend"))
                .andExpect(jsonPath("$.createdById").value(createdById.toString()))
                .andExpect(jsonPath("$.contentItems[0].id").value(contentItemId.toString()))
                .andExpect(jsonPath("$.contentItems[0].title").value("Spring Boot Course"))
                .andExpect(jsonPath("$.contentItems[0].type").value("COURSE"));
    }

    @Test
    @DisplayName("GET /api/v1/products -> returns all products")
    void getAll_ShouldReturnAllProducts() throws Exception {
        UUID categoryId = UUID.randomUUID();
        UUID createdById = UUID.randomUUID();

        ProductResponse first = new ProductResponse(
                UUID.randomUUID(),
                "Java Package",
                "First package",
                ProductType.PACKAGE,
                new BigDecimal("149.99"),
                false,
                List.of(),
                new CategoryShortResponse(categoryId, "Backend", "Backend"),
                createdById,
                OffsetDateTime.parse("2026-04-15T12:00:00+04:00"),
                OffsetDateTime.parse("2026-04-15T12:30:00+04:00")
        );

        ProductResponse second = new ProductResponse(
                UUID.randomUUID(),
                "Frontend Package",
                "Second package",
                ProductType.PACKAGE,
                new BigDecimal("99.99"),
                true,
                List.of(),
                new CategoryShortResponse(categoryId, "Frontend", "Frontend"),
                createdById,
                OffsetDateTime.parse("2026-04-16T12:00:00+04:00"),
                OffsetDateTime.parse("2026-04-16T12:30:00+04:00")
        );

        when(productService.getAll()).thenReturn(List.of(first, second));

        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("Java Package"))
                .andExpect(jsonPath("$[0].type").value("PACKAGE"))
                .andExpect(jsonPath("$[1].title").value("Frontend Package"))
                .andExpect(jsonPath("$[1].type").value("PACKAGE"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("POST /api/v1/products -> creates product")
    void create_ShouldReturnCreatedProduct() throws Exception {
        UUID productId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        UUID createdById = UUID.randomUUID();
        UUID contentItemId = UUID.randomUUID();

        String requestJson = """
                {
                  "type": "PACKAGE",
                  "title": "Java Backend Package",
                  "description": "Spring Boot package",
                  "price": 149.99,
                  "is_private": false,
                  "categoryId": "%s",
                  "createdById": "%s",
                  "contentItemIds": ["%s"]
                }
                """.formatted(categoryId, createdById, contentItemId);

        ProductResponse response = new ProductResponse(
                productId,
                "Java Backend Package",
                "Spring Boot package",
                ProductType.PACKAGE,
                new BigDecimal("149.99"),
                false,
                List.of(
                        new ContentItemShortResponse(
                                contentItemId,
                                "Spring Boot Course",
                                "Backend",
                                ContentItemType.COURSE
                        )
                ),
                new CategoryShortResponse(categoryId, "Backend", "Backend"),
                createdById,
                OffsetDateTime.parse("2026-04-15T12:00:00+04:00"),
                OffsetDateTime.parse("2026-04-15T12:30:00+04:00")
        );

        when(productService.create(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(productId.toString()))
                .andExpect(jsonPath("$.title").value("Java Backend Package"))
                .andExpect(jsonPath("$.description").value("Spring Boot package"))
                .andExpect(jsonPath("$.type").value("PACKAGE"))
                .andExpect(jsonPath("$.price").value(149.99))
                .andExpect(jsonPath("$.createdById").value(createdById.toString()))
                .andExpect(jsonPath("$.category.id").value(categoryId.toString()))
                .andExpect(jsonPath("$.contentItems[0].id").value(contentItemId.toString()))
                .andExpect(jsonPath("$.contentItems[0].title").value("Spring Boot Course"))
                .andExpect(jsonPath("$.contentItems[0].type").value("COURSE"));
    }

    @Test
    @WithMockUser(roles = {"PROFESSOR"})
    @DisplayName("PATCH /api/v1/products/{id} -> updates product")
    void update_ShouldReturnUpdatedProduct() throws Exception {
        UUID productId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        UUID createdById = UUID.randomUUID();

        String requestJson = """
                {
                  "title": "Updated Java Package",
                  "description": "Updated package description",
                  "price": 199.99,
                  "is_private": true
                }
                """;

        ProductResponse response = new ProductResponse(
                productId,
                "Updated Java Package",
                "Updated package description",
                ProductType.PACKAGE,
                new BigDecimal("199.99"),
                true,
                List.of(),
                new CategoryShortResponse(categoryId, "Backend", "Backend"),
                createdById,
                OffsetDateTime.parse("2026-04-15T12:00:00+04:00"),
                OffsetDateTime.parse("2026-04-15T13:00:00+04:00")
        );

        when(productService.update(eq(productId), any())).thenReturn(response);

        mockMvc.perform(patch("/api/v1/products/{id}", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId.toString()))
                .andExpect(jsonPath("$.title").value("Updated Java Package"))
                .andExpect(jsonPath("$.description").value("Updated package description"))
                .andExpect(jsonPath("$.type").value("PACKAGE"))
                .andExpect(jsonPath("$.price").value(199.99))
                .andExpect(jsonPath("$.isPrivate").value(true));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("DELETE /api/v1/products/{id} -> deletes product")
    void delete_ShouldReturnNoContent() throws Exception {
        UUID productId = UUID.randomUUID();

        doNothing().when(productService).delete(productId);

        mockMvc.perform(delete("/api/v1/products/{id}", productId))
                .andExpect(status().isNoContent());

        verify(productService).delete(productId);
    }
}