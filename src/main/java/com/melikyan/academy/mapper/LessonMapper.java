package com.melikyan.academy.mapper;

import com.melikyan.academy.entity.Lesson;
import com.melikyan.academy.dto.response.lesson.LessonResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LessonMapper {
    @Mapping(target = "courseId", source = "course.id")
    @Mapping(target = "createdById", source = "createdBy.id")
    LessonResponse toResponse(Lesson lesson);

    List<LessonResponse> toResponseList(List<Lesson> lessons);

}
