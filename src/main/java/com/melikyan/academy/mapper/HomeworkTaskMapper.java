package com.melikyan.academy.mapper;

import com.melikyan.academy.entity.HomeworkTask;
import com.melikyan.academy.dto.response.homeworkTask.HomeworkTaskResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface HomeworkTaskMapper {
    @Mapping(target = "lessonId", source = "lesson.id")
    @Mapping(target = "createdById", source = "createdBy.id")
    HomeworkTaskResponse toResponse(HomeworkTask homeworkTask);

    List<HomeworkTaskResponse> toResponseList(List<HomeworkTask> homeworkTasks);
}
