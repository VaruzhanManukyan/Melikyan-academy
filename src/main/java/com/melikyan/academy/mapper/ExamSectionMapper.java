package com.melikyan.academy.mapper;

import com.melikyan.academy.entity.ExamSection;
import org.mapstruct.NullValuePropertyMappingStrategy;
import com.melikyan.academy.dto.response.examSection.ExamSectionResponse;
import com.melikyan.academy.dto.request.examSection.CreateExamSectionRequest;
import com.melikyan.academy.dto.request.examSection.UpdateExamSectionRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ExamSectionMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "exam", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "examTasks", ignore = true)
    @Mapping(target = "sections", ignore = true)
    ExamSection toEntity(CreateExamSectionRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "exam", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "examTasks", ignore = true)
    @Mapping(target = "sections", ignore = true)
    void updateEntityFromRequest(UpdateExamSectionRequest request, @MappingTarget ExamSection examSection);

    @Mapping(target = "examId", source = "exam.id")
    @Mapping(target = "createdById", source = "createdBy.id")
    ExamSectionResponse toResponse(ExamSection examSection);

    List<ExamSectionResponse> toResponseList(List<ExamSection> examSections);
}
