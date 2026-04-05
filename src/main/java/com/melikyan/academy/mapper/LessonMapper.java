package com.melikyan.academy.mapper;

import com.melikyan.academy.entity.Lesson;
import org.mapstruct.NullValuePropertyMappingStrategy;
import com.melikyan.academy.dto.response.lesson.LessonResponse;
import com.melikyan.academy.dto.request.lesson.CreateLessonRequest;
import com.melikyan.academy.dto.request.lesson.UpdateLessonRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LessonMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "course", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "lessonAttendances", ignore = true)
    @Mapping(target = "lessonTranslations", ignore = true)
    Lesson toEntity(CreateLessonRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "course", ignore = true)
    @Mapping(target = "lessonAttendances", ignore = true)
    @Mapping(target = "lessonTranslations", ignore = true)
    void updateEntityFromRequest(UpdateLessonRequest request, @MappingTarget Lesson lesson);

    @Mapping(target = "courseId", source = "course.id")
    @Mapping(target = "createdById", source = "createdBy.id")
    LessonResponse toResponse(Lesson lesson);

    List<LessonResponse> toResponseList(List<Lesson> lessons);

}
