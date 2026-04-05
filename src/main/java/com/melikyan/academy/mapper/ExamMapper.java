package com.melikyan.academy.mapper;

import com.melikyan.academy.entity.Exam;
import org.mapstruct.NullValuePropertyMappingStrategy;
import com.melikyan.academy.dto.response.exam.ExamResponse;
import com.melikyan.academy.dto.request.exam.CreateExamRequest;
import com.melikyan.academy.dto.request.exam.UpdateExamRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ExamMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "purchasable", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "examSections", ignore = true)
    Exam toEntity(CreateExamRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "purchasable", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "examSections", ignore = true)
    void updateEntityFromRequest(UpdateExamRequest request, @MappingTarget Exam exam);

    @Mapping(target = "purchasableId", source = "purchasable.id")
    ExamResponse toResponse(Exam exam);

    List<ExamResponse> toResponseList(List<Exam> exams);
}
