package com.melikyan.academy.mapper;

import org.mapstruct.*;
import com.melikyan.academy.entity.SectionTranslation;
import com.melikyan.academy.dto.response.sectionTranslation.SectionTranslationResponse;
import com.melikyan.academy.dto.request.sectionTranslation.CreateSectionTranslationRequest;
import com.melikyan.academy.dto.request.sectionTranslation.UpdateSectionTranslationRequest;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SectionTranslationMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "examSection", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    SectionTranslation toEntity(CreateSectionTranslationRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "examSection", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(UpdateSectionTranslationRequest request, @MappingTarget SectionTranslation translation);

    @Mapping(target = "examSectionId", source = "examSection.id")
    @Mapping(target = "createdById", source = "createdBy.id")
    SectionTranslationResponse toResponse(SectionTranslation translation);

    List<SectionTranslationResponse> toResponseList(List<SectionTranslation> translations);
}
