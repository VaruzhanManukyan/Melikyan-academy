package com.melikyan.academy.mapper;

import com.melikyan.academy.entity.Course;
import org.mapstruct.NullValuePropertyMappingStrategy;
import com.melikyan.academy.dto.response.course.CourseResponse;
import com.melikyan.academy.dto.request.course.CreateCourseRequest;
import com.melikyan.academy.dto.request.course.UpdateCourseRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CourseMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "purchasable", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "professors", ignore = true)
    @Mapping(target = "lessons", ignore = true)
    Course toEntity(CreateCourseRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "purchasable", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "professors", ignore = true)
    @Mapping(target = "lessons", ignore = true)
    void updateEntityFromRequest(UpdateCourseRequest request, @MappingTarget Course course);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "purchasableId", source = "purchasable.id")
    @Mapping(target = "title", source = "purchasable.title")
    @Mapping(target = "description", source = "purchasable.description")
    @Mapping(target = "type", source = "purchasable.type")
    @Mapping(target = "categoryId", source = "purchasable.category.id")
    @Mapping(target = "createdById", source = "purchasable.createdBy.id")
    @Mapping(target = "startDate", source = "startDate")
    @Mapping(target = "durationWeeks", source = "durationWeeks")
    @Mapping(target = "createdAt", source = "purchasable.createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    CourseResponse toResponse(Course course);

    List<CourseResponse> toResponseList(List<Course> courses);
}