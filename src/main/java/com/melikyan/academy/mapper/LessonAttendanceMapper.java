package com.melikyan.academy.mapper;

import com.melikyan.academy.entity.LessonAttendance;
import com.melikyan.academy.dto.response.lessonAttendance.LessonAttendanceResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LessonAttendanceMapper {
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "lessonId", source = "lesson.id")
    LessonAttendanceResponse toResponse(LessonAttendance lessonAttendance);

    List<LessonAttendanceResponse> toResponseList(List<LessonAttendance> lessonAttendances);
}