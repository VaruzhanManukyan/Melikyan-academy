package com.melikyan.academy.mapper;

import com.melikyan.academy.entity.Course;
import com.melikyan.academy.dto.response.course.CourseResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CourseMapper {
    @Mapping(target = "id", source = "id")
    @Mapping(target = "contentItemId", source = "contentItem.id")
    @Mapping(target = "title", source = "contentItem.title")
    @Mapping(target = "description", source = "contentItem.description")
    @Mapping(target = "type", source = "contentItem.type")
    @Mapping(target = "createdById", source = "contentItem.createdBy.id")
    @Mapping(target = "startDate", source = "startDate")
    @Mapping(target = "durationWeeks", source = "durationWeeks")
    @Mapping(target = "createdAt", source = "contentItem.createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    CourseResponse toResponse(Course course);

    List<CourseResponse> toResponseList(List<Course> courses);
}