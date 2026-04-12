package com.melikyan.academy.mapper;

import com.melikyan.academy.dto.request.examTask.CreateExamTaskRequest;
import com.melikyan.academy.dto.request.examTask.UpdateExamTaskRequest;
import com.melikyan.academy.dto.response.examTask.ExamTaskResponse;
import com.melikyan.academy.entity.ExamTask;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ExamTaskMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "section", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "examSubmissions", ignore = true)
    ExamTask toEntity(CreateExamTaskRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "section", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "examSubmissions", ignore = true)
    void updateEntityFromRequest(UpdateExamTaskRequest request, @MappingTarget ExamTask examTask);

    @Mapping(target = "sectionId", source = "section.id")
    @Mapping(target = "createdById", source = "createdBy.id")
    ExamTaskResponse toResponse(ExamTask examTask);

    List<ExamTaskResponse> toResponseList(List<ExamTask> examTasks);
}