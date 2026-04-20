package com.melikyan.academy.service;

import org.mockito.ArgumentCaptor;
import com.melikyan.academy.entity.User;
import com.melikyan.academy.entity.Product;
import com.melikyan.academy.entity.Category;
import com.melikyan.academy.entity.ContentItem;
import com.melikyan.academy.mapper.ProductMapper;
import org.mockito.junit.jupiter.MockitoExtension;
import com.melikyan.academy.entity.enums.ProductType;
import com.melikyan.academy.entity.ProductContentItem;
import com.melikyan.academy.repository.UserRepository;
import com.melikyan.academy.repository.ProductRepository;
import com.melikyan.academy.repository.CategoryRepository;
import com.melikyan.academy.repository.ContentItemRepository;
import org.springframework.web.server.ResponseStatusException;
import com.melikyan.academy.dto.response.product.ProductResponse;
import com.melikyan.academy.repository.ProductContentItemRepository;
import com.melikyan.academy.dto.request.product.CreateProductRequest;
import com.melikyan.academy.dto.request.product.UpdateProductRequest;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.anyList;
import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductMapper productMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ContentItemRepository contentItemRepository;

    @Mock
    private ProductContentItemRepository productContentItemRepository;

    @InjectMocks
    private ProductService productService;

    private UUID productId;
    private UUID userId;
    private UUID categoryId;
    private UUID firstContentItemId;
    private UUID secondContentItemId;

    private User user;
    private Category category;
    private ContentItem firstContentItem;
    private ContentItem secondContentItem;
    private Product product;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        userId = UUID.randomUUID();
        categoryId = UUID.randomUUID();
        firstContentItemId = UUID.randomUUID();
        secondContentItemId = UUID.randomUUID();

        user = new User();
        user.setId(userId);

        category = new Category();
        category.setId(categoryId);
        category.setTitle("Backend");

        firstContentItem = new ContentItem();
        firstContentItem.setId(firstContentItemId);
        firstContentItem.setTitle("Spring Boot Course");
        firstContentItem.setDescription("Learn Spring Boot");

        secondContentItem = new ContentItem();
        secondContentItem.setId(secondContentItemId);
        secondContentItem.setTitle("Hibernate Course");
        secondContentItem.setDescription("Learn Hibernate");

        product = new Product();
        product.setId(productId);
        product.setType(ProductType.PACKAGE);
        product.setTitle("Java Package");
        product.setDescription("Backend package");
        product.setPrice(new BigDecimal("149.99"));
        product.setIsPrivate(false);
        product.setCategory(category);
        product.setCreatedBy(user);

        attachContentItems(product, firstContentItem);
    }

    private void attachContentItems(Product product, ContentItem... items) {
        List<ProductContentItem> links = new ArrayList<>();

        for (ContentItem item : items) {
            ProductContentItem link = new ProductContentItem();
            link.setProduct(product);
            link.setContentItem(item);
            links.add(link);
        }

        product.setContentItems(links);
    }

    @Test
    @DisplayName("create -> saves product and returns response")
    void create_ShouldSaveProductAndReturnResponse() {
        CreateProductRequest request = mock(CreateProductRequest.class);
        ProductResponse response = mock(ProductResponse.class);

        Product mappedProduct = new Product();
        mappedProduct.setType(ProductType.PACKAGE);
        mappedProduct.setPrice(new BigDecimal("149.99"));
        mappedProduct.setIsPrivate(false);

        when(request.type()).thenReturn(ProductType.PACKAGE);
        when(request.title()).thenReturn("  Java Backend Package  ");
        when(request.description()).thenReturn("  Spring Boot package  ");
        when(request.categoryId()).thenReturn(categoryId);
        when(request.createdById()).thenReturn(userId);
        when(request.contentItemIds()).thenReturn(List.of(firstContentItemId, secondContentItemId));

        when(contentItemRepository.findAllById(List.of(firstContentItemId, secondContentItemId)))
                .thenReturn(List.of(firstContentItem, secondContentItem));
        when(productRepository.existsByTitleIgnoreCase("Java Backend Package")).thenReturn(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(productMapper.toEntity(request)).thenReturn(mappedProduct);
        when(productRepository.saveAndFlush(any(Product.class))).thenAnswer(invocation -> {
            Product saved = invocation.getArgument(0);
            saved.setId(productId);
            return saved;
        });
        when(productRepository.findDetailedById(productId)).thenReturn(Optional.of(mappedProduct));
        when(productMapper.toResponse(mappedProduct)).thenReturn(response);

        ProductResponse result = productService.create(request);

        assertNotNull(result);
        assertEquals(response, result);

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).saveAndFlush(productCaptor.capture());

        Product savedProduct = productCaptor.getValue();
        assertEquals("Java Backend Package", savedProduct.getTitle());
        assertEquals("Spring Boot package", savedProduct.getDescription());
        assertEquals(ProductType.PACKAGE, savedProduct.getType());
        assertEquals(new BigDecimal("149.99"), savedProduct.getPrice());
        assertEquals(false, savedProduct.getIsPrivate());
        assertEquals(category, savedProduct.getCategory());
        assertEquals(user, savedProduct.getCreatedBy());

        verify(productContentItemRepository).saveAllAndFlush(anyList());
    }

    @Test
    @DisplayName("create -> single: inherits title and description from content item when both are null")
    void create_ShouldInheritTitleAndDescription_WhenSingleTypeAndNullTitleAndDescription() {
        CreateProductRequest request = mock(CreateProductRequest.class);
        ProductResponse response = mock(ProductResponse.class);

        Product mappedProduct = new Product();
        mappedProduct.setType(ProductType.SINGLE);
        mappedProduct.setPrice(new BigDecimal("49.99"));
        mappedProduct.setIsPrivate(false);

        when(request.type()).thenReturn(ProductType.SINGLE);
        when(request.title()).thenReturn(null);
        when(request.description()).thenReturn(null);
        when(request.categoryId()).thenReturn(categoryId);
        when(request.createdById()).thenReturn(userId);
        when(request.contentItemIds()).thenReturn(List.of(firstContentItemId));

        when(contentItemRepository.findAllById(List.of(firstContentItemId)))
                .thenReturn(List.of(firstContentItem));
        when(productRepository.existsByTitleIgnoreCase("Spring Boot Course")).thenReturn(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(productMapper.toEntity(request)).thenReturn(mappedProduct);
        when(productRepository.saveAndFlush(any(Product.class))).thenAnswer(invocation -> {
            Product saved = invocation.getArgument(0);
            saved.setId(productId);
            return saved;
        });
        when(productRepository.findDetailedById(productId)).thenReturn(Optional.of(mappedProduct));
        when(productMapper.toResponse(mappedProduct)).thenReturn(response);

        ProductResponse result = productService.create(request);

        assertNotNull(result);
        assertEquals(response, result);

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).saveAndFlush(productCaptor.capture());

        Product savedProduct = productCaptor.getValue();
        assertEquals("Spring Boot Course", savedProduct.getTitle());
        assertEquals("Learn Spring Boot", savedProduct.getDescription());
    }

    @Test
    @DisplayName("create -> single: explicit title and description override content item values")
    void create_ShouldKeepExplicitTitleAndDescription_WhenSingleTypeAndBothProvided() {
        CreateProductRequest request = mock(CreateProductRequest.class);
        ProductResponse response = mock(ProductResponse.class);

        Product mappedProduct = new Product();
        mappedProduct.setType(ProductType.SINGLE);
        mappedProduct.setPrice(new BigDecimal("49.99"));
        mappedProduct.setIsPrivate(false);

        when(request.type()).thenReturn(ProductType.SINGLE);
        when(request.title()).thenReturn("  Custom Title  ");
        when(request.description()).thenReturn("  Custom Desc  ");
        when(request.categoryId()).thenReturn(categoryId);
        when(request.createdById()).thenReturn(userId);
        when(request.contentItemIds()).thenReturn(List.of(firstContentItemId));

        when(contentItemRepository.findAllById(List.of(firstContentItemId)))
                .thenReturn(List.of(firstContentItem));
        when(productRepository.existsByTitleIgnoreCase("Custom Title")).thenReturn(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(productMapper.toEntity(request)).thenReturn(mappedProduct);
        when(productRepository.saveAndFlush(any(Product.class))).thenAnswer(invocation -> {
            Product saved = invocation.getArgument(0);
            saved.setId(productId);
            return saved;
        });
        when(productRepository.findDetailedById(productId)).thenReturn(Optional.of(mappedProduct));
        when(productMapper.toResponse(mappedProduct)).thenReturn(response);

        ProductResponse result = productService.create(request);

        assertNotNull(result);
        assertEquals(response, result);

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).saveAndFlush(productCaptor.capture());

        Product savedProduct = productCaptor.getValue();
        assertEquals("Custom Title", savedProduct.getTitle());
        assertEquals("Custom Desc", savedProduct.getDescription());
    }

    @Test
    @DisplayName("create -> throws bad request when single product contains more than one content item")
    void create_ShouldThrowBadRequest_WhenSingleContainsMoreThanOneContentItem() {
        CreateProductRequest request = mock(CreateProductRequest.class);

        when(request.type()).thenReturn(ProductType.SINGLE);
        when(request.contentItemIds()).thenReturn(List.of(firstContentItemId, secondContentItemId));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> productService.create(request)
        );

        assertEquals(400, ex.getStatusCode().value());
        verify(productRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("create -> throws bad request when package title is blank")
    void create_ShouldThrowBadRequest_WhenPackageTitleIsBlank() {
        CreateProductRequest request = mock(CreateProductRequest.class);

        when(request.type()).thenReturn(ProductType.PACKAGE);
        when(request.title()).thenReturn("   ");
        when(request.contentItemIds()).thenReturn(List.of(firstContentItemId));
        when(contentItemRepository.findAllById(List.of(firstContentItemId)))
                .thenReturn(List.of(firstContentItem));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> productService.create(request)
        );

        assertEquals(400, ex.getStatusCode().value());
        verify(productRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("getById -> returns mapped response")
    void getById_ShouldReturnMappedResponse() {
        ProductResponse response = mock(ProductResponse.class);

        when(productRepository.findDetailedById(productId)).thenReturn(Optional.of(product));
        when(productMapper.toResponse(product)).thenReturn(response);

        ProductResponse result = productService.getById(productId);

        assertEquals(response, result);
        verify(productRepository).findDetailedById(productId);
        verify(productMapper).toResponse(product);
    }

    @Test
    @DisplayName("getAll -> returns mapped list")
    void getAll_ShouldReturnMappedList() {
        Product first = new Product();
        Product second = new Product();

        ProductResponse firstResponse = mock(ProductResponse.class);
        ProductResponse secondResponse = mock(ProductResponse.class);

        when(productRepository.findAllBy()).thenReturn(List.of(first, second));
        when(productMapper.toResponseList(List.of(first, second)))
                .thenReturn(List.of(firstResponse, secondResponse));

        List<ProductResponse> result = productService.getAll();

        assertEquals(2, result.size());
        verify(productRepository).findAllBy();
        verify(productMapper).toResponseList(List.of(first, second));
    }

    @Test
    @DisplayName("update -> updates provided fields and returns response")
    void update_ShouldUpdateProductAndReturnResponse() {
        UUID newCategoryId = UUID.randomUUID();

        Category newCategory = new Category();
        newCategory.setId(newCategoryId);
        newCategory.setTitle("Updated Backend");

        UpdateProductRequest request = mock(UpdateProductRequest.class);
        ProductResponse response = mock(ProductResponse.class);

        when(request.type()).thenReturn(ProductType.PACKAGE);
        when(request.title()).thenReturn("  Updated Java Package  ");
        when(request.description()).thenReturn("  Updated description  ");
        when(request.categoryId()).thenReturn(newCategoryId);
        when(request.contentItemIds()).thenReturn(List.of(secondContentItemId));

        when(productRepository.findDetailedById(productId))
                .thenReturn(Optional.of(product), Optional.of(product));
        when(contentItemRepository.findAllById(List.of(secondContentItemId)))
                .thenReturn(List.of(secondContentItem));
        when(productRepository.findByTitleIgnoreCase("Updated Java Package"))
                .thenReturn(product);
        when(categoryRepository.findById(newCategoryId)).thenReturn(Optional.of(newCategory));
        when(productRepository.saveAndFlush(any(Product.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(productMapper.toResponse(product)).thenReturn(response);

        ProductResponse result = productService.update(productId, request);

        assertEquals(response, result);
        assertEquals("Updated Java Package", product.getTitle());
        assertEquals("Updated description", product.getDescription());
        assertEquals(newCategory, product.getCategory());
        assertEquals(ProductType.PACKAGE, product.getType());

        verify(productRepository).saveAndFlush(product);
        verify(productContentItemRepository).deleteAllByProduct_Id(productId);
        verify(productContentItemRepository).saveAllAndFlush(anyList());
    }

    @Test
    @DisplayName("update -> single: inherits title and description from content item when both are null")
    void update_ShouldInheritTitleAndDescription_WhenSingleTypeAndNullTitleAndDescription() {
        product.setType(ProductType.SINGLE);

        UpdateProductRequest request = mock(UpdateProductRequest.class);
        ProductResponse response = mock(ProductResponse.class);

        when(request.type()).thenReturn(null);
        when(request.title()).thenReturn(null);
        when(request.description()).thenReturn(null);
        when(request.contentItemIds()).thenReturn(List.of(firstContentItemId));

        when(productRepository.findDetailedById(productId))
                .thenReturn(Optional.of(product), Optional.of(product));
        when(contentItemRepository.findAllById(List.of(firstContentItemId)))
                .thenReturn(List.of(firstContentItem));
        when(productRepository.findByTitleIgnoreCase("Spring Boot Course"))
                .thenReturn(product);
        when(productRepository.saveAndFlush(any(Product.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(productMapper.toResponse(product)).thenReturn(response);

        ProductResponse result = productService.update(productId, request);

        assertEquals(response, result);
        assertEquals("Spring Boot Course", product.getTitle());
        assertEquals("Learn Spring Boot", product.getDescription());

        verify(productContentItemRepository, never()).deleteAllByProduct_Id(any());
        verify(productContentItemRepository, never()).saveAllAndFlush(anyList());
    }

    @Test
    @DisplayName("update -> single: explicit title and description override content item values")
    void update_ShouldKeepExplicitTitleAndDescription_WhenSingleTypeAndBothProvided() {
        product.setType(ProductType.SINGLE);

        UpdateProductRequest request = mock(UpdateProductRequest.class);
        ProductResponse response = mock(ProductResponse.class);

        when(request.type()).thenReturn(null);
        when(request.title()).thenReturn("  My Custom Title  ");
        when(request.description()).thenReturn("  My Custom Desc  ");
        when(request.contentItemIds()).thenReturn(List.of(firstContentItemId));

        when(productRepository.findDetailedById(productId))
                .thenReturn(Optional.of(product), Optional.of(product));
        when(contentItemRepository.findAllById(List.of(firstContentItemId)))
                .thenReturn(List.of(firstContentItem));
        when(productRepository.findByTitleIgnoreCase("My Custom Title"))
                .thenReturn(product);
        when(productRepository.saveAndFlush(any(Product.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(productMapper.toResponse(product)).thenReturn(response);

        ProductResponse result = productService.update(productId, request);

        assertEquals(response, result);
        assertEquals("My Custom Title", product.getTitle());
        assertEquals("My Custom Desc", product.getDescription());

        verify(productContentItemRepository, never()).deleteAllByProduct_Id(any());
        verify(productContentItemRepository, never()).saveAllAndFlush(anyList());
    }

    @Test
    @DisplayName("update -> single: keeps existing title and description when content items are not changed")
    void update_ShouldKeepExistingTitleAndDescription_WhenSingleTypeAndFieldsAreNullAndContentItemsNotChanged() {
        product.setType(ProductType.SINGLE);

        UpdateProductRequest request = mock(UpdateProductRequest.class);
        ProductResponse response = mock(ProductResponse.class);

        when(request.type()).thenReturn(null);
        when(request.title()).thenReturn(null);
        when(request.description()).thenReturn(null);
        when(request.contentItemIds()).thenReturn(null);

        when(productRepository.findDetailedById(productId))
                .thenReturn(Optional.of(product), Optional.of(product));
        when(contentItemRepository.findAllById(List.of(firstContentItemId)))
                .thenReturn(List.of(firstContentItem));
        when(productRepository.findByTitleIgnoreCase("Java Package"))
                .thenReturn(product);
        when(productRepository.saveAndFlush(any(Product.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(productMapper.toResponse(product)).thenReturn(response);

        ProductResponse result = productService.update(productId, request);

        assertEquals(response, result);
        assertEquals("Java Package", product.getTitle());
        assertEquals("Backend package", product.getDescription());

        verify(productContentItemRepository, never()).deleteAllByProduct_Id(any());
        verify(productContentItemRepository, never()).saveAllAndFlush(anyList());
    }

    @Test
    @DisplayName("update -> throws bad request when package title is blank")
    void update_ShouldThrowBadRequest_WhenPackageTitleIsBlank() {
        UpdateProductRequest request = mock(UpdateProductRequest.class);

        when(productRepository.findDetailedById(productId)).thenReturn(Optional.of(product));
        when(request.type()).thenReturn(ProductType.PACKAGE);
        when(request.title()).thenReturn("   ");
        when(request.contentItemIds()).thenReturn(List.of(firstContentItemId));
        when(contentItemRepository.findAllById(List.of(firstContentItemId)))
                .thenReturn(List.of(firstContentItem));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> productService.update(productId, request)
        );

        assertEquals(400, ex.getStatusCode().value());
        verify(productRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("delete -> deletes product relations first and then deletes product by id")
    void delete_ShouldDeleteProductRelationsFirstAndThenDeleteProductById() {
        when(productRepository.findDetailedById(productId)).thenReturn(Optional.of(product));

        productService.delete(productId);

        verify(productContentItemRepository).deleteAllByProduct_Id(productId);
        verify(productRepository).deleteById(productId);
    }
}