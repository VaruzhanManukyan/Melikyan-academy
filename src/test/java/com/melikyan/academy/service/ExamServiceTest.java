package com.melikyan.academy.service;

import org.mockito.ArgumentCaptor;
import com.melikyan.academy.entity.Exam;
import com.melikyan.academy.entity.User;
import com.melikyan.academy.mapper.ExamMapper;
import com.melikyan.academy.entity.ContentItem;
import org.mockito.junit.jupiter.MockitoExtension;
import com.melikyan.academy.repository.ExamRepository;
import com.melikyan.academy.repository.UserRepository;
import org.springframework.test.util.ReflectionTestUtils;
import com.melikyan.academy.entity.enums.ContentItemType;
import com.melikyan.academy.dto.response.exam.ExamResponse;
import com.melikyan.academy.repository.ContentItemRepository;
import org.springframework.web.server.ResponseStatusException;
import com.melikyan.academy.dto.request.exam.CreateExamRequest;
import com.melikyan.academy.dto.request.exam.UpdateExamRequest;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ExamServiceTest {
    @Mock
    private ExamMapper examMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ExamRepository examRepository;

    @Mock
    private ContentItemRepository contentItemRepository;

    @InjectMocks
    private ExamService examService;

    private UUID userId;
    private UUID examId;
    private UUID contentItemId;

    private User user;
    private ContentItem contentItem;
    private Exam exam;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        examId = UUID.randomUUID();
        contentItemId = UUID.randomUUID();

        user = new User();
        ReflectionTestUtils.setField(user, "id", userId);

        contentItem = new ContentItem();
        ReflectionTestUtils.setField(contentItem, "id", contentItemId);
        contentItem.setTitle("Old title");
        contentItem.setDescription("Old description");
        contentItem.setType(ContentItemType.EXAM);
        contentItem.setCreatedBy(user);

        exam = new Exam();
        ReflectionTestUtils.setField(exam, "id", examId);
        exam.setContentItem(contentItem);
    }

    @Test
    @DisplayName("create -> saves content item and exam, returns response")
    void create_ShouldSaveContentItemAndExamAndReturnResponse() {
        CreateExamRequest request = mock(CreateExamRequest.class);
        ExamResponse response = mock(ExamResponse.class);

        when(request.title()).thenReturn("  Java Final Exam  ");
        when(request.description()).thenReturn("  Exam description  ");
        when(request.createdById()).thenReturn(userId);

        when(contentItemRepository.existsByTypeAndTitleIgnoreCase(
                ContentItemType.EXAM,
                "Java Final Exam"
        )).thenReturn(false);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        when(contentItemRepository.saveAndFlush(any(ContentItem.class))).thenAnswer(invocation -> {
            ContentItem saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", contentItemId);
            return saved;
        });

        when(examRepository.saveAndFlush(any(Exam.class))).thenAnswer(invocation -> {
            Exam saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", examId);
            return saved;
        });

        when(examMapper.toResponse(any(Exam.class))).thenReturn(response);

        ExamResponse result = examService.create(request);

        assertEquals(response, result);

        ArgumentCaptor<ContentItem> contentCaptor = ArgumentCaptor.forClass(ContentItem.class);
        verify(contentItemRepository).saveAndFlush(contentCaptor.capture());

        ContentItem savedContentItem = contentCaptor.getValue();
        assertEquals(ContentItemType.EXAM, savedContentItem.getType());
        assertEquals("Java Final Exam", savedContentItem.getTitle());
        assertEquals("Exam description", savedContentItem.getDescription());
        assertEquals(0, savedContentItem.getTotalSteps());
        assertEquals(user, savedContentItem.getCreatedBy());

        ArgumentCaptor<Exam> examCaptor = ArgumentCaptor.forClass(Exam.class);
        verify(examRepository).saveAndFlush(examCaptor.capture());

        Exam savedExam = examCaptor.getValue();
        assertEquals(savedContentItem, savedExam.getContentItem());

        verify(examMapper).toResponse(any(Exam.class));
    }

    @Test
    @DisplayName("create -> throws bad request when title is blank")
    void create_ShouldThrowBadRequest_WhenTitleIsBlank() {
        CreateExamRequest request = mock(CreateExamRequest.class);

        when(request.title()).thenReturn("   ");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> examService.create(request)
        );

        assertEquals(400, exception.getStatusCode().value());
        assertEquals("Exam title must not be blank", exception.getReason());

        verifyNoInteractions(userRepository, examRepository, contentItemRepository, examMapper);
    }

    @Test
    @DisplayName("create -> throws conflict when title already exists")
    void create_ShouldThrowConflict_WhenTitleAlreadyExists() {
        CreateExamRequest request = mock(CreateExamRequest.class);

        when(request.title()).thenReturn("Java Final Exam");
        when(request.description()).thenReturn(null);

        when(contentItemRepository.existsByTypeAndTitleIgnoreCase(
                ContentItemType.EXAM,
                "Java Final Exam"
        )).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> examService.create(request)
        );

        assertEquals(409, exception.getStatusCode().value());
        assertEquals("Exam with this title already exists", exception.getReason());

        verify(userRepository, never()).findById(any(UUID.class));
        verify(contentItemRepository, never()).saveAndFlush(any(ContentItem.class));
        verify(examRepository, never()).saveAndFlush(any(Exam.class));
        verifyNoInteractions(examMapper);
    }

    @Test
    @DisplayName("create -> throws not found when user does not exist")
    void create_ShouldThrowNotFound_WhenUserDoesNotExist() {
        CreateExamRequest request = mock(CreateExamRequest.class);

        when(request.title()).thenReturn("Java Final Exam");
        when(request.description()).thenReturn("Exam description");
        when(request.createdById()).thenReturn(userId);

        when(contentItemRepository.existsByTypeAndTitleIgnoreCase(
                ContentItemType.EXAM,
                "Java Final Exam"
        )).thenReturn(false);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> examService.create(request)
        );

        assertEquals(404, exception.getStatusCode().value());
        assertEquals("User not found with id: " + userId, exception.getReason());

        verify(contentItemRepository, never()).saveAndFlush(any(ContentItem.class));
        verify(examRepository, never()).saveAndFlush(any(Exam.class));
        verifyNoInteractions(examMapper);
    }

    @Test
    @DisplayName("getById -> returns mapped response")
    void getById_ShouldReturnMappedResponse() {
        ExamResponse response = mock(ExamResponse.class);

        when(examRepository.findDetailedById(examId)).thenReturn(Optional.of(exam));
        when(examMapper.toResponse(exam)).thenReturn(response);

        ExamResponse result = examService.getById(examId);

        assertEquals(response, result);
        verify(examRepository).findDetailedById(examId);
        verify(examMapper).toResponse(exam);
    }

    @Test
    @DisplayName("getById -> throws not found when exam does not exist")
    void getById_ShouldThrowNotFound_WhenExamDoesNotExist() {
        when(examRepository.findDetailedById(examId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> examService.getById(examId)
        );

        assertEquals(404, exception.getStatusCode().value());
        assertEquals("Exam not found with id: " + examId, exception.getReason());

        verifyNoInteractions(examMapper);
    }

    @Test
    @DisplayName("getAll -> returns mapped list")
    void getAll_ShouldReturnMappedList() {
        Exam secondExam = new Exam();
        List<Exam> exams = List.of(exam, secondExam);

        ExamResponse first = mock(ExamResponse.class);
        ExamResponse second = mock(ExamResponse.class);
        List<ExamResponse> responses = List.of(first, second);

        when(examRepository.findAllDetailed()).thenReturn(exams);
        when(examMapper.toResponseList(exams)).thenReturn(responses);

        List<ExamResponse> result = examService.getAll();

        assertEquals(responses, result);
        verify(examRepository).findAllDetailed();
        verify(examMapper).toResponseList(exams);
    }

    @Test
    @DisplayName("update -> updates content item fields")
    void update_ShouldUpdateContentItemFields() {
        UpdateExamRequest request = mock(UpdateExamRequest.class);
        ExamResponse response = mock(ExamResponse.class);

        when(request.title()).thenReturn("  Updated Exam  ");
        when(request.description()).thenReturn("   ");

        when(examRepository.findDetailedById(examId)).thenReturn(Optional.of(exam));

        when(contentItemRepository.existsByTypeAndTitleIgnoreCaseAndIdNot(
                ContentItemType.EXAM,
                "Updated Exam",
                contentItemId
        )).thenReturn(false);

        when(examRepository.saveAndFlush(any(Exam.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(examMapper.toResponse(any(Exam.class))).thenReturn(response);

        ExamResponse result = examService.update(examId, request);

        assertEquals(response, result);
        assertEquals("Updated Exam", contentItem.getTitle());
        assertNull(contentItem.getDescription());

        verify(examRepository).saveAndFlush(exam);
        verify(examMapper).toResponse(exam);
    }

    @Test
    @DisplayName("update -> throws bad request when title is blank")
    void update_ShouldThrowBadRequest_WhenTitleIsBlank() {
        UpdateExamRequest request = mock(UpdateExamRequest.class);

        when(request.title()).thenReturn("   ");
        when(examRepository.findDetailedById(examId)).thenReturn(Optional.of(exam));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> examService.update(examId, request)
        );

        assertEquals(400, exception.getStatusCode().value());
        assertEquals("Exam title must not be blank", exception.getReason());

        verify(contentItemRepository, never()).existsByTypeAndTitleIgnoreCaseAndIdNot(
                any(ContentItemType.class),
                anyString(),
                any(UUID.class)
        );
        verify(examRepository, never()).saveAndFlush(any(Exam.class));
        verifyNoInteractions(examMapper);
    }

    @Test
    @DisplayName("update -> throws conflict when new title already exists")
    void update_ShouldThrowConflict_WhenNewTitleAlreadyExists() {
        UpdateExamRequest request = mock(UpdateExamRequest.class);

        when(request.title()).thenReturn("Existing Exam");

        when(examRepository.findDetailedById(examId)).thenReturn(Optional.of(exam));

        when(contentItemRepository.existsByTypeAndTitleIgnoreCaseAndIdNot(
                ContentItemType.EXAM,
                "Existing Exam",
                contentItemId
        )).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> examService.update(examId, request)
        );

        assertEquals(409, exception.getStatusCode().value());
        assertEquals("Exam with this title already exists", exception.getReason());

        verify(examRepository, never()).saveAndFlush(any(Exam.class));
        verifyNoInteractions(examMapper);
    }

    @Test
    @DisplayName("delete -> deletes exam and content item")
    void delete_ShouldDeleteExamAndContentItem() {
        when(examRepository.findDetailedById(examId)).thenReturn(Optional.of(exam));

        examService.delete(examId);

        verify(examRepository).delete(exam);
        verify(contentItemRepository).delete(contentItem);
    }

    @Test
    @DisplayName("delete -> throws not found when exam does not exist")
    void delete_ShouldThrowNotFound_WhenExamDoesNotExist() {
        when(examRepository.findDetailedById(examId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> examService.delete(examId)
        );

        assertEquals(404, exception.getStatusCode().value());
        assertEquals("Exam not found with id: " + examId, exception.getReason());

        verify(examRepository, never()).delete(any(Exam.class));
        verify(contentItemRepository, never()).delete(any(ContentItem.class));
    }
}