package com.melikyan.academy.mapper;

import com.melikyan.academy.entity.Homework;
import org.mapstruct.NullValuePropertyMappingStrategy;
import com.melikyan.academy.dto.response.homework.HomeworkResponse;
import com.melikyan.academy.dto.request.homework.CreateHomeworkRequest;
import com.melikyan.academy.dto.request.homework.UpdateHomeworkRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface HomeworkMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "lesson", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "homeworkTasks", ignore = true)
    Homework toEntity(CreateHomeworkRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "lesson", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "homeworkTasks", ignore = true)
    void updateEntityFromRequest(UpdateHomeworkRequest request, @MappingTarget Homework homework);

    @Mapping(target = "lessonId", source = "lesson.id")
    @Mapping(target = "createdById", source = "createdBy.id")
    HomeworkResponse toResponse(Homework homework);

    List<HomeworkResponse> toResponseList(List<Homework> homeworks);
}
