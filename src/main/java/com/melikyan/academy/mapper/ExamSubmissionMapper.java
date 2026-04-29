package com.melikyan.academy.mapper;

import com.melikyan.academy.entity.ExamSubmission;
import com.melikyan.academy.dto.response.examSubmission.ExamSubmissionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ExamSubmissionMapper {
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "taskId", source = "task.id")
    ExamSubmissionResponse toResponse(ExamSubmission examSubmission);

    List<ExamSubmissionResponse> toResponseList(List<ExamSubmission> examSubmissions);
}
