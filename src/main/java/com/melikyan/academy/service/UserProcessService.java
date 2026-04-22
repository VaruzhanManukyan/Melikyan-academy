package com.melikyan.academy.service;

import com.melikyan.academy.entity.User;
import org.springframework.http.HttpStatus;
import com.melikyan.academy.entity.UserProcess;
import com.melikyan.academy.entity.ContentItem;
import com.melikyan.academy.mapper.UserProcessMapper;
import com.melikyan.academy.repository.UserRepository;
import com.melikyan.academy.repository.ContentItemRepository;
import com.melikyan.academy.repository.UserProcessRepository;
import org.springframework.web.server.ResponseStatusException;
import com.melikyan.academy.dto.response.userProcess.UserProcessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserProcessService {
    private final UserRepository userRepository;
    private final UserProcessMapper userProcessMapper;
    private final ContentItemRepository contentItemRepository;
    private final UserProcessRepository userProcessRepository;

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

    private ContentItem getContentItemById(UUID id) {
        return contentItemRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Content item not found with id: " + id
                ));
    }

    private UserProcess getUserProcessByUserIdAndContentItemId(UUID userId, UUID contentItemId) {
        return userProcessRepository.findByUserIdAndContentItemId(userId, contentItemId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Progress not found for user id " + userId + " and content item id " + contentItemId
                ));
    }

    public List<UserProcessResponse> getMyProgress(String email) {
        User user = getUserByEmail(email);
        List<UserProcess> userProcesses = userProcessRepository.findAllByUserId(user.getId());
        return userProcessMapper.toResponseList(userProcesses);
    }

    public UserProcessResponse getMyProgressByContentItemId(UUID contentItemId, String email) {
        User user = getUserByEmail(email);
        getContentItemById(contentItemId);

        UserProcess userProcess = getUserProcessByUserIdAndContentItemId(user.getId(), contentItemId);
        return userProcessMapper.toResponse(userProcess);
    }

    public List<UserProcessResponse> getUserProgress(UUID userId) {
        getUserById(userId);

        List<UserProcess> userProcesses = userProcessRepository.findAllByUserId(userId);
        return userProcessMapper.toResponseList(userProcesses);
    }

    public UserProcessResponse getUserProgressByContentItemId(UUID userId, UUID contentItemId) {
        getUserById(userId);
        getContentItemById(contentItemId);

        UserProcess userProcess = getUserProcessByUserIdAndContentItemId(userId, contentItemId);
        return userProcessMapper.toResponse(userProcess);
    }
}