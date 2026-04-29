package com.melikyan.academy.service;

import com.melikyan.academy.entity.User;
import com.melikyan.academy.entity.Exam;
import com.melikyan.academy.entity.Course;
import com.melikyan.academy.repository.*;
import org.springframework.http.HttpStatus;
import com.melikyan.academy.entity.ContentItem;
import com.melikyan.academy.entity.enums.ContentItemType;
import com.melikyan.academy.entity.ContentItemTranslation;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.dao.DataIntegrityViolationException;
import com.melikyan.academy.mapper.ContentItemTranslationMapper;
import com.melikyan.academy.dto.response.contentItemTranslation.ContentItemTranslationResponse;
import com.melikyan.academy.dto.request.contentItemTranslation.CreateContentItemTranslationRequest;
import com.melikyan.academy.dto.request.contentItemTranslation.UpdateContentItemTranslationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.Locale;

@Service
@Transactional
@RequiredArgsConstructor
public class ContentItemTranslationService {
    private final UserRepository userRepository;
    private final ExamRepository examRepository;
    private final CourseRepository courseRepository;
    private final LanguageRepository languageRepository;
    private final ContentItemRepository contentItemRepository;
    private final ContentItemTranslationMapper contentItemTranslationMapper;
    private final ContentItemTranslationRepository contentItemTranslationRepository;

    private String normalizeCode(String code) {
        String normalizedCode = code.trim().toLowerCase(Locale.ROOT);

        if (normalizedCode.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Content item translation code must not be blank"
            );
        }

        if (normalizedCode.length() != 2) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Content item translation code must contain exactly 2 characters"
            );
        }

        return normalizedCode;
    }

    private String normalizeTitle(String title) {
        String normalizedTitle = title.trim();

        if (normalizedTitle.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Content item translation title must not be blank"
            );
        }

        return normalizedTitle;
    }

    private String normalizeDescription(String description) {
        if (description == null) {
            return null;
        }

        String normalizedDescription = description.trim();
        return normalizedDescription.isBlank() ? null : normalizedDescription;
    }

    private User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found with id: " + id
                ));
    }

    private Course getCourseById(UUID id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Course not found with id: " + id
                ));
    }

    private Exam getExamById(UUID id) {
        return examRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Exam not found with id: " + id
                ));
    }

    private ContentItem getContentItemById(UUID id) {
        return contentItemRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Content item not found with id: " + id
                ));
    }

    private ContentItemTranslation getContentItemTranslationById(UUID id) {
        return contentItemTranslationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Content item translation not found with id: " + id
                ));
    }

    private void validateLanguageExists(String code) {
        if (!languageRepository.existsByCodeIgnoreCase(code)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Language not found with code: " + code
            );
        }
    }

    private void validateContentItemType(
            ContentItemTranslation contentItemTranslation,
            ContentItemType type
    ) {
        if (contentItemTranslation.getContentItem().getType() != type) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Content item translation not found with id: " + contentItemTranslation.getId()
            );
        }
    }

    private void validateContentItemType(
            ContentItem contentItem,
            ContentItemType type
    ) {
        if (contentItem.getType() != type) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Content item type must be " + type
            );
        }
    }

    private ContentItem getCourseContentItem(UUID courseId) {
        Course course = getCourseById(courseId);
        return course.getContentItem();
    }

    private ContentItem getExamContentItem(UUID examId) {
        Exam exam = getExamById(examId);
        return exam.getContentItem();
    }

    public ContentItemTranslationResponse createCourseTranslation(CreateContentItemTranslationRequest request) {
        String normalizedCode = normalizeCode(request.code());
        String normalizedTitle = normalizeTitle(request.title());
        String normalizedDescription = normalizeDescription(request.description());

        ContentItem contentItem = getContentItemById(request.contentItemId());
        validateContentItemType(contentItem, ContentItemType.COURSE);

        User createdBy = getUserById(request.createdById());

        validateLanguageExists(normalizedCode);

        if (contentItemTranslationRepository.existsByContentItemIdAndCodeIgnoreCase(
                contentItem.getId(),
                normalizedCode
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Course translation with this code already exists for this course"
            );
        }

        ContentItemTranslation contentItemTranslation = new ContentItemTranslation();
        contentItemTranslation.setCode(normalizedCode);
        contentItemTranslation.setTitle(normalizedTitle);
        contentItemTranslation.setDescription(normalizedDescription);
        contentItemTranslation.setContentItem(contentItem);
        contentItemTranslation.setCreatedBy(createdBy);

        try {
            ContentItemTranslation savedContentItemTranslation =
                    contentItemTranslationRepository.saveAndFlush(contentItemTranslation);

            return contentItemTranslationMapper.toResponse(savedContentItemTranslation);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Course translation with this code already exists for this course",
                    exception
            );
        }
    }

    public ContentItemTranslationResponse createExamTranslation(CreateContentItemTranslationRequest request) {
        String normalizedCode = normalizeCode(request.code());
        String normalizedTitle = normalizeTitle(request.title());
        String normalizedDescription = normalizeDescription(request.description());

        ContentItem contentItem = getContentItemById(request.contentItemId());
        validateContentItemType(contentItem, ContentItemType.EXAM);

        User createdBy = getUserById(request.createdById());

        validateLanguageExists(normalizedCode);

        if (contentItemTranslationRepository.existsByContentItemIdAndCodeIgnoreCase(
                contentItem.getId(),
                normalizedCode
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Exam translation with this code already exists for this exam"
            );
        }

        ContentItemTranslation contentItemTranslation = new ContentItemTranslation();
        contentItemTranslation.setCode(normalizedCode);
        contentItemTranslation.setTitle(normalizedTitle);
        contentItemTranslation.setDescription(normalizedDescription);
        contentItemTranslation.setContentItem(contentItem);
        contentItemTranslation.setCreatedBy(createdBy);

        try {
            ContentItemTranslation savedContentItemTranslation =
                    contentItemTranslationRepository.saveAndFlush(contentItemTranslation);

            return contentItemTranslationMapper.toResponse(savedContentItemTranslation);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Exam translation with this code already exists for this exam",
                    exception
            );
        }
    }

    @Transactional(readOnly = true)
    public ContentItemTranslationResponse getCourseTranslationById(UUID id) {
        ContentItemTranslation contentItemTranslation = getContentItemTranslationById(id);
        validateContentItemType(contentItemTranslation, ContentItemType.COURSE);

        return contentItemTranslationMapper.toResponse(contentItemTranslation);
    }

    @Transactional(readOnly = true)
    public ContentItemTranslationResponse getExamTranslationById(UUID id) {
        ContentItemTranslation contentItemTranslation = getContentItemTranslationById(id);
        validateContentItemType(contentItemTranslation, ContentItemType.EXAM);

        return contentItemTranslationMapper.toResponse(contentItemTranslation);
    }

    @Transactional(readOnly = true)
    public List<ContentItemTranslationResponse> getAllCourseTranslations() {
        List<ContentItemTranslation> contentItemTranslations =
                contentItemTranslationRepository.findByContentItemType(ContentItemType.COURSE);

        return contentItemTranslationMapper.toResponseList(contentItemTranslations);
    }

    @Transactional(readOnly = true)
    public List<ContentItemTranslationResponse> getAllExamTranslations() {
        List<ContentItemTranslation> contentItemTranslations =
                contentItemTranslationRepository.findByContentItemType(ContentItemType.EXAM);

        return contentItemTranslationMapper.toResponseList(contentItemTranslations);
    }

    @Transactional(readOnly = true)
    public List<ContentItemTranslationResponse> getCourseTranslationsByCode(String code) {
        String normalizedCode = normalizeCode(code);
        validateLanguageExists(normalizedCode);

        List<ContentItemTranslation> contentItemTranslations =
                contentItemTranslationRepository.findByContentItemTypeAndCodeIgnoreCase(
                        ContentItemType.COURSE,
                        normalizedCode
                );

        return contentItemTranslationMapper.toResponseList(contentItemTranslations);
    }

    @Transactional(readOnly = true)
    public List<ContentItemTranslationResponse> getExamTranslationsByCode(String code) {
        String normalizedCode = normalizeCode(code);
        validateLanguageExists(normalizedCode);

        List<ContentItemTranslation> contentItemTranslations =
                contentItemTranslationRepository.findByContentItemTypeAndCodeIgnoreCase(
                        ContentItemType.EXAM,
                        normalizedCode
                );

        return contentItemTranslationMapper.toResponseList(contentItemTranslations);
    }

    @Transactional(readOnly = true)
    public List<ContentItemTranslationResponse> getByCourseId(UUID courseId) {
        ContentItem contentItem = getCourseContentItem(courseId);

        List<ContentItemTranslation> contentItemTranslations =
                contentItemTranslationRepository.findByContentItemId(contentItem.getId());

        return contentItemTranslationMapper.toResponseList(contentItemTranslations);
    }

    @Transactional(readOnly = true)
    public List<ContentItemTranslationResponse> getByExamId(UUID examId) {
        ContentItem contentItem = getExamContentItem(examId);

        List<ContentItemTranslation> contentItemTranslations =
                contentItemTranslationRepository.findByContentItemId(contentItem.getId());

        return contentItemTranslationMapper.toResponseList(contentItemTranslations);
    }

    @Transactional(readOnly = true)
    public ContentItemTranslationResponse getByCourseIdAndCode(UUID courseId, String code) {
        String normalizedCode = normalizeCode(code);

        ContentItem contentItem = getCourseContentItem(courseId);
        validateLanguageExists(normalizedCode);

        ContentItemTranslation contentItemTranslation = contentItemTranslationRepository
                .findByContentItemIdAndCodeIgnoreCase(contentItem.getId(), normalizedCode)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Course translation not found with course id: " +
                                courseId + " and code: " + normalizedCode
                ));

        return contentItemTranslationMapper.toResponse(contentItemTranslation);
    }

    @Transactional(readOnly = true)
    public ContentItemTranslationResponse getByExamIdAndCode(UUID examId, String code) {
        String normalizedCode = normalizeCode(code);

        ContentItem contentItem = getExamContentItem(examId);
        validateLanguageExists(normalizedCode);

        ContentItemTranslation contentItemTranslation = contentItemTranslationRepository
                .findByContentItemIdAndCodeIgnoreCase(contentItem.getId(), normalizedCode)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Exam translation not found with exam id: " +
                                examId + " and code: " + normalizedCode
                ));

        return contentItemTranslationMapper.toResponse(contentItemTranslation);
    }

    public ContentItemTranslationResponse updateCourseTranslation(
            UUID id,
            UpdateContentItemTranslationRequest request
    ) {
        ContentItemTranslation contentItemTranslation = getContentItemTranslationById(id);
        validateContentItemType(contentItemTranslation, ContentItemType.COURSE);

        if (
                request.code() == null &&
                        request.title() == null &&
                        request.description() == null
        ) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "At least one field must be provided"
            );
        }

        if (request.code() != null) {
            String normalizedCode = normalizeCode(request.code());
            validateLanguageExists(normalizedCode);

            if (contentItemTranslationRepository.existsByContentItemIdAndCodeIgnoreCaseAndIdNot(
                    contentItemTranslation.getContentItem().getId(),
                    normalizedCode,
                    id
            )) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Course translation with this code already exists for this course"
                );
            }

            contentItemTranslation.setCode(normalizedCode);
        }

        if (request.title() != null) {
            contentItemTranslation.setTitle(normalizeTitle(request.title()));
        }

        if (request.description() != null) {
            contentItemTranslation.setDescription(normalizeDescription(request.description()));
        }

        try {
            ContentItemTranslation savedContentItemTranslation =
                    contentItemTranslationRepository.saveAndFlush(contentItemTranslation);

            return contentItemTranslationMapper.toResponse(savedContentItemTranslation);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Course translation with this code already exists for this course",
                    exception
            );
        }
    }

    public ContentItemTranslationResponse updateExamTranslation(
            UUID id,
            UpdateContentItemTranslationRequest request
    ) {
        ContentItemTranslation contentItemTranslation = getContentItemTranslationById(id);
        validateContentItemType(contentItemTranslation, ContentItemType.EXAM);

        if (
                request.code() == null &&
                        request.title() == null &&
                        request.description() == null
        ) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "At least one field must be provided"
            );
        }

        if (request.code() != null) {
            String normalizedCode = normalizeCode(request.code());
            validateLanguageExists(normalizedCode);

            if (contentItemTranslationRepository.existsByContentItemIdAndCodeIgnoreCaseAndIdNot(
                    contentItemTranslation.getContentItem().getId(),
                    normalizedCode,
                    id
            )) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Exam translation with this code already exists for this exam"
                );
            }

            contentItemTranslation.setCode(normalizedCode);
        }

        if (request.title() != null) {
            contentItemTranslation.setTitle(normalizeTitle(request.title()));
        }

        if (request.description() != null) {
            contentItemTranslation.setDescription(normalizeDescription(request.description()));
        }

        try {
            ContentItemTranslation savedContentItemTranslation =
                    contentItemTranslationRepository.saveAndFlush(contentItemTranslation);

            return contentItemTranslationMapper.toResponse(savedContentItemTranslation);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Exam translation with this code already exists for this exam",
                    exception
            );
        }
    }

    public void deleteCourseTranslation(UUID id) {
        ContentItemTranslation contentItemTranslation = getContentItemTranslationById(id);
        validateContentItemType(contentItemTranslation, ContentItemType.COURSE);

        contentItemTranslationRepository.delete(contentItemTranslation);
    }

    public void deleteExamTranslation(UUID id) {
        ContentItemTranslation contentItemTranslation = getContentItemTranslationById(id);
        validateContentItemType(contentItemTranslation, ContentItemType.EXAM);

        contentItemTranslationRepository.delete(contentItemTranslation);
    }
}