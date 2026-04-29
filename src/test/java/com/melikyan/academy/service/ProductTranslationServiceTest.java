package com.melikyan.academy.service;

import com.melikyan.academy.dto.request.productTranslation.CreateProductTranslationRequest;
import com.melikyan.academy.dto.request.productTranslation.UpdateProductTranslationRequest;
import com.melikyan.academy.dto.response.productTranslation.ProductTranslationResponse;
import com.melikyan.academy.entity.Product;
import com.melikyan.academy.entity.ProductTranslation;
import com.melikyan.academy.entity.User;
import com.melikyan.academy.mapper.ProductTranslationMapper;
import com.melikyan.academy.repository.LanguageRepository;
import com.melikyan.academy.repository.ProductRepository;
import com.melikyan.academy.repository.ProductTranslationRepository;
import com.melikyan.academy.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.RecordComponent;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductTranslationServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private LanguageRepository languageRepository;

    @Mock
    private ProductTranslationMapper productTranslationMapper;

    @Mock
    private ProductTranslationRepository productTranslationRepository;

    @InjectMocks
    private ProductTranslationService productTranslationService;

    private CreateProductTranslationRequest createRequest(
            String code,
            String title,
            String description,
            UUID productId,
            UUID createdById
    ) {
        Map<String, Object> values = new HashMap<>();
        values.put("code", code);
        values.put("title", title);
        values.put("description", description);
        values.put("productId", productId);
        values.put("createdById", createdById);

        return createRecord(CreateProductTranslationRequest.class, values);
    }

    private UpdateProductTranslationRequest updateRequest(
            String code,
            String title,
            String description
    ) {
        Map<String, Object> values = new HashMap<>();
        values.put("code", code);
        values.put("title", title);
        values.put("description", description);

        return createRecord(UpdateProductTranslationRequest.class, values);
    }

    private <T> T createRecord(Class<T> type, Map<String, Object> values) {
        try {
            RecordComponent[] components = type.getRecordComponents();

            Class<?>[] parameterTypes = Arrays.stream(components)
                    .map(RecordComponent::getType)
                    .toArray(Class<?>[]::new);

            Object[] args = Arrays.stream(components)
                    .map(component -> values.get(component.getName()))
                    .toArray();

            return type.getDeclaredConstructor(parameterTypes).newInstance(args);
        } catch (ReflectiveOperationException exception) {
            throw new RuntimeException(exception);
        }
    }

    @Test
    void createProductTranslation_shouldCreateAndReturnResponse() {
        UUID productId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID translationId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

        CreateProductTranslationRequest request = createRequest(
                " EN ",
                "  Product title  ",
                "  Product description  ",
                productId,
                userId
        );

        Product product = new Product();
        product.setId(productId);

        User user = new User();
        user.setId(userId);

        ProductTranslation savedProductTranslation = new ProductTranslation();
        savedProductTranslation.setId(translationId);
        savedProductTranslation.setCode("en");
        savedProductTranslation.setTitle("Product title");
        savedProductTranslation.setDescription("Product description");
        savedProductTranslation.setProduct(product);
        savedProductTranslation.setCreatedBy(user);

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

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(languageRepository.existsByCodeIgnoreCase("en")).thenReturn(true);
        when(productTranslationRepository.existsByProductIdAndCodeIgnoreCase(productId, "en"))
                .thenReturn(false);
        when(productTranslationRepository.saveAndFlush(any(ProductTranslation.class)))
                .thenReturn(savedProductTranslation);
        when(productTranslationMapper.toResponse(savedProductTranslation))
                .thenReturn(response);

        ProductTranslationResponse result = productTranslationService.create(request);

        assertNotNull(result);
        assertEquals(response, result);

        ArgumentCaptor<ProductTranslation> captor = ArgumentCaptor.forClass(ProductTranslation.class);
        verify(productTranslationRepository).saveAndFlush(captor.capture());

        ProductTranslation productTranslation = captor.getValue();

        assertEquals("en", productTranslation.getCode());
        assertEquals("Product title", productTranslation.getTitle());
        assertEquals("Product description", productTranslation.getDescription());
        assertEquals(product, productTranslation.getProduct());
        assertEquals(user, productTranslation.getCreatedBy());

        verify(productRepository).findById(productId);
        verify(userRepository).findById(userId);
        verify(languageRepository).existsByCodeIgnoreCase("en");
        verify(productTranslationRepository).existsByProductIdAndCodeIgnoreCase(productId, "en");
        verify(productTranslationMapper).toResponse(savedProductTranslation);
    }

    @Test
    void create_shouldThrowNotFound_whenProductDoesNotExist() {
        UUID productId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        CreateProductTranslationRequest request = createRequest(
                "en",
                "Product title",
                "Description",
                productId,
                userId
        );

        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> productTranslationService.create(request)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Product not found with id"));

        verify(productRepository).findById(productId);
        verify(userRepository, never()).findById(any());
        verify(languageRepository, never()).existsByCodeIgnoreCase(any());
        verify(productTranslationRepository, never()).saveAndFlush(any());
    }

    @Test
    void create_shouldThrowNotFound_whenUserDoesNotExist() {
        UUID productId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        CreateProductTranslationRequest request = createRequest(
                "en",
                "Product title",
                "Description",
                productId,
                userId
        );

        Product product = new Product();
        product.setId(productId);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> productTranslationService.create(request)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(exception.getReason().contains("User not found with id"));

        verify(productRepository).findById(productId);
        verify(userRepository).findById(userId);
        verify(languageRepository, never()).existsByCodeIgnoreCase(any());
        verify(productTranslationRepository, never()).saveAndFlush(any());
    }

    @Test
    void create_shouldThrowNotFound_whenLanguageDoesNotExist() {
        UUID productId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        CreateProductTranslationRequest request = createRequest(
                "en",
                "Product title",
                "Description",
                productId,
                userId
        );

        Product product = new Product();
        product.setId(productId);

        User user = new User();
        user.setId(userId);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(languageRepository.existsByCodeIgnoreCase("en")).thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> productTranslationService.create(request)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("404 NOT_FOUND \"Language not found with code: en\"", exception.getMessage());

        verify(languageRepository).existsByCodeIgnoreCase("en");
        verify(productTranslationRepository, never()).saveAndFlush(any());
    }

    @Test
    void create_shouldThrowConflict_whenTranslationAlreadyExistsForProduct() {
        UUID productId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        CreateProductTranslationRequest request = createRequest(
                "en",
                "Product title",
                "Description",
                productId,
                userId
        );

        Product product = new Product();
        product.setId(productId);

        User user = new User();
        user.setId(userId);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(languageRepository.existsByCodeIgnoreCase("en")).thenReturn(true);
        when(productTranslationRepository.existsByProductIdAndCodeIgnoreCase(productId, "en"))
                .thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> productTranslationService.create(request)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals(
                "409 CONFLICT \"Product translation with this code already exists for this product\"",
                exception.getMessage()
        );

        verify(productTranslationRepository).existsByProductIdAndCodeIgnoreCase(productId, "en");
        verify(productTranslationRepository, never()).saveAndFlush(any());
    }

    @Test
    void create_shouldThrowBadRequest_whenCodeIsBlank() {
        UUID productId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        CreateProductTranslationRequest request = createRequest(
                "   ",
                "Product title",
                "Description",
                productId,
                userId
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> productTranslationService.create(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals(
                "400 BAD_REQUEST \"Product translation code must not be blank\"",
                exception.getMessage()
        );

        verify(productRepository, never()).findById(any());
        verify(productTranslationRepository, never()).saveAndFlush(any());
    }

    @Test
    void create_shouldThrowBadRequest_whenTitleIsBlank() {
        UUID productId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        CreateProductTranslationRequest request = createRequest(
                "en",
                "   ",
                "Description",
                productId,
                userId
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> productTranslationService.create(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals(
                "400 BAD_REQUEST \"Product translation title must not be blank\"",
                exception.getMessage()
        );

        verify(productRepository, never()).findById(any());
        verify(productTranslationRepository, never()).saveAndFlush(any());
    }

    @Test
    void create_shouldThrowConflict_whenSaveFailsByUniqueConstraint() {
        UUID productId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        CreateProductTranslationRequest request = createRequest(
                "en",
                "Product title",
                "Description",
                productId,
                userId
        );

        Product product = new Product();
        product.setId(productId);

        User user = new User();
        user.setId(userId);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(languageRepository.existsByCodeIgnoreCase("en")).thenReturn(true);
        when(productTranslationRepository.existsByProductIdAndCodeIgnoreCase(productId, "en"))
                .thenReturn(false);
        when(productTranslationRepository.saveAndFlush(any(ProductTranslation.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> productTranslationService.create(request)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals(
                "409 CONFLICT \"Product translation with this code already exists for this product\"",
                exception.getMessage()
        );

        verify(productTranslationRepository).saveAndFlush(any(ProductTranslation.class));
        verify(productTranslationMapper, never()).toResponse(any());
    }

    @Test
    void getById_shouldReturnResponse() {
        UUID productId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID translationId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

        Product product = new Product();
        product.setId(productId);

        User user = new User();
        user.setId(userId);

        ProductTranslation productTranslation = new ProductTranslation();
        productTranslation.setId(translationId);
        productTranslation.setCode("en");
        productTranslation.setTitle("Product title");
        productTranslation.setDescription("Product description");
        productTranslation.setProduct(product);
        productTranslation.setCreatedBy(user);

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

        when(productTranslationRepository.findById(translationId))
                .thenReturn(Optional.of(productTranslation));
        when(productTranslationMapper.toResponse(productTranslation))
                .thenReturn(response);

        ProductTranslationResponse result = productTranslationService.getById(translationId);

        assertNotNull(result);
        assertEquals(response, result);

        verify(productTranslationRepository).findById(translationId);
        verify(productTranslationMapper).toResponse(productTranslation);
    }

    @Test
    void getById_shouldThrowNotFound_whenDoesNotExist() {
        UUID translationId = UUID.randomUUID();

        when(productTranslationRepository.findById(translationId))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> productTranslationService.getById(translationId)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Product translation not found with id"));

        verify(productTranslationRepository).findById(translationId);
        verify(productTranslationMapper, never()).toResponse(any());
    }

    @Test
    void getAll_shouldReturnResponseList() {
        ProductTranslation translation1 = new ProductTranslation();
        translation1.setId(UUID.randomUUID());
        translation1.setCode("en");

        ProductTranslation translation2 = new ProductTranslation();
        translation2.setId(UUID.randomUUID());
        translation2.setCode("hy");

        List<ProductTranslation> translations = List.of(translation1, translation2);

        List<ProductTranslationResponse> responses = List.of(
                mock(ProductTranslationResponse.class),
                mock(ProductTranslationResponse.class)
        );

        when(productTranslationRepository.findAll()).thenReturn(translations);
        when(productTranslationMapper.toResponseList(translations)).thenReturn(responses);

        List<ProductTranslationResponse> result = productTranslationService.getAll();

        assertNotNull(result);
        assertEquals(2, result.size());

        verify(productTranslationRepository).findAll();
        verify(productTranslationMapper).toResponseList(translations);
    }

    @Test
    void getByCode_shouldReturnResponseList() {
        ProductTranslation translation = new ProductTranslation();
        translation.setId(UUID.randomUUID());
        translation.setCode("en");

        List<ProductTranslation> translations = List.of(translation);
        List<ProductTranslationResponse> responses = List.of(mock(ProductTranslationResponse.class));

        when(languageRepository.existsByCodeIgnoreCase("en")).thenReturn(true);
        when(productTranslationRepository.findByCodeIgnoreCase("en")).thenReturn(translations);
        when(productTranslationMapper.toResponseList(translations)).thenReturn(responses);

        List<ProductTranslationResponse> result = productTranslationService.getByCode(" EN ");

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(languageRepository).existsByCodeIgnoreCase("en");
        verify(productTranslationRepository).findByCodeIgnoreCase("en");
        verify(productTranslationMapper).toResponseList(translations);
    }

    @Test
    void getByProductId_shouldReturnResponseList() {
        UUID productId = UUID.randomUUID();

        Product product = new Product();
        product.setId(productId);

        ProductTranslation translation = new ProductTranslation();
        translation.setId(UUID.randomUUID());
        translation.setProduct(product);

        List<ProductTranslation> translations = List.of(translation);
        List<ProductTranslationResponse> responses = List.of(mock(ProductTranslationResponse.class));

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productTranslationRepository.findByProductId(productId)).thenReturn(translations);
        when(productTranslationMapper.toResponseList(translations)).thenReturn(responses);

        List<ProductTranslationResponse> result = productTranslationService.getByProductId(productId);

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(productRepository).findById(productId);
        verify(productTranslationRepository).findByProductId(productId);
        verify(productTranslationMapper).toResponseList(translations);
    }

    @Test
    void getByProductIdAndCode_shouldReturnResponse() {
        UUID productId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID translationId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

        Product product = new Product();
        product.setId(productId);

        User user = new User();
        user.setId(userId);

        ProductTranslation translation = new ProductTranslation();
        translation.setId(translationId);
        translation.setCode("en");
        translation.setProduct(product);
        translation.setCreatedBy(user);

        ProductTranslationResponse response = new ProductTranslationResponse(
                translationId,
                "Product title",
                "Description",
                "en",
                productId,
                userId,
                now,
                now
        );

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(languageRepository.existsByCodeIgnoreCase("en")).thenReturn(true);
        when(productTranslationRepository.findByProductIdAndCodeIgnoreCase(productId, "en"))
                .thenReturn(Optional.of(translation));
        when(productTranslationMapper.toResponse(translation)).thenReturn(response);

        ProductTranslationResponse result =
                productTranslationService.getByProductIdAndCode(productId, " EN ");

        assertNotNull(result);
        assertEquals(response, result);

        verify(productRepository).findById(productId);
        verify(languageRepository).existsByCodeIgnoreCase("en");
        verify(productTranslationRepository).findByProductIdAndCodeIgnoreCase(productId, "en");
        verify(productTranslationMapper).toResponse(translation);
    }

    @Test
    void updateProductTranslation_shouldUpdateAndReturnResponse() {
        UUID productId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID translationId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

        UpdateProductTranslationRequest request = updateRequest(
                " HY ",
                "  Ապրանք  ",
                "  Հայերեն նկարագրություն  "
        );

        Product product = new Product();
        product.setId(productId);

        User user = new User();
        user.setId(userId);

        ProductTranslation translation = new ProductTranslation();
        translation.setId(translationId);
        translation.setCode("en");
        translation.setTitle("Product title");
        translation.setDescription("Description");
        translation.setProduct(product);
        translation.setCreatedBy(user);

        ProductTranslationResponse response = new ProductTranslationResponse(
                translationId,
                "Ապրանք",
                "Հայերեն նկարագրություն",
                "hy",
                productId,
                userId,
                now,
                now
        );

        when(productTranslationRepository.findById(translationId))
                .thenReturn(Optional.of(translation));
        when(languageRepository.existsByCodeIgnoreCase("hy"))
                .thenReturn(true);
        when(productTranslationRepository.existsByProductIdAndCodeIgnoreCaseAndIdNot(
                productId,
                "hy",
                translationId
        )).thenReturn(false);
        when(productTranslationRepository.saveAndFlush(translation))
                .thenReturn(translation);
        when(productTranslationMapper.toResponse(translation))
                .thenReturn(response);

        ProductTranslationResponse result = productTranslationService.update(translationId, request);

        assertNotNull(result);
        assertEquals(response, result);
        assertEquals("hy", translation.getCode());
        assertEquals("Ապրանք", translation.getTitle());
        assertEquals("Հայերեն նկարագրություն", translation.getDescription());

        verify(productTranslationRepository).findById(translationId);
        verify(languageRepository).existsByCodeIgnoreCase("hy");
        verify(productTranslationRepository).existsByProductIdAndCodeIgnoreCaseAndIdNot(
                productId,
                "hy",
                translationId
        );
        verify(productTranslationRepository).saveAndFlush(translation);
        verify(productTranslationMapper).toResponse(translation);
    }

    @Test
    void update_shouldThrowBadRequest_whenNoFieldsProvided() {
        UUID translationId = UUID.randomUUID();

        UpdateProductTranslationRequest request = updateRequest(
                null,
                null,
                null
        );

        ProductTranslation translation = new ProductTranslation();
        translation.setId(translationId);

        when(productTranslationRepository.findById(translationId))
                .thenReturn(Optional.of(translation));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> productTranslationService.update(translationId, request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("400 BAD_REQUEST \"At least one field must be provided\"", exception.getMessage());

        verify(productTranslationRepository).findById(translationId);
        verify(productTranslationRepository, never()).saveAndFlush(any());
    }

    @Test
    void update_shouldThrowConflict_whenCodeAlreadyExistsForProduct() {
        UUID productId = UUID.randomUUID();
        UUID translationId = UUID.randomUUID();

        UpdateProductTranslationRequest request = updateRequest(
                "hy",
                null,
                null
        );

        Product product = new Product();
        product.setId(productId);

        ProductTranslation translation = new ProductTranslation();
        translation.setId(translationId);
        translation.setProduct(product);

        when(productTranslationRepository.findById(translationId))
                .thenReturn(Optional.of(translation));
        when(languageRepository.existsByCodeIgnoreCase("hy"))
                .thenReturn(true);
        when(productTranslationRepository.existsByProductIdAndCodeIgnoreCaseAndIdNot(
                productId,
                "hy",
                translationId
        )).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> productTranslationService.update(translationId, request)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals(
                "409 CONFLICT \"Product translation with this code already exists for this product\"",
                exception.getMessage()
        );

        verify(productTranslationRepository).existsByProductIdAndCodeIgnoreCaseAndIdNot(
                productId,
                "hy",
                translationId
        );
        verify(productTranslationRepository, never()).saveAndFlush(any());
    }

    @Test
    void update_shouldThrowConflict_whenSaveFailsByUniqueConstraint() {
        UUID productId = UUID.randomUUID();
        UUID translationId = UUID.randomUUID();

        UpdateProductTranslationRequest request = updateRequest(
                "hy",
                "Armenian title",
                null
        );

        Product product = new Product();
        product.setId(productId);

        ProductTranslation translation = new ProductTranslation();
        translation.setId(translationId);
        translation.setProduct(product);

        when(productTranslationRepository.findById(translationId))
                .thenReturn(Optional.of(translation));
        when(languageRepository.existsByCodeIgnoreCase("hy"))
                .thenReturn(true);
        when(productTranslationRepository.existsByProductIdAndCodeIgnoreCaseAndIdNot(
                productId,
                "hy",
                translationId
        )).thenReturn(false);
        when(productTranslationRepository.saveAndFlush(translation))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> productTranslationService.update(translationId, request)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals(
                "409 CONFLICT \"Product translation with this code already exists for this product\"",
                exception.getMessage()
        );

        verify(productTranslationRepository).saveAndFlush(translation);
        verify(productTranslationMapper, never()).toResponse(any());
    }

    @Test
    void deleteProductTranslation_shouldDelete() {
        UUID translationId = UUID.randomUUID();

        ProductTranslation translation = new ProductTranslation();
        translation.setId(translationId);

        when(productTranslationRepository.findById(translationId))
                .thenReturn(Optional.of(translation));

        productTranslationService.delete(translationId);

        verify(productTranslationRepository).findById(translationId);
        verify(productTranslationRepository).delete(translation);
    }

    @Test
    void deleteProductTranslation_shouldThrowNotFound_whenDoesNotExist() {
        UUID translationId = UUID.randomUUID();

        when(productTranslationRepository.findById(translationId))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> productTranslationService.delete(translationId)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Product translation not found with id"));

        verify(productTranslationRepository).findById(translationId);
        verify(productTranslationRepository, never()).delete(any());
    }
}
