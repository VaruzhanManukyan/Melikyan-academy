package com.melikyan.academy.service;

import com.melikyan.academy.entity.User;
import org.springframework.http.HttpStatus;
import com.melikyan.academy.entity.Product;
import com.melikyan.academy.entity.ContentItem;
import com.melikyan.academy.entity.Transaction;
import com.melikyan.academy.entity.UserProcess;
import com.melikyan.academy.repository.UserRepository;
import com.melikyan.academy.entity.ProductContentItem;
import com.melikyan.academy.entity.ProductRegistration;
import com.melikyan.academy.entity.enums.TransactionType;
import com.melikyan.academy.repository.ProductRepository;
import com.melikyan.academy.entity.enums.TransactionStatus;
import com.melikyan.academy.entity.enums.RegistrationStatus;
import com.melikyan.academy.mapper.ProductRegistrationMapper;
import com.melikyan.academy.repository.TransactionRepository;
import com.melikyan.academy.repository.UserProcessRepository;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.dao.DataIntegrityViolationException;
import com.melikyan.academy.repository.ProductRegistrationRepository;
import com.melikyan.academy.dto.response.productRegistration.ProductRegistrationResponse;
import com.melikyan.academy.dto.request.productRegistration.CreateProductRegistrationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductRegistrationService {
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final TransactionRepository transactionRepository;
    private final UserProcessRepository userProcessRepository;
    private final ProductRegistrationMapper productRegistrationMapper;
    private final ProductRegistrationRepository productRegistrationRepository;

    private ProductRegistration getProductRegistrationEntityById(UUID id) {
        return productRegistrationRepository.findDetailedById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product registration not found with id: " + id
                ));
    }

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

    private Product getProductById(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product not found with id: " + id
                ));
    }

    private Transaction getTransactionById(UUID id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Transaction not found with id: " + id
                ));
    }

    private void validateNoActiveRegistration(UUID userId, UUID productId) {
        if (productRegistrationRepository.existsByUserIdAndProductIdAndStatus(
                userId,
                productId,
                RegistrationStatus.ACTIVE
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "User already has an active registration for this product"
            );
        }
    }

    private Transaction validateAndGetSuccessfulTransaction(
            UUID transactionId,
            UUID userId,
            UUID productId
    ) {
        Transaction transaction = getTransactionById(transactionId);

        if (transaction.getUser() == null || !transaction.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Transaction does not belong to the specified user"
            );
        }

        if (transaction.getProduct() == null || !transaction.getProduct().getId().equals(productId)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Transaction does not belong to the specified product"
            );
        }

        if (transaction.getTransactionType() != TransactionType.PAYMENT) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Transaction must be a payment"
            );
        }

        if (transaction.getStatus() != TransactionStatus.SUCCESS) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Transaction is not successful"
            );
        }

        return transaction;
    }

    private ProductRegistration saveRegistration(User user, Product product, Transaction transaction) {
        for (ProductContentItem productContentItem : product.getContentItems()) {
            ContentItem contentItem = productContentItem.getContentItem();

            if (!userProcessRepository.existsByUserIdAndContentItemId(user.getId(), contentItem.getId())) {
                UserProcess userProcess = new UserProcess();
                userProcess.setUser(user);
                userProcess.setContentItem(contentItem);
                userProcess.setCurrentStep(0);
                userProcess.setScoreAccumulated(BigDecimal.ZERO);
                userProcess.setLastAccessedAt(OffsetDateTime.now());

                userProcessRepository.save(userProcess);
            }
        }

        ProductRegistration productRegistration = new ProductRegistration();
        productRegistration.setStatus(RegistrationStatus.ACTIVE);
        productRegistration.setUser(user);
        productRegistration.setProduct(product);
        productRegistration.setTransaction(transaction);

        try {
            return productRegistrationRepository.saveAndFlush(productRegistration);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "User already has an active registration for this product",
                    exception
            );
        }
    }

    public ProductRegistrationResponse grantAccess(CreateProductRegistrationRequest request) {
        User user = getUserById(request.userId());
        Product product = getProductById(request.productId());

        validateNoActiveRegistration(user.getId(), product.getId());

        Transaction transaction = null;
        if (request.transactionId() != null) {
            transaction = validateAndGetSuccessfulTransaction(
                    request.transactionId(),
                    user.getId(),
                    product.getId()
            );
        }

        ProductRegistration savedProductRegistration = saveRegistration(user, product, transaction);
        return productRegistrationMapper.toResponse(savedProductRegistration);
    }

    public ProductRegistrationResponse createAfterSuccessfulPayment(
            UUID userId,
            UUID productId,
            UUID transactionId
    ) {
        User user = getUserById(userId);
        Product product = getProductById(productId);

        validateNoActiveRegistration(user.getId(), product.getId());

        Transaction transaction = validateAndGetSuccessfulTransaction(
                transactionId,
                user.getId(),
                product.getId()
        );

        ProductRegistration savedProductRegistration = saveRegistration(user, product, transaction);
        return productRegistrationMapper.toResponse(savedProductRegistration);
    }

    @Transactional(readOnly = true)
    public ProductRegistrationResponse getById(UUID id) {
        ProductRegistration productRegistration = getProductRegistrationEntityById(id);
        return productRegistrationMapper.toResponse(productRegistration);
    }

    @Transactional(readOnly = true)
    public List<ProductRegistrationResponse> getByUserId(UUID userId) {
        List<ProductRegistration> productRegistrations = productRegistrationRepository.findAllByUserId(userId);
        return productRegistrationMapper.toResponseList(productRegistrations);
    }

    @Transactional(readOnly = true)
    public List<ProductRegistrationResponse> getByProductId(UUID productId) {
        List<ProductRegistration> productRegistrations = productRegistrationRepository.findAllByProductId(productId);
        return productRegistrationMapper.toResponseList(productRegistrations);
    }

    @Transactional(readOnly = true)
    public List<ProductRegistrationResponse> getMyRegistrations(String email) {
        User user = getUserByEmail(email);
        List<ProductRegistration> productRegistrations = productRegistrationRepository.findAllByUserId(user.getId());
        return productRegistrationMapper.toResponseList(productRegistrations);
    }

    @Transactional(readOnly = true)
    public ProductRegistrationResponse getMyRegistrationById(UUID id, String email) {
        User user = getUserByEmail(email);
        ProductRegistration productRegistration = getProductRegistrationEntityById(id);

        if (!productRegistration.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You do not have access to this product registration"
            );
        }

        return productRegistrationMapper.toResponse(productRegistration);
    }

    private ProductRegistrationResponse updateStatus(UUID id, RegistrationStatus status) {
        ProductRegistration productRegistration = getProductRegistrationEntityById(id);
        productRegistration.setStatus(status);

        try {
            ProductRegistration savedProductRegistration = productRegistrationRepository.saveAndFlush(productRegistration);

            return productRegistrationMapper.toResponse(savedProductRegistration);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "User already has an active registration for this product",
                    exception
            );
        }
    }

    public ProductRegistrationResponse activate(UUID id) {
        return updateStatus(id, RegistrationStatus.ACTIVE);
    }

    public ProductRegistrationResponse suspend(UUID id) {
        return updateStatus(id, RegistrationStatus.SUSPENDED);
    }

    public ProductRegistrationResponse expire(UUID id) {
        return updateStatus(id, RegistrationStatus.EXPIRED);
    }
}