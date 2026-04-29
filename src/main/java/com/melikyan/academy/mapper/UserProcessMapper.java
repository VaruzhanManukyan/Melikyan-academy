package com.melikyan.academy.mapper;

import com.melikyan.academy.entity.UserProcess;
import com.melikyan.academy.dto.response.userProcess.UserProcessResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserProcessMapper {
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "contentItemId", source = "contentItem.id")
    UserProcessResponse toResponse(UserProcess userProcess);

    List<UserProcessResponse> toResponseList(List<UserProcess> userProcesses);
}
