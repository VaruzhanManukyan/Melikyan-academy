package com.melikyan.academy.mapper;

import com.melikyan.academy.entity.HomeworkSubmission;
import com.melikyan.academy.dto.response.homeworkSubmission.HomeworkSubmissionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface HomeworkSubmissionMapper {
    @Mapping(target = "note", source = "note")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "taskId", source = "task.id")
    HomeworkSubmissionResponse toResponse(HomeworkSubmission homeworkSubmission);

    List<HomeworkSubmissionResponse> toResponseList(List<HomeworkSubmission> homeworkSubmissions);
}
