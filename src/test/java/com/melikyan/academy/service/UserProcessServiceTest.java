package com.melikyan.academy.service;

import com.melikyan.academy.entity.User;
import com.melikyan.academy.entity.ContentItem;
import com.melikyan.academy.entity.UserProcess;
import org.mockito.junit.jupiter.MockitoExtension;
import com.melikyan.academy.mapper.UserProcessMapper;
import com.melikyan.academy.repository.UserRepository;
import com.melikyan.academy.repository.UserProcessRepository;
import com.melikyan.academy.repository.ContentItemRepository;
import org.springframework.web.server.ResponseStatusException;
import com.melikyan.academy.dto.response.userProcess.UserProcessResponse;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.UUID;
import java.util.Optional;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.mockito.Mockito.*;

import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(MockitoExtension.class)
class UserProcessServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private UserProcessMapper userProcessMapper;

    @Mock
    private ContentItemRepository contentItemRepository;

    @Mock
    private UserProcessRepository userProcessRepository;

    @InjectMocks
    private UserProcessService userProcessService;

    private UUID userId;
    private UUID contentItemId;
    private User user;
    private ContentItem contentItem;
    private UserProcess userProcess;
    private UserProcessResponse response;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        contentItemId = UUID.randomUUID();

        user = new User();
        user.setId(userId);
        user.setEmail("test@test.com");

        contentItem = new ContentItem();
        contentItem.setId(contentItemId);
        contentItem.setTotalSteps(10);

        userProcess = new UserProcess();
        userProcess.setId(UUID.randomUUID());
        userProcess.setUser(user);
        userProcess.setContentItem(contentItem);
        userProcess.setCurrentStep(3);
        userProcess.setScoreAccumulated(BigDecimal.valueOf(25.50));
        userProcess.setLastAccessedAt(OffsetDateTime.parse("2026-04-23T12:00:00Z"));

        response = new UserProcessResponse(
                userProcess.getId(),
                3,
                BigDecimal.valueOf(25.50),
                userId,
                contentItemId,
                OffsetDateTime.parse("2026-04-23T12:00:00Z"),
                OffsetDateTime.parse("2026-04-20T10:00:00Z"),
                OffsetDateTime.parse("2026-04-23T12:00:00Z")
        );
    }

    @Test
    @DisplayName("getMyProgress -> returns mapped responses")
    void getMyProgress_ShouldReturnMappedResponses() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(userProcessRepository.findAllByUserId(userId)).thenReturn(List.of(userProcess));
        when(userProcessMapper.toResponseList(List.of(userProcess))).thenReturn(List.of(response));

        List<UserProcessResponse> result = userProcessService.getMyProgress("test@test.com");

        assertEquals(1, result.size());
        assertEquals(response, result.get(0));

        verify(userRepository).findByEmail("test@test.com");
        verify(userProcessRepository).findAllByUserId(userId);
        verify(userProcessMapper).toResponseList(List.of(userProcess));
    }

    @Test
    @DisplayName("getMyProgress -> throws not found when user does not exist")
    void getMyProgress_ShouldThrowNotFound_WhenUserDoesNotExist() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> userProcessService.getMyProgress("test@test.com")
        );

        assertEquals(404, exception.getStatusCode().value());
        assertEquals("User not found with email: test@test.com", exception.getReason());

        verify(userRepository).findByEmail("test@test.com");
        verifyNoInteractions(userProcessRepository, userProcessMapper);
    }

    @Test
    @DisplayName("getMyProgressByContentItemId -> returns mapped response")
    void getMyProgressByContentItemId_ShouldReturnMappedResponse() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(contentItemRepository.findById(contentItemId)).thenReturn(Optional.of(contentItem));
        when(userProcessRepository.findByUserIdAndContentItemId(userId, contentItemId))
                .thenReturn(Optional.of(userProcess));
        when(userProcessMapper.toResponse(userProcess)).thenReturn(response);

        UserProcessResponse result = userProcessService.getMyProgressByContentItemId(contentItemId, "test@test.com");

        assertEquals(response, result);

        verify(userRepository).findByEmail("test@test.com");
        verify(contentItemRepository).findById(contentItemId);
        verify(userProcessRepository).findByUserIdAndContentItemId(userId, contentItemId);
        verify(userProcessMapper).toResponse(userProcess);
    }

    @Test
    @DisplayName("getMyProgressByContentItemId -> throws not found when content item does not exist")
    void getMyProgressByContentItemId_ShouldThrowNotFound_WhenContentItemDoesNotExist() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(contentItemRepository.findById(contentItemId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> userProcessService.getMyProgressByContentItemId(contentItemId, "test@test.com")
        );

        assertEquals(404, exception.getStatusCode().value());
        assertEquals("Content item not found with id: " + contentItemId, exception.getReason());

        verify(userRepository).findByEmail("test@test.com");
        verify(contentItemRepository).findById(contentItemId);
        verifyNoMoreInteractions(userProcessRepository, userProcessMapper);
    }

    @Test
    @DisplayName("getMyProgressByContentItemId -> throws not found when progress does not exist")
    void getMyProgressByContentItemId_ShouldThrowNotFound_WhenProgressDoesNotExist() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(contentItemRepository.findById(contentItemId)).thenReturn(Optional.of(contentItem));
        when(userProcessRepository.findByUserIdAndContentItemId(userId, contentItemId))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> userProcessService.getMyProgressByContentItemId(contentItemId, "test@test.com")
        );

        assertEquals(404, exception.getStatusCode().value());
        assertEquals(
                "Progress not found for user id " + userId + " and content item id " + contentItemId,
                exception.getReason()
        );

        verify(userRepository).findByEmail("test@test.com");
        verify(contentItemRepository).findById(contentItemId);
        verify(userProcessRepository).findByUserIdAndContentItemId(userId, contentItemId);
        verifyNoInteractions(userProcessMapper);
    }

    @Test
    @DisplayName("getUserProgress -> returns mapped responses")
    void getUserProgress_ShouldReturnMappedResponses() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userProcessRepository.findAllByUserId(userId)).thenReturn(List.of(userProcess));
        when(userProcessMapper.toResponseList(List.of(userProcess))).thenReturn(List.of(response));

        List<UserProcessResponse> result = userProcessService.getUserProgress(userId);

        assertEquals(1, result.size());
        assertEquals(response, result.get(0));

        verify(userRepository).findById(userId);
        verify(userProcessRepository).findAllByUserId(userId);
        verify(userProcessMapper).toResponseList(List.of(userProcess));
    }

    @Test
    @DisplayName("getUserProgress -> throws not found when user does not exist")
    void getUserProgress_ShouldThrowNotFound_WhenUserDoesNotExist() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> userProcessService.getUserProgress(userId)
        );

        assertEquals(404, exception.getStatusCode().value());
        assertEquals("User not found with id: " + userId, exception.getReason());

        verify(userRepository).findById(userId);
        verifyNoInteractions(userProcessRepository, userProcessMapper);
    }

    @Test
    @DisplayName("getUserProgressByContentItemId -> returns mapped response")
    void getUserProgressByContentItemId_ShouldReturnMappedResponse() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(contentItemRepository.findById(contentItemId)).thenReturn(Optional.of(contentItem));
        when(userProcessRepository.findByUserIdAndContentItemId(userId, contentItemId))
                .thenReturn(Optional.of(userProcess));
        when(userProcessMapper.toResponse(userProcess)).thenReturn(response);

        UserProcessResponse result = userProcessService.getUserProgressByContentItemId(userId, contentItemId);

        assertEquals(response, result);

        verify(userRepository).findById(userId);
        verify(contentItemRepository).findById(contentItemId);
        verify(userProcessRepository).findByUserIdAndContentItemId(userId, contentItemId);
        verify(userProcessMapper).toResponse(userProcess);
    }

    @Test
    @DisplayName("getUserProgressByContentItemId -> throws not found when user does not exist")
    void getUserProgressByContentItemId_ShouldThrowNotFound_WhenUserDoesNotExist() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> userProcessService.getUserProgressByContentItemId(userId, contentItemId)
        );

        assertEquals(404, exception.getStatusCode().value());
        assertEquals("User not found with id: " + userId, exception.getReason());

        verify(userRepository).findById(userId);
        verifyNoInteractions(contentItemRepository, userProcessRepository, userProcessMapper);
    }

    @Test
    @DisplayName("getUserProgressByContentItemId -> throws not found when content item does not exist")
    void getUserProgressByContentItemId_ShouldThrowNotFound_WhenContentItemDoesNotExist() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(contentItemRepository.findById(contentItemId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> userProcessService.getUserProgressByContentItemId(userId, contentItemId)
        );

        assertEquals(404, exception.getStatusCode().value());
        assertEquals("Content item not found with id: " + contentItemId, exception.getReason());

        verify(userRepository).findById(userId);
        verify(contentItemRepository).findById(contentItemId);
        verifyNoMoreInteractions(userProcessRepository, userProcessMapper);
    }

    @Test
    @DisplayName("getUserProgressByContentItemId -> throws not found when progress does not exist")
    void getUserProgressByContentItemId_ShouldThrowNotFound_WhenProgressDoesNotExist() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(contentItemRepository.findById(contentItemId)).thenReturn(Optional.of(contentItem));
        when(userProcessRepository.findByUserIdAndContentItemId(userId, contentItemId))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> userProcessService.getUserProgressByContentItemId(userId, contentItemId)
        );

        assertEquals(404, exception.getStatusCode().value());
        assertEquals(
                "Progress not found for user id " + userId + " and content item id " + contentItemId,
                exception.getReason()
        );

        verify(userRepository).findById(userId);
        verify(contentItemRepository).findById(contentItemId);
        verify(userProcessRepository).findByUserIdAndContentItemId(userId, contentItemId);
        verifyNoInteractions(userProcessMapper);
    }
}