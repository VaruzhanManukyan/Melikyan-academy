package com.melikyan.academy.service;

import com.melikyan.academy.entity.User;
import org.springframework.http.HttpStatus;
import com.melikyan.academy.entity.Product;
import com.melikyan.academy.entity.Transaction;
import com.melikyan.academy.repository.UserRepository;
import com.melikyan.academy.entity.ProductRegistration;
import com.melikyan.academy.repository.ProductRepository;
import com.melikyan.academy.entity.enums.RegistrationStatus;
import com.melikyan.academy.repository.TransactionRepository;
import com.melikyan.academy.mapper.ProductRegistrationMapper;
import org.springframework.web.server.ResponseStatusException;
import com.melikyan.academy.repository.ProductRegistrationRepository;
import com.melikyan.academy.dto.response.productRegistration.ProductRegistrationResponse;
import com.melikyan.academy.dto.request.productRegistration.CreateProductRegistrationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductRegistrationService {
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final TransactionRepository transactionRepository;
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
        if (productRegistrationRepository.existsByUserIdAndProductId(userId, productId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "User already has this product"
            );
        }
    }

    private ProductRegistration saveRegistration(User user, Product product, Transaction transaction) {
        ProductRegistration productRegistration = new ProductRegistration();
        productRegistration.setStatus(RegistrationStatus.ACTIVE);
        productRegistration.setUser(user);
        productRegistration.setProduct(product);
        productRegistration.setTransaction(transaction);

        return productRegistrationRepository.saveAndFlush(productRegistration);
    }

    public ProductRegistrationResponse grantAccess(CreateProductRegistrationRequest request) {
        User user = getUserById(request.userId());
        Product product = getProductById(request.productId());

        validateNoActiveRegistration(user.getId(), product.getId());

        Transaction transaction = null;
        if (request.transactionId() != null) {
            transaction = getTransactionById(request.transactionId());
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
        Transaction transaction = getTransactionById(transactionId);

        validateNoActiveRegistration(user.getId(), product.getId());

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

    public ProductRegistrationResponse activate(UUID id) {
        ProductRegistration productRegistration = getProductRegistrationEntityById(id);
        productRegistration.setStatus(RegistrationStatus.ACTIVE);

        ProductRegistration savedProductRegistration =
                productRegistrationRepository.saveAndFlush(productRegistration);

        return productRegistrationMapper.toResponse(savedProductRegistration);
    }

    public ProductRegistrationResponse suspend(UUID id) {
        ProductRegistration productRegistration = getProductRegistrationEntityById(id);
        productRegistration.setStatus(RegistrationStatus.SUSPENDED);

        ProductRegistration savedProductRegistration =
                productRegistrationRepository.saveAndFlush(productRegistration);

        return productRegistrationMapper.toResponse(savedProductRegistration);
    }

    public ProductRegistrationResponse expire(UUID id) {
        ProductRegistration productRegistration = getProductRegistrationEntityById(id);
        productRegistration.setStatus(RegistrationStatus.EXPIRED);

        ProductRegistration savedProductRegistration =
                productRegistrationRepository.saveAndFlush(productRegistration);

        return productRegistrationMapper.toResponse(savedProductRegistration);
    }
}