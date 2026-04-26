package com.melikyan.academy.dto.response.professor;

public record ProfessorUserResponse(
        String message,
        ProfessorUserData professor
) {
}