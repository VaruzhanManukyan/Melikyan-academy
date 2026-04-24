package com.melikyan.academy.mapper;

import com.melikyan.academy.dto.request.homeworkSubmission.UpdateHomeworkSubmissionRequest;
import com.melikyan.academy.entity.HomeworkSubmission;
import org.mapstruct.NullValuePropertyMappingStrategy;
import com.melikyan.academy.dto.response.homeworkSubmission.HomeworkSubmissionResponse;
import com.melikyan.academy.dto.request.homeworkSubmission.CreateHomeworkSubmissionRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface HomeworkSubmissionMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "task", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    HomeworkSubmission toEntity(CreateHomeworkSubmissionRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "task", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(UpdateHomeworkSubmissionRequest request, @MappingTarget HomeworkSubmission homeworkSubmission);

    @Mapping(target = "note", source = "note")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "taskId", source = "task.id")
    HomeworkSubmissionResponse toResponse(HomeworkSubmission homeworkSubmission);

    List<HomeworkSubmissionResponse> toResponseList(List<HomeworkSubmission> homeworkSubmissions);
}
