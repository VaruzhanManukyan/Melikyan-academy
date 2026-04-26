package com.melikyan.academy.mapper;

import com.melikyan.academy.entity.User;
import com.melikyan.academy.entity.Professor;
import com.melikyan.academy.dto.response.professor.ProfessorResponse;
import com.melikyan.academy.dto.response.professor.ProfessorUserData;
import com.melikyan.academy.dto.response.professor.ProfessorUserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProfessorMapper {
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "courseId", source = "course.id")
    ProfessorResponse toResponse(Professor professor);

    List<ProfessorResponse> toResponseList(List<Professor> professors);

    List<ProfessorUserData> toUserDataList(List<User> users);

    @Mapping(target = "message", constant = "Registration successful")
    @Mapping(target = "professor", source = ".")
    ProfessorUserResponse toRegisterResponse(User user);
}