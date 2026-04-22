package com.melikyan.academy.service;

import org.mockito.ArgumentCaptor;
import com.melikyan.academy.entity.User;
import com.melikyan.academy.entity.Product;
import com.melikyan.academy.entity.Transaction;
import org.mockito.junit.jupiter.MockitoExtension;
import com.melikyan.academy.repository.UserRepository;
import com.melikyan.academy.entity.ProductRegistration;
import com.melikyan.academy.repository.ProductRepository;
import com.melikyan.academy.entity.enums.TransactionType;
import com.melikyan.academy.entity.enums.TransactionStatus;
import com.melikyan.academy.entity.enums.RegistrationStatus;
import com.melikyan.academy.mapper.ProductRegistrationMapper;
import com.melikyan.academy.repository.TransactionRepository;
import com.melikyan.academy.repository.UserProcessRepository;
import org.springframework.web.server.ResponseStatusException;
import com.melikyan.academy.repository.ProductRegistrationRepository;
import com.melikyan.academy.dto.response.productRegistration.ProductRegistrationResponse;
import com.melikyan.academy.dto.request.productRegistration.CreateProductRegistrationRequest;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class ProductRegistrationServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserProcessRepository userProcessRepository;

    @Mock
    private ProductRegistrationMapper productRegistrationMapper;

    @Mock
    private ProductRegistrationRepository productRegistrationRepository;

    @InjectMocks
    private ProductRegistrationService productRegistrationService;

    private UUID registrationId;
    private UUID userId;
    private UUID secondUserId;
    private UUID productId;
    private UUID secondProductId;
    private UUID transactionId;
    private String email;

    private User user;
    private User secondUser;
    private Product product;
    private Product secondProduct;
    private Transaction transaction;
    private ProductRegistration registration;

    @BeforeEach
    void setUp() {
        registrationId = UUID.randomUUID();
        userId = UUID.randomUUID();
        secondUserId = UUID.randomUUID();
        productId = UUID.randomUUID();
        secondProductId = UUID.randomUUID();
        transactionId = UUID.randomUUID();
        email = "test@test.com";

        user = new User();
        user.setId(userId);
        user.setEmail(email);

        secondUser = new User();
        secondUser.setId(secondUserId);
        secondUser.setEmail("other@test.com");

        product = new Product();
        product.setId(productId);
        product.setContentItems(List.of());

        secondProduct = new Product();
        secondProduct.setId(secondProductId);
        secondProduct.setContentItems(List.of());

        transaction = new Transaction();
        transaction.setId(transactionId);
        transaction.setUser(user);
        transaction.setProduct(product);
        transaction.setTransactionType(TransactionType.PAYMENT);
        transaction.setStatus(TransactionStatus.SUCCESS);

        registration = new ProductRegistration();
        registration.setId(registrationId);
        registration.setUser(user);
        registration.setProduct(product);
        registration.setTransaction(transaction);
        registration.setStatus(RegistrationStatus.ACTIVE);
    }

    @Test
    @DisplayName("grantAccess -> saves active registration and returns response when transaction is provided")
    void grantAccess_ShouldSaveActiveRegistrationAndReturnResponse_WhenTransactionProvided() {
        CreateProductRegistrationRequest request = mock(CreateProductRegistrationRequest.class);
        ProductRegistrationResponse response = mock(ProductRegistrationResponse.class);

        when(request.userId()).thenReturn(userId);
        when(request.productId()).thenReturn(productId);
        when(request.transactionId()).thenReturn(transactionId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));
        when(productRegistrationRepository.existsByUserIdAndProductIdAndStatus(
                userId,
                productId,
                RegistrationStatus.ACTIVE
        )).thenReturn(false);
        when(productRegistrationRepository.saveAndFlush(any(ProductRegistration.class))).thenAnswer(invocation -> {
            ProductRegistration saved = invocation.getArgument(0);
            saved.setId(registrationId);
            return saved;
        });
        when(productRegistrationMapper.toResponse(any(ProductRegistration.class))).thenReturn(response);

        ProductRegistrationResponse result = productRegistrationService.grantAccess(request);

        assertNotNull(result);
        assertEquals(response, result);

        ArgumentCaptor<ProductRegistration> captor = ArgumentCaptor.forClass(ProductRegistration.class);
        verify(productRegistrationRepository).saveAndFlush(captor.capture());

        ProductRegistration savedRegistration = captor.getValue();
        assertEquals(RegistrationStatus.ACTIVE, savedRegistration.getStatus());
        assertEquals(user, savedRegistration.getUser());
        assertEquals(product, savedRegistration.getProduct());
        assertEquals(transaction, savedRegistration.getTransaction());
    }

    @Test
    @DisplayName("grantAccess -> saves active registration and returns response when transaction is null")
    void grantAccess_ShouldSaveActiveRegistrationAndReturnResponse_WhenTransactionIsNull() {
        CreateProductRegistrationRequest request = mock(CreateProductRegistrationRequest.class);
        ProductRegistrationResponse response = mock(ProductRegistrationResponse.class);

        when(request.userId()).thenReturn(userId);
        when(request.productId()).thenReturn(productId);
        when(request.transactionId()).thenReturn(null);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRegistrationRepository.existsByUserIdAndProductIdAndStatus(
                userId,
                productId,
                RegistrationStatus.ACTIVE
        )).thenReturn(false);
        when(productRegistrationRepository.saveAndFlush(any(ProductRegistration.class))).thenAnswer(invocation -> {
            ProductRegistration saved = invocation.getArgument(0);
            saved.setId(registrationId);
            return saved;
        });
        when(productRegistrationMapper.toResponse(any(ProductRegistration.class))).thenReturn(response);

        ProductRegistrationResponse result = productRegistrationService.grantAccess(request);

        assertNotNull(result);
        assertEquals(response, result);

        ArgumentCaptor<ProductRegistration> captor = ArgumentCaptor.forClass(ProductRegistration.class);
        verify(productRegistrationRepository).saveAndFlush(captor.capture());

        ProductRegistration savedRegistration = captor.getValue();
        assertEquals(RegistrationStatus.ACTIVE, savedRegistration.getStatus());
        assertEquals(user, savedRegistration.getUser());
        assertEquals(product, savedRegistration.getProduct());
        assertNull(savedRegistration.getTransaction());

        verify(transactionRepository, never()).findById(any());
    }

    @Test
    @DisplayName("grantAccess -> throws conflict when user already has active registration")
    void grantAccess_ShouldThrowConflict_WhenUserAlreadyHasActiveRegistration() {
        CreateProductRegistrationRequest request = mock(CreateProductRegistrationRequest.class);

        when(request.userId()).thenReturn(userId);
        when(request.productId()).thenReturn(productId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRegistrationRepository.existsByUserIdAndProductIdAndStatus(
                userId,
                productId,
                RegistrationStatus.ACTIVE
        )).thenReturn(true);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> productRegistrationService.grantAccess(request)
        );

        assertEquals(409, ex.getStatusCode().value());
        assertEquals("User already has an active registration for this product", ex.getReason());
        verify(productRegistrationRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("grantAccess -> throws bad request when transaction belongs to another user")
    void grantAccess_ShouldThrowBadRequest_WhenTransactionBelongsToAnotherUser() {
        CreateProductRegistrationRequest request = mock(CreateProductRegistrationRequest.class);
        transaction.setUser(secondUser);

        when(request.userId()).thenReturn(userId);
        when(request.productId()).thenReturn(productId);
        when(request.transactionId()).thenReturn(transactionId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));
        when(productRegistrationRepository.existsByUserIdAndProductIdAndStatus(
                userId,
                productId,
                RegistrationStatus.ACTIVE
        )).thenReturn(false);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> productRegistrationService.grantAccess(request)
        );

        assertEquals(400, ex.getStatusCode().value());
        assertEquals("Transaction does not belong to the specified user", ex.getReason());
        verify(productRegistrationRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("grantAccess -> throws bad request when transaction belongs to another product")
    void grantAccess_ShouldThrowBadRequest_WhenTransactionBelongsToAnotherProduct() {
        CreateProductRegistrationRequest request = mock(CreateProductRegistrationRequest.class);
        transaction.setProduct(secondProduct);

        when(request.userId()).thenReturn(userId);
        when(request.productId()).thenReturn(productId);
        when(request.transactionId()).thenReturn(transactionId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));
        when(productRegistrationRepository.existsByUserIdAndProductIdAndStatus(
                userId,
                productId,
                RegistrationStatus.ACTIVE
        )).thenReturn(false);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> productRegistrationService.grantAccess(request)
        );

        assertEquals(400, ex.getStatusCode().value());
        assertEquals("Transaction does not belong to the specified product", ex.getReason());
        verify(productRegistrationRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("grantAccess -> throws bad request when transaction is not payment")
    void grantAccess_ShouldThrowBadRequest_WhenTransactionIsNotPayment() {
        CreateProductRegistrationRequest request = mock(CreateProductRegistrationRequest.class);
        transaction.setTransactionType(TransactionType.REFUND);

        when(request.userId()).thenReturn(userId);
        when(request.productId()).thenReturn(productId);
        when(request.transactionId()).thenReturn(transactionId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));
        when(productRegistrationRepository.existsByUserIdAndProductIdAndStatus(
                userId,
                productId,
                RegistrationStatus.ACTIVE
        )).thenReturn(false);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> productRegistrationService.grantAccess(request)
        );

        assertEquals(400, ex.getStatusCode().value());
        assertEquals("Transaction must be a payment", ex.getReason());
        verify(productRegistrationRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("grantAccess -> throws bad request when transaction is not successful")
    void grantAccess_ShouldThrowBadRequest_WhenTransactionIsNotSuccessful() {
        CreateProductRegistrationRequest request = mock(CreateProductRegistrationRequest.class);
        transaction.setStatus(TransactionStatus.PENDING);

        when(request.userId()).thenReturn(userId);
        when(request.productId()).thenReturn(productId);
        when(request.transactionId()).thenReturn(transactionId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));
        when(productRegistrationRepository.existsByUserIdAndProductIdAndStatus(
                userId,
                productId,
                RegistrationStatus.ACTIVE
        )).thenReturn(false);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> productRegistrationService.grantAccess(request)
        );

        assertEquals(400, ex.getStatusCode().value());
        assertEquals("Transaction is not successful", ex.getReason());
        verify(productRegistrationRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("createAfterSuccessfulPayment -> saves active registration and returns response")
    void createAfterSuccessfulPayment_ShouldSaveActiveRegistrationAndReturnResponse() {
        ProductRegistrationResponse response = mock(ProductRegistrationResponse.class);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));
        when(productRegistrationRepository.existsByUserIdAndProductIdAndStatus(
                userId,
                productId,
                RegistrationStatus.ACTIVE
        )).thenReturn(false);
        when(productRegistrationRepository.saveAndFlush(any(ProductRegistration.class))).thenAnswer(invocation -> {
            ProductRegistration saved = invocation.getArgument(0);
            saved.setId(registrationId);
            return saved;
        });
        when(productRegistrationMapper.toResponse(any(ProductRegistration.class))).thenReturn(response);

        ProductRegistrationResponse result = productRegistrationService.createAfterSuccessfulPayment(
                userId,
                productId,
                transactionId
        );

        assertNotNull(result);
        assertEquals(response, result);

        ArgumentCaptor<ProductRegistration> captor = ArgumentCaptor.forClass(ProductRegistration.class);
        verify(productRegistrationRepository).saveAndFlush(captor.capture());

        ProductRegistration savedRegistration = captor.getValue();
        assertEquals(RegistrationStatus.ACTIVE, savedRegistration.getStatus());
        assertEquals(user, savedRegistration.getUser());
        assertEquals(product, savedRegistration.getProduct());
        assertEquals(transaction, savedRegistration.getTransaction());
    }

    @Test
    @DisplayName("getById -> returns mapped response")
    void getById_ShouldReturnMappedResponse() {
        ProductRegistrationResponse response = mock(ProductRegistrationResponse.class);

        when(productRegistrationRepository.findDetailedById(registrationId)).thenReturn(Optional.of(registration));
        when(productRegistrationMapper.toResponse(registration)).thenReturn(response);

        ProductRegistrationResponse result = productRegistrationService.getById(registrationId);

        assertNotNull(result);
        assertEquals(response, result);
    }

    @Test
    @DisplayName("getByUserId -> returns mapped responses")
    void getByUserId_ShouldReturnMappedResponses() {
        List<ProductRegistration> registrations = List.of(registration);
        List<ProductRegistrationResponse> responses = List.of(mock(ProductRegistrationResponse.class));

        when(productRegistrationRepository.findAllByUserId(userId)).thenReturn(registrations);
        when(productRegistrationMapper.toResponseList(registrations)).thenReturn(responses);

        List<ProductRegistrationResponse> result = productRegistrationService.getByUserId(userId);

        assertNotNull(result);
        assertEquals(responses, result);
    }

    @Test
    @DisplayName("getByProductId -> returns mapped responses")
    void getByProductId_ShouldReturnMappedResponses() {
        List<ProductRegistration> registrations = List.of(registration);
        List<ProductRegistrationResponse> responses = List.of(mock(ProductRegistrationResponse.class));

        when(productRegistrationRepository.findAllByProductId(productId)).thenReturn(registrations);
        when(productRegistrationMapper.toResponseList(registrations)).thenReturn(responses);

        List<ProductRegistrationResponse> result = productRegistrationService.getByProductId(productId);

        assertNotNull(result);
        assertEquals(responses, result);
    }

    @Test
    @DisplayName("getMyRegistrations -> returns mapped responses")
    void getMyRegistrations_ShouldReturnMappedResponses() {
        List<ProductRegistration> registrations = List.of(registration);
        List<ProductRegistrationResponse> responses = List.of(mock(ProductRegistrationResponse.class));

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(productRegistrationRepository.findAllByUserId(userId)).thenReturn(registrations);
        when(productRegistrationMapper.toResponseList(registrations)).thenReturn(responses);

        List<ProductRegistrationResponse> result = productRegistrationService.getMyRegistrations(email);

        assertNotNull(result);
        assertEquals(responses, result);
    }

    @Test
    @DisplayName("getMyRegistrationById -> returns response when registration belongs to authenticated user")
    void getMyRegistrationById_ShouldReturnResponse_WhenRegistrationBelongsToAuthenticatedUser() {
        ProductRegistrationResponse response = mock(ProductRegistrationResponse.class);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(productRegistrationRepository.findDetailedById(registrationId)).thenReturn(Optional.of(registration));
        when(productRegistrationMapper.toResponse(registration)).thenReturn(response);

        ProductRegistrationResponse result = productRegistrationService.getMyRegistrationById(registrationId, email);

        assertNotNull(result);
        assertEquals(response, result);
    }

    @Test
    @DisplayName("getMyRegistrationById -> throws forbidden when registration belongs to another user")
    void getMyRegistrationById_ShouldThrowForbidden_WhenRegistrationBelongsToAnotherUser() {
        ProductRegistration otherRegistration = new ProductRegistration();
        otherRegistration.setId(registrationId);
        otherRegistration.setUser(secondUser);
        otherRegistration.setProduct(product);
        otherRegistration.setStatus(RegistrationStatus.ACTIVE);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(productRegistrationRepository.findDetailedById(registrationId)).thenReturn(Optional.of(otherRegistration));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> productRegistrationService.getMyRegistrationById(registrationId, email)
        );

        assertEquals(403, ex.getStatusCode().value());
        assertEquals("You do not have access to this product registration", ex.getReason());
        verify(productRegistrationMapper, never()).toResponse(any());
    }

    @Test
    @DisplayName("activate -> sets active status and returns response")
    void activate_ShouldSetStatusActiveAndReturnResponse() {
        ProductRegistrationResponse response = mock(ProductRegistrationResponse.class);
        registration.setStatus(RegistrationStatus.SUSPENDED);

        when(productRegistrationRepository.findDetailedById(registrationId)).thenReturn(Optional.of(registration));
        when(productRegistrationRepository.saveAndFlush(any(ProductRegistration.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(productRegistrationMapper.toResponse(any(ProductRegistration.class))).thenReturn(response);

        ProductRegistrationResponse result = productRegistrationService.activate(registrationId);

        assertNotNull(result);
        assertEquals(response, result);
        assertEquals(RegistrationStatus.ACTIVE, registration.getStatus());
    }

    @Test
    @DisplayName("suspend -> sets suspended status and returns response")
    void suspend_ShouldSetStatusSuspendedAndReturnResponse() {
        ProductRegistrationResponse response = mock(ProductRegistrationResponse.class);

        when(productRegistrationRepository.findDetailedById(registrationId)).thenReturn(Optional.of(registration));
        when(productRegistrationRepository.saveAndFlush(any(ProductRegistration.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(productRegistrationMapper.toResponse(any(ProductRegistration.class))).thenReturn(response);

        ProductRegistrationResponse result = productRegistrationService.suspend(registrationId);

        assertNotNull(result);
        assertEquals(response, result);
        assertEquals(RegistrationStatus.SUSPENDED, registration.getStatus());
    }

    @Test
    @DisplayName("expire -> sets expired status and returns response")
    void expire_ShouldSetStatusExpiredAndReturnResponse() {
        ProductRegistrationResponse response = mock(ProductRegistrationResponse.class);

        when(productRegistrationRepository.findDetailedById(registrationId)).thenReturn(Optional.of(registration));
        when(productRegistrationRepository.saveAndFlush(any(ProductRegistration.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(productRegistrationMapper.toResponse(any(ProductRegistration.class))).thenReturn(response);

        ProductRegistrationResponse result = productRegistrationService.expire(registrationId);

        assertNotNull(result);
        assertEquals(response, result);
        assertEquals(RegistrationStatus.EXPIRED, registration.getStatus());
    }
}