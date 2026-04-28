package com.melikyan.academy.mapper;

import com.melikyan.academy.entity.HomeworkTask;
import com.melikyan.academy.dto.response.homeworkTask.HomeworkTaskResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface HomeworkTaskMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "homework", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "homeworkSubmissions", ignore = true)
    HomeworkTask toEntity(CreateHomeworkTaskRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "homework", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "homeworkSubmissions", ignore = true)
    void updateEntityFromRequest(UpdateHomeworkTaskRequest request, @MappingTarget HomeworkTask homeworkTask);

    @Mapping(target = "homeworkId", source = "homework.id")
    @Mapping(target = "createdById", source = "createdBy.id")
    HomeworkTaskResponse toResponse(HomeworkTask homeworkTask);

    List<HomeworkTaskResponse> toResponseList(List<HomeworkTask> homeworkTasks);
}
