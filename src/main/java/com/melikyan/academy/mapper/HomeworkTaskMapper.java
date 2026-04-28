package com.melikyan.academy.mapper;

import com.melikyan.academy.entity.HomeworkTask;
import org.mapstruct.NullValuePropertyMappingStrategy;
import com.melikyan.academy.dto.response.homeworkTask.HomeworkTaskResponse;
import com.melikyan.academy.dto.request.homeworkTask.CreateHomeworkTaskRequest;
import com.melikyan.academy.dto.request.homeworkTask.UpdateHomeworkTaskRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface HomeworkTaskMapper {
    @Mapping(target = "homeworkId", source = "homework.id")
    @Mapping(target = "createdById", source = "createdBy.id")
    HomeworkTaskResponse toResponse(HomeworkTask homeworkTask);

    List<HomeworkTaskResponse> toResponseList(List<HomeworkTask> homeworkTasks);
}
