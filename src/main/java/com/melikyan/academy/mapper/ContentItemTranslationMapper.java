package com.melikyan.academy.mapper;

import org.mapstruct.*;
import com.melikyan.academy.entity.ContentItemTranslation;
import com.melikyan.academy.dto.response.contentItemTranslation.ContentItemTranslationResponse;
import com.melikyan.academy.dto.request.contentItemTranslation.CreateContentItemTranslationRequest;
import com.melikyan.academy.dto.request.contentItemTranslation.UpdateContentItemTranslationRequest;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ContentItemTranslationMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "contentItem", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ContentItemTranslation toEntity(CreateContentItemTranslationRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "contentItem", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(UpdateContentItemTranslationRequest request, @MappingTarget ContentItemTranslation translation);

    @Mapping(target = "contentItemId", source = "contentItem.id")
    @Mapping(target = "createdById", source = "createdBy.id")
    ContentItemTranslationResponse toResponse(ContentItemTranslation translation);

    List<ContentItemTranslationResponse> toResponseList(List<ContentItemTranslation> translations);
}