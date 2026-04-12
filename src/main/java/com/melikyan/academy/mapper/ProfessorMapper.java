package com.melikyan.academy.mapper;

import com.melikyan.academy.entity.Professor;
import org.mapstruct.NullValuePropertyMappingStrategy;
import com.melikyan.academy.dto.response.professor.ProfessorResponse;
import com.melikyan.academy.dto.request.professor.CreateProfessorRequest;
import com.melikyan.academy.dto.request.professor.UpdateProfessorRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProfessorMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "course", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Professor toEntity(CreateProfessorRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "course", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)

    void updateEntityFromRequest(UpdateProfessorRequest request, @MappingTarget Professor professor);
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "courseId", source = "course.id")
    ProfessorResponse toResponse(Professor professor);

    List<ProfessorResponse> toResponseList(List<Professor> professors);
}
