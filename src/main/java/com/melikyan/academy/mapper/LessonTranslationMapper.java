package com.melikyan.academy.mapper;

import org.mapstruct.*;
import com.melikyan.academy.entity.LessonTranslation;
import com.melikyan.academy.dto.response.lessonTranslation.LessonTranslationResponse;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LessonTranslationMapper {
    @Mapping(target = "lessonId", source = "lesson.id")
    @Mapping(target = "createdById", source = "createdBy.id")
    LessonTranslationResponse toResponse(LessonTranslation translation);

    List<LessonTranslationResponse> toResponseList(List<LessonTranslation> translations);
}
