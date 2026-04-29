package com.melikyan.academy.mapper;

import com.melikyan.academy.entity.Language;
import com.melikyan.academy.dto.response.language.LanguageResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LanguageMapper {
    List<LanguageResponse> toResponseList(List<Language> languages);

    @Mapping(target = "createdById", source = "createdBy.id")
    LanguageResponse toResponse(Language language);
}
