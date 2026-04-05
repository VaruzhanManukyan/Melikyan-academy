package com.melikyan.academy.mapper;

import com.melikyan.academy.entity.LessonAttendance;
import org.mapstruct.NullValuePropertyMappingStrategy;
import com.melikyan.academy.dto.response.lessonAttendance.LessonAttendanceResponse;
import com.melikyan.academy.dto.request.lessonAttendance.CreateLessonAttendanceRequest;
import com.melikyan.academy.dto.request.lessonAttendance.UpdateLessonAttendanceRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LessonAttendanceMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "lesson", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    LessonAttendance toEntity(CreateLessonAttendanceRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "lesson", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateEntityFromRequest(UpdateLessonAttendanceRequest request, @MappingTarget LessonAttendance lessonAttendance);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "lessonId", source = "lesson.id")
    LessonAttendanceResponse toResponse(LessonAttendance lessonAttendance);

    List<LessonAttendanceResponse> toResponseList(List<LessonAttendance> lessonAttendances);
}