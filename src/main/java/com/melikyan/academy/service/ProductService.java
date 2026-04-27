package com.melikyan.academy.service;

import com.melikyan.academy.entity.User;
import org.springframework.http.HttpStatus;
import com.melikyan.academy.entity.Product;
import com.melikyan.academy.entity.Category;
import com.melikyan.academy.entity.ContentItem;
import com.melikyan.academy.mapper.ProductMapper;
import com.melikyan.academy.entity.enums.ProductType;
import com.melikyan.academy.entity.ProductContentItem;
import com.melikyan.academy.repository.UserRepository;
import com.melikyan.academy.repository.ProductRepository;
import com.melikyan.academy.repository.CategoryRepository;
import com.melikyan.academy.repository.ContentItemRepository;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.dao.DataIntegrityViolationException;
import com.melikyan.academy.dto.response.product.ProductResponse;
import com.melikyan.academy.repository.ProductContentItemRepository;
import com.melikyan.academy.dto.request.product.CreateProductRequest;
import com.melikyan.academy.dto.request.product.UpdateProductRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Objects;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;
import java.util.function.Function;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductService {
    private final ProductMapper productMapper;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ContentItemRepository contentItemRepository;
    private final ProductContentItemRepository productContentItemRepository;

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeTitle(String title, ProductType type, ContentItem contentItem) {
        String normalizedTitle = trimToNull(title);

        if (type == ProductType.PACKAGE) {
            if (normalizedTitle == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Product title must not be blank for package"
                );
            }
            return normalizedTitle;
        }

        if (type == ProductType.SINGLE) {
            if (normalizedTitle != null) {
                return normalizedTitle;
            }

            String contentItemTitle = contentItem == null ? null : trimToNull(contentItem.getTitle());
            if (contentItemTitle == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Content item title must not be blank for single product"
                );
            }

            return contentItemTitle;
        }

        return normalizedTitle;
    }

    private String normalizeDescription(String description, ProductType type, ContentItem contentItem) {
        String normalizedDescription = trimToNull(description);

        if (type == ProductType.SINGLE) {
            if (normalizedDescription != null) {
                return normalizedDescription;
            }

            return contentItem == null ? null : trimToNull(contentItem.getDescription());
        }

        return normalizedDescription;
    }

    private void validateTitleUnique(String title) {
        if (title == null) {
            return;
        }

        if (productRepository.existsByTitleIgnoreCase(title)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Product with title '" + title + "' already exists"
            );
        }
    }

    private void validateTitleUniqueForUpdate(UUID productId, String title) {
        if (title == null) {
            return;
        }

        Product existingProduct = productRepository.findByTitleIgnoreCase(title);
        if (existingProduct != null && !existingProduct.getId().equals(productId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Product with title '" + title + "' already exists"
            );
        }
    }

    private void validateCreateType(ProductType type) {
        if (type == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Product type must not be null"
            );
        }
    }

    private void validateContentItemIds(ProductType type, List<UUID> contentItemIds) {
        if (contentItemIds == null || contentItemIds.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Product must contain at least one content item"
            );
        }

        if (type == ProductType.SINGLE && contentItemIds.size() != 1) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Single product must contain exactly one content item"
            );
        }
    }

    private List<UUID> normalizeContentItemIds(List<UUID> contentItemIds) {
        if (contentItemIds == null || contentItemIds.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Content item ids must not be empty"
            );
        }

        LinkedHashSet<UUID> normalizedIds = new LinkedHashSet<>();

        for (UUID id : contentItemIds) {
            if (id == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Content item id must not be null"
                );
            }

            normalizedIds.add(id);
        }

        return List.copyOf(normalizedIds);
    }

    private Product getProductEntityById(UUID id) {
        return productRepository.findDetailedById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product not found with id: " + id
                ));
    }

    private User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found with id: " + id
                ));
    }

    private Category getCategoryById(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Category not found with id: " + id
                ));
    }

    private List<ContentItem> getContentItemsByIds(List<UUID> ids) {
        List<ContentItem> contentItems = contentItemRepository.findAllById(ids);

        Map<UUID, ContentItem> contentItemMap = contentItems.stream()
                .collect(Collectors.toMap(ContentItem::getId, Function.identity()));

        List<UUID> missingIds = ids.stream()
                .filter(id -> !contentItemMap.containsKey(id))
                .toList();

        if (!missingIds.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Content item(s) not found: " + missingIds
            );
        }

        return ids.stream()
                .map(contentItemMap::get)
                .toList();
    }

    private List<UUID> getCurrentContentItemIds(Product product) {
        if (product.getContentItems() == null || product.getContentItems().isEmpty()) {
            return List.of();
        }

        return product.getContentItems().stream()
                .map(ProductContentItem::getContentItem)
                .filter(Objects::nonNull)
                .map(ContentItem::getId)
                .toList();
    }

    private List<ProductContentItem> buildProductContentItems(Product product, List<ContentItem> contentItems) {
        return contentItems.stream()
                .map(contentItem -> {
                    ProductContentItem productContentItem = new ProductContentItem();
                    productContentItem.setProduct(product);
                    productContentItem.setContentItem(contentItem);
                    return productContentItem;
                })
                .toList();
    }

    public ProductResponse create(CreateProductRequest request) {
        validateCreateType(request.type());

        List<UUID> normalizedContentItemIds = normalizeContentItemIds(request.contentItemIds());
        validateContentItemIds(request.type(), normalizedContentItemIds);

        List<ContentItem> contentItems = getContentItemsByIds(normalizedContentItemIds);
        ContentItem primaryContentItem = contentItems.getFirst();

        String finalTitle = normalizeTitle(request.title(), request.type(), primaryContentItem);
        String finalDescription = normalizeDescription(request.description(), request.type(), primaryContentItem);

        validateTitleUnique(finalTitle);

        User createdBy = getUserById(request.createdById());
        Category category = getCategoryById(request.categoryId());

        Product product = productMapper.toEntity(request);
        product.setType(request.type());
        product.setTitle(finalTitle);
        product.setDescription(finalDescription);
        product.setCategory(category);
        product.setCreatedBy(createdBy);

        try {
            Product savedProduct = productRepository.saveAndFlush(product);

            List<ProductContentItem> productContentItems = buildProductContentItems(savedProduct, contentItems);
            productContentItemRepository.saveAllAndFlush(productContentItems);

            savedProduct.setContentItems(productContentItems);

            return productMapper.toResponse(savedProduct);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Product with this title already exists or contains duplicate content item",
                    exception

            );
        }
    }

    @Transactional(readOnly = true)
    public ProductResponse getById(UUID id) {
        Product product = getProductEntityById(id);
        return productMapper.toResponse(product);
    }
    @Transactional(readOnly = true)
    public List<ProductResponse> getAll() {
        List<Product> products = productRepository.findAllBy();
        return productMapper.toResponseList(products);
    }
    public ProductResponse update(UUID id, UpdateProductRequest request) {
        Product product = getProductEntityById(id);

        List<UUID> currentContentItemIds = getCurrentContentItemIds(product);

        ProductType actualType = request.type() != null
                ? request.type()
                : product.getType();

        List<UUID> actualContentItemIds = request.contentItemIds() != null
                ? normalizeContentItemIds(request.contentItemIds())
                : currentContentItemIds;

        validateContentItemIds(actualType, actualContentItemIds);

        List<ContentItem> contentItems = getContentItemsByIds(actualContentItemIds);
        ContentItem primaryContentItem = contentItems.getFirst();

        String finalTitle;
        String finalDescription;

        if (actualType == ProductType.SINGLE) {
            if (request.title() != null || request.contentItemIds() != null) {
                finalTitle = normalizeTitle(request.title(), actualType, primaryContentItem);
            } else {
                finalTitle = normalizeTitle(product.getTitle(), actualType, primaryContentItem);
            }

            if (request.description() != null || request.contentItemIds() != null) {
                finalDescription = normalizeDescription(request.description(), actualType, primaryContentItem);
            } else {
                finalDescription = normalizeDescription(product.getDescription(), actualType, primaryContentItem);
            }
        } else {
            String rawTitle = request.title() != null
                    ? request.title()
                    : product.getTitle();

            String rawDescription = request.description() != null
                    ? request.description()
                    : product.getDescription();

            finalTitle = normalizeTitle(rawTitle, actualType, primaryContentItem);
            finalDescription = normalizeDescription(rawDescription, actualType, primaryContentItem);
        }

        validateTitleUniqueForUpdate(product.getId(), finalTitle);

        productMapper.updateEntityFromRequest(request, product);

        product.setType(actualType);
        product.setTitle(finalTitle);
        product.setDescription(finalDescription);

        if (request.categoryId() != null) {
            Category category = getCategoryById(request.categoryId());
            product.setCategory(category);
        }

        try {
            Product savedProduct = productRepository.saveAndFlush(product);

            if (request.contentItemIds() != null && !currentContentItemIds.equals(actualContentItemIds)) {
                productContentItemRepository.deleteAllByProduct_Id(savedProduct.getId());
                productContentItemRepository.flush();

                List<ProductContentItem> productContentItems =
                        buildProductContentItems(savedProduct, contentItems);

                productContentItemRepository.saveAllAndFlush(productContentItems);
            }

            Product detailedProduct = getProductEntityById(savedProduct.getId());
            return productMapper.toResponse(detailedProduct);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Product with this title already exists or contains duplicate content item",
                    exception
            );
        }
    }

    public void delete(UUID id) {
        getProductEntityById(id);

        productContentItemRepository.deleteAllByProduct_Id(id);
        productRepository.deleteById(id);
    }
}