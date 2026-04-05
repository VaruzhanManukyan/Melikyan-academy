package com.melikyan.academy.mapper;

import org.mapstruct.*;
import com.melikyan.academy.entity.LessonTranslation;
import com.melikyan.academy.dto.response.lessonTranslation.LessonTranslationResponse;
import com.melikyan.academy.dto.request.lessonTranslation.CreateLessonTranslationRequest;
import com.melikyan.academy.dto.request.lessonTranslation.UpdateLessonTranslationRequest;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LessonTranslationMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "lesson", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    LessonTranslation toEntity(CreateLessonTranslationRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "lesson", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(UpdateLessonTranslationRequest request, @MappingTarget LessonTranslation translation);

    @Mapping(target = "lessonId", source = "lesson.id")
    @Mapping(target = "createdById", source = "createdBy.id")
    LessonTranslationResponse toResponse(LessonTranslation translation);

    List<LessonTranslationResponse> toResponseList(List<LessonTranslation> translations);
}
