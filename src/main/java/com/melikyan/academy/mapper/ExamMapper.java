package com.melikyan.academy.mapper;

import com.melikyan.academy.entity.Exam;
import com.melikyan.academy.dto.response.exam.ExamResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ExamMapper {
    @Mapping(target = "id", source = "id")
    @Mapping(target = "contentItemId", source = "contentItem.id")
    @Mapping(target = "title", source = "contentItem.title")
    @Mapping(target = "description", source = "contentItem.description")
    @Mapping(target = "type", source = "contentItem.type")
    @Mapping(target = "createdById", source = "contentItem.createdBy.id")
    @Mapping(target = "createdAt", source = "contentItem.createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    ExamResponse toResponse(Exam exam);

    List<ExamResponse> toResponseList(List<Exam> exams);
}
