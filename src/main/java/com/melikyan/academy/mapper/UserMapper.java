package com.melikyan.academy.mapper;

import org.mapstruct.*;
import com.melikyan.academy.entity.User;
import com.melikyan.academy.dto.response.auth.LoginResponse;
import com.melikyan.academy.dto.request.auth.RegisterRequest;
import com.melikyan.academy.dto.response.auth.RegisterResponse;
import com.melikyan.academy.dto.response.user.UserProfileResponse;
import com.melikyan.academy.dto.request.user.UpdateProfileRequest;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "avatarUrl", ignore = true)
    @Mapping(target = "bio", ignore = true)
    @Mapping(target = "userProcesses", ignore = true)
    @Mapping(target = "professors", ignore = true)
    @Mapping(target = "certificates", ignore = true)
    @Mapping(target = "transactions", ignore = true)
    @Mapping(target = "productRegistrations", ignore = true)
    @Mapping(target = "homeworkSubmissions", ignore = true)
    @Mapping(target = "lessonAttendances", ignore = true)
    @Mapping(target = "examSubmissions", ignore = true)
    User toEntity(RegisterRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "avatarUrl", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "userProcesses", ignore = true)
    @Mapping(target = "professors", ignore = true)
    @Mapping(target = "certificates", ignore = true)
    @Mapping(target = "transactions", ignore = true)
    @Mapping(target = "productRegistrations", ignore = true)
    @Mapping(target = "homeworkSubmissions", ignore = true)
    @Mapping(target = "lessonAttendances", ignore = true)
    @Mapping(target = "examSubmissions", ignore = true)
    void updateProfile(@MappingTarget User user, UpdateProfileRequest request);

    UserProfileResponse toProfileResponse(User user);

    List<UserProfileResponse> toResponseList(List<User> users);

    @Mapping(target = "authenticated", constant = "true")
    @Mapping(target = "message", constant = "Login successful")
    @Mapping(target = "user", source = ".")
    LoginResponse toLoginResponse(User user);

    @Mapping(target = "message", constant = "Registration successful")
    RegisterResponse toRegisterResponse(User user);
}