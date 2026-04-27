package com.melikyan.academy.mapper;

import com.melikyan.academy.entity.ExamSection;
import com.melikyan.academy.dto.response.examSection.ExamSectionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ExamSectionMapper {
    @Mapping(target = "examId", source = "exam.id")
    @Mapping(target = "createdById", source = "createdBy.id")
    ExamSectionResponse toResponse(ExamSection examSection);

    List<ExamSectionResponse> toResponseList(List<ExamSection> examSections);
}
