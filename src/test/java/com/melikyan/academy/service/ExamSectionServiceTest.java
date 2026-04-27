package com.melikyan.academy.service;

import org.mockito.ArgumentCaptor;
import com.melikyan.academy.entity.User;
import com.melikyan.academy.entity.Exam;
import com.melikyan.academy.entity.ExamSection;
import org.mockito.junit.jupiter.MockitoExtension;
import com.melikyan.academy.mapper.ExamSectionMapper;
import com.melikyan.academy.repository.ExamRepository;
import com.melikyan.academy.repository.UserRepository;
import com.melikyan.academy.repository.ExamSectionRepository;
import org.springframework.web.server.ResponseStatusException;
import com.melikyan.academy.dto.response.examSection.ExamSectionResponse;
import com.melikyan.academy.dto.request.examSection.CreateExamSectionRequest;
import com.melikyan.academy.dto.request.examSection.UpdateExamSectionRequest;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.UUID;
import java.time.Duration;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ExamSectionServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private ExamRepository examRepository;

    @Mock
    private ExamSectionMapper examSectionMapper;

    @Mock
    private ExamSectionRepository examSectionRepository;

    @InjectMocks
    private ExamSectionService examSectionService;

    private UUID examSectionId;
    private UUID examId;
    private UUID userId;

    private User user;
    private Exam exam;
    private ExamSection examSection;

    @BeforeEach
    void setUp() {
        examSectionId = UUID.randomUUID();
        examId = UUID.randomUUID();
        userId = UUID.randomUUID();

        user = new User();
        user.setId(userId);

        exam = new Exam();
        exam.setId(examId);

        examSection = new ExamSection();
        examSection.setId(examSectionId);
        examSection.setOrderIndex(1);
        examSection.setTitle("Exam section title");
        examSection.setDescription("Exam section description");
        examSection.setDuration(Duration.ofMinutes(90));
        examSection.setExam(exam);
        examSection.setCreatedBy(user);
    }

    @Test
    @DisplayName("create -> saves exam section and returns response")
    void create_ShouldSaveExamSectionAndReturnResponse() {
        CreateExamSectionRequest request = new CreateExamSectionRequest(
                1,
                "  Introduction Section  ",
                "  First exam section  ",
                Duration.ofMinutes(90),
                examId,
                userId
        );

        ExamSectionResponse response = mock(ExamSectionResponse.class);

        when(examSectionRepository.existsByExamIdAndTitleIgnoreCase(examId, "Introduction Section"))
                .thenReturn(false);
        when(examSectionRepository.existsByExamIdAndOrderIndex(examId, 1))
                .thenReturn(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(examRepository.findDetailedById(examId)).thenReturn(Optional.of(exam));
        when(examSectionRepository.saveAndFlush(any(ExamSection.class))).thenAnswer(invocation -> {
            ExamSection saved = invocation.getArgument(0);
            saved.setId(examSectionId);
            return saved;
        });
        when(examSectionMapper.toResponse(any(ExamSection.class))).thenReturn(response);

        ExamSectionResponse result = examSectionService.create(request);

        assertNotNull(result);
        assertEquals(response, result);

        ArgumentCaptor<ExamSection> examSectionCaptor = ArgumentCaptor.forClass(ExamSection.class);
        verify(examSectionRepository).saveAndFlush(examSectionCaptor.capture());

        ExamSection savedExamSection = examSectionCaptor.getValue();
        assertEquals(1, savedExamSection.getOrderIndex());
        assertEquals("Introduction Section", savedExamSection.getTitle());
        assertEquals("First exam section", savedExamSection.getDescription());
        assertEquals(Duration.ofMinutes(90), savedExamSection.getDuration());
        assertEquals(exam, savedExamSection.getExam());
        assertEquals(user, savedExamSection.getCreatedBy());
    }

    @Test
    @DisplayName("create -> throws conflict when orderIndex already exists in exam")
    void create_ShouldThrowConflict_WhenOrderIndexAlreadyExists() {
        CreateExamSectionRequest request = new CreateExamSectionRequest(
                1,
                "Section",
                "Description",
                Duration.ofMinutes(90),
                examId,
                userId
        );

        when(examSectionRepository.existsByExamIdAndTitleIgnoreCase(examId, "Section"))
                .thenReturn(false);
        when(examSectionRepository.existsByExamIdAndOrderIndex(examId, 1))
                .thenReturn(true);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> examSectionService.create(request)
        );

        assertEquals(409, ex.getStatusCode().value());
        verify(examSectionRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("create -> throws conflict when title already exists in exam")
    void create_ShouldThrowConflict_WhenTitleAlreadyExists() {
        CreateExamSectionRequest request = new CreateExamSectionRequest(
                1,
                "Section",
                "Description",
                Duration.ofMinutes(90),
                examId,
                userId
        );

        when(examSectionRepository.existsByExamIdAndTitleIgnoreCase(examId, "Section"))
                .thenReturn(true);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> examSectionService.create(request)
        );

        assertEquals(409, ex.getStatusCode().value());
        verify(examSectionRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("create -> throws bad request when title is blank")
    void create_ShouldThrowBadRequest_WhenTitleIsBlank() {
        CreateExamSectionRequest request = new CreateExamSectionRequest(
                1,
                "   ",
                "Description",
                Duration.ofMinutes(90),
                examId,
                userId
        );

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> examSectionService.create(request)
        );

        assertEquals(400, ex.getStatusCode().value());
        verify(examSectionRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("create -> throws not found when user does not exist")
    void create_ShouldThrowNotFound_WhenUserDoesNotExist() {
        CreateExamSectionRequest request = new CreateExamSectionRequest(
                1,
                "Section",
                "Description",
                Duration.ofMinutes(90),
                examId,
                userId
        );

        when(examSectionRepository.existsByExamIdAndTitleIgnoreCase(examId, "Section"))
                .thenReturn(false);
        when(examSectionRepository.existsByExamIdAndOrderIndex(examId, 1))
                .thenReturn(false);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> examSectionService.create(request)
        );

        assertEquals(404, ex.getStatusCode().value());
        verify(examSectionRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("create -> throws not found when exam does not exist")
    void create_ShouldThrowNotFound_WhenExamDoesNotExist() {
        CreateExamSectionRequest request = new CreateExamSectionRequest(
                1,
                "Section",
                "Description",
                Duration.ofMinutes(90),
                examId,
                userId
        );

        when(examSectionRepository.existsByExamIdAndTitleIgnoreCase(examId, "Section"))
                .thenReturn(false);
        when(examSectionRepository.existsByExamIdAndOrderIndex(examId, 1))
                .thenReturn(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(examRepository.findDetailedById(examId)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> examSectionService.create(request)
        );

        assertEquals(404, ex.getStatusCode().value());
        verify(examSectionRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("getById -> returns mapped response")
    void getById_ShouldReturnMappedResponse() {
        ExamSectionResponse response = mock(ExamSectionResponse.class);

        when(examSectionRepository.findById(examSectionId)).thenReturn(Optional.of(examSection));
        when(examSectionMapper.toResponse(examSection)).thenReturn(response);

        ExamSectionResponse result = examSectionService.getById(examSectionId);

        assertEquals(response, result);
        verify(examSectionRepository).findById(examSectionId);
        verify(examSectionMapper).toResponse(examSection);
    }

    @Test
    @DisplayName("getById -> throws not found when exam section does not exist")
    void getById_ShouldThrowNotFound_WhenExamSectionDoesNotExist() {
        when(examSectionRepository.findById(examSectionId)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> examSectionService.getById(examSectionId)
        );

        assertEquals(404, ex.getStatusCode().value());
        verify(examSectionMapper, never()).toResponse(any());
    }

    @Test
    @DisplayName("getAll -> returns mapped list")
    void getAll_ShouldReturnMappedList() {
        ExamSection first = new ExamSection();
        ExamSection second = new ExamSection();

        ExamSectionResponse firstResponse = mock(ExamSectionResponse.class);
        ExamSectionResponse secondResponse = mock(ExamSectionResponse.class);

        when(examSectionRepository.findAll()).thenReturn(List.of(first, second));
        when(examSectionMapper.toResponseList(List.of(first, second)))
                .thenReturn(List.of(firstResponse, secondResponse));

        List<ExamSectionResponse> result = examSectionService.getAll();

        assertEquals(2, result.size());
        verify(examSectionRepository).findAll();
        verify(examSectionMapper).toResponseList(List.of(first, second));
    }

    @Test
    @DisplayName("getByExamId -> returns mapped sections by exam id")
    void getByExamId_ShouldReturnMappedSectionsByExamId() {
        ExamSection first = new ExamSection();
        ExamSection second = new ExamSection();

        ExamSectionResponse firstResponse = mock(ExamSectionResponse.class);
        ExamSectionResponse secondResponse = mock(ExamSectionResponse.class);

        when(examRepository.findDetailedById(examId)).thenReturn(Optional.of(exam));
        when(examSectionRepository.findByExamIdOrderByOrderIndexAsc(examId))
                .thenReturn(List.of(first, second));
        when(examSectionMapper.toResponseList(List.of(first, second)))
                .thenReturn(List.of(firstResponse, secondResponse));

        List<ExamSectionResponse> result = examSectionService.getByExamId(examId);

        assertEquals(2, result.size());
        verify(examRepository).findDetailedById(examId);
        verify(examSectionRepository).findByExamIdOrderByOrderIndexAsc(examId);
        verify(examSectionMapper).toResponseList(List.of(first, second));
    }

    @Test
    @DisplayName("update -> updates provided fields and returns response")
    void update_ShouldUpdateExamSectionAndReturnResponse() {
        UpdateExamSectionRequest request = new UpdateExamSectionRequest(
                2,
                "  Updated section  ",
                "  Updated description  ",
                Duration.ofMinutes(60)
        );

        ExamSectionResponse response = mock(ExamSectionResponse.class);

        when(examSectionRepository.findById(examSectionId)).thenReturn(Optional.of(examSection));
        when(examSectionRepository.existsByExamIdAndOrderIndexAndIdNot(examId, 2, examSectionId))
                .thenReturn(false);
        when(examSectionRepository.existsByExamIdAndTitleIgnoreCaseAndIdNot(examId, "Updated section", examSectionId))
                .thenReturn(false);
        when(examSectionRepository.saveAndFlush(any(ExamSection.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(examSectionMapper.toResponse(any(ExamSection.class))).thenReturn(response);

        ExamSectionResponse result = examSectionService.update(examSectionId, request);

        assertEquals(response, result);
        assertEquals(2, examSection.getOrderIndex());
        assertEquals("Updated section", examSection.getTitle());
        assertEquals("Updated description", examSection.getDescription());
        assertEquals(Duration.ofMinutes(60), examSection.getDuration());

        verify(examSectionRepository).saveAndFlush(examSection);
    }

    @Test
    @DisplayName("update -> throws conflict when new orderIndex already exists")
    void update_ShouldThrowConflict_WhenOrderIndexAlreadyExists() {
        UpdateExamSectionRequest request = new UpdateExamSectionRequest(
                2,
                null,
                null,
                null
        );

        when(examSectionRepository.findById(examSectionId)).thenReturn(Optional.of(examSection));
        when(examSectionRepository.existsByExamIdAndOrderIndexAndIdNot(examId, 2, examSectionId))
                .thenReturn(true);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> examSectionService.update(examSectionId, request)
        );

        assertEquals(409, ex.getStatusCode().value());
        verify(examSectionRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("update -> throws conflict when new title already exists")
    void update_ShouldThrowConflict_WhenTitleAlreadyExists() {
        UpdateExamSectionRequest request = new UpdateExamSectionRequest(
                null,
                "Existing section",
                null,
                null
        );

        when(examSectionRepository.findById(examSectionId)).thenReturn(Optional.of(examSection));
        when(examSectionRepository.existsByExamIdAndTitleIgnoreCaseAndIdNot(examId, "Existing section", examSectionId))
                .thenReturn(true);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> examSectionService.update(examSectionId, request)
        );

        assertEquals(409, ex.getStatusCode().value());
        verify(examSectionRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("delete -> deletes existing exam section")
    void delete_ShouldDeleteExamSection() {
        when(examSectionRepository.findById(examSectionId)).thenReturn(Optional.of(examSection));

        examSectionService.delete(examSectionId);

        verify(examSectionRepository).delete(examSection);
    }
}