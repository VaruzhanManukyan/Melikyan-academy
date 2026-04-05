package com.melikyan.academy.mapper;

import com.melikyan.academy.dto.request.homeworkTranslation.CreateHomeworkTranslationRequest;
import com.melikyan.academy.dto.request.homeworkTranslation.UpdateHomeworkTranslationRequest;
import com.melikyan.academy.dto.response.homeworkTranslation.HomeworkTranslationResponse;
import com.melikyan.academy.entity.HomeworkTranslation;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface HomeworkTranslationMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "homework", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    HomeworkTranslation toEntity(CreateHomeworkTranslationRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "homework", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(UpdateHomeworkTranslationRequest request, @MappingTarget HomeworkTranslation translation);

    @Mapping(target = "homeworkId", source = "homework.id")
    @Mapping(target = "createdById", source = "createdBy.id")
    HomeworkTranslationResponse toResponse(HomeworkTranslation translation);

    List<HomeworkTranslationResponse> toResponseList(List<HomeworkTranslation> translations);
}
