package com.melikyan.academy.mapper;

import com.melikyan.academy.entity.Language;
import org.mapstruct.NullValuePropertyMappingStrategy;
import com.melikyan.academy.dto.response.language.LanguageResponse;
import com.melikyan.academy.dto.request.language.UpdateLanguageRequest;
import com.melikyan.academy.dto.request.language.CreateLanguageRequest;
import org.mapstruct.Mapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.MappingTarget;

import java.util.List;

public interface LanguageMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    Language toEntity(CreateLanguageRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    void updateEntity(@MappingTarget Language entity, UpdateLanguageRequest request);

    List<LanguageResponse> toResponseList(List<Language> languages);

    @Mapping(target = "createdById", source = "createdBy.id")
    LanguageResponse toResponse(Language language);
}
