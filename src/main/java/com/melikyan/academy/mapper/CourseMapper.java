package com.melikyan.academy.mapper;

import com.melikyan.academy.entity.Course;
import org.mapstruct.NullValuePropertyMappingStrategy;
import com.melikyan.academy.dto.response.course.CourseResponse;
import com.melikyan.academy.dto.request.course.UpdateCourseRequest;
import com.melikyan.academy.dto.request.course.CreateCourseRequest;
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

    @Mapping(target = "purchasableId", source = "purchasable.id")
    CourseResponse toResponse(Course course);

    List<CourseResponse> toResponseList(List<Course> courses);
}
