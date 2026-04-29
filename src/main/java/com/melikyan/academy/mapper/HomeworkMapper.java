package com.melikyan.academy.mapper;

import com.melikyan.academy.entity.Homework;
import com.melikyan.academy.dto.response.homework.HomeworkResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface HomeworkMapper {
    @Mapping(target = "lessonId", source = "lesson.id")
    @Mapping(target = "createdById", source = "createdBy.id")
    HomeworkResponse toResponse(Homework homework);

    List<HomeworkResponse> toResponseList(List<Homework> homeworks);
}
