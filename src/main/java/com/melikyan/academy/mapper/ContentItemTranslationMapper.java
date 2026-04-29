package com.melikyan.academy.mapper;

import org.mapstruct.*;
import com.melikyan.academy.entity.ContentItemTranslation;
import com.melikyan.academy.dto.response.contentItemTranslation.ContentItemTranslationResponse;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ContentItemTranslationMapper {
    @Mapping(target = "contentItemId", source = "contentItem.id")
    @Mapping(target = "createdById", source = "createdBy.id")
    ContentItemTranslationResponse toResponse(ContentItemTranslation translation);

    List<ContentItemTranslationResponse> toResponseList(List<ContentItemTranslation> translations);
}