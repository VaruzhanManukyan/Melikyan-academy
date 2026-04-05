package com.melikyan.academy.mapper;

import com.melikyan.academy.entity.ExamSubmission;
import org.mapstruct.NullValuePropertyMappingStrategy;
import com.melikyan.academy.dto.response.examSubmission.ExamSubmissionResponse;
import com.melikyan.academy.dto.request.examSubmission.CreateExamSubmissionRequest;
import com.melikyan.academy.dto.request.examSubmission.UpdateExamSubmissionRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ExamSubmissionMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "task", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    ExamSubmission toEntity(CreateExamSubmissionRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "task", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateEntityFromRequest(UpdateExamSubmissionRequest request, @MappingTarget ExamSubmission examSubmission);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "taskId", source = "task.id")
    ExamSubmissionResponse toResponse(ExamSubmission examSubmission);

    List<ExamSubmissionResponse> toResponseList(List<ExamSubmission> examSubmissions);
}
