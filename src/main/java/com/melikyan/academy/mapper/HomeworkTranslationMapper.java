package com.melikyan.academy.mapper;

import org.mapstruct.*;
import com.melikyan.academy.entity.HomeworkTranslation;
import com.melikyan.academy.dto.response.homeworkTranslation.HomeworkTranslationResponse;

import java.util.List;

@Mapper(componentModel = "spring")
public interface HomeworkTranslationMapper {
    @Mapping(target = "homeworkId", source = "homework.id")
    @Mapping(target = "createdById", source = "createdBy.id")
    HomeworkTranslationResponse toResponse(HomeworkTranslation translation);

    List<HomeworkTranslationResponse> toResponseList(List<HomeworkTranslation> translations);
}
