package com.melikyan.academy.mapper;

import org.mapstruct.*;
import com.melikyan.academy.entity.SectionTranslation;
import com.melikyan.academy.dto.response.sectionTranslation.SectionTranslationResponse;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SectionTranslationMapper {
    @Mapping(target = "examSectionId", source = "examSection.id")
    @Mapping(target = "createdById", source = "createdBy.id")
    SectionTranslationResponse toResponse(SectionTranslation translation);

    List<SectionTranslationResponse> toResponseList(List<SectionTranslation> translations);
}
