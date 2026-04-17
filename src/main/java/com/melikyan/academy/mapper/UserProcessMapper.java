package com.melikyan.academy.mapper;

import com.melikyan.academy.entity.UserProcess;
import org.mapstruct.NullValuePropertyMappingStrategy;
import com.melikyan.academy.dto.response.userProcess.UserProcessResponse;
import com.melikyan.academy.dto.request.userProcess.CreateUserProcessRequest;
import com.melikyan.academy.dto.request.userProcess.UpdateUserProcessRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserProcessMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "contentItem", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    UserProcess toEntity(CreateUserProcessRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "contentItem", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(UpdateUserProcessRequest request, @MappingTarget UserProcess userProcess);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "contentItemId", source = "contentItem.id")
    UserProcessResponse toResponse(UserProcess userProcess);

    List<UserProcessResponse> toResponseList(List<UserProcess> userProcesses);
}
