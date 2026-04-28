package com.melikyan.academy.mapper;

import com.melikyan.academy.entity.ExamTask;
import com.melikyan.academy.dto.response.examTask.ExamTaskResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ExamTaskMapper {
    @Mapping(target = "sectionId", source = "section.id")
    @Mapping(target = "createdById", source = "createdBy.id")
    ExamTaskResponse toResponse(ExamTask examTask);

    List<ExamTaskResponse> toResponseList(List<ExamTask> examTasks);
}