package com.karasov.file_service.dto;

public record ErrorResponseDto(
        String message,
        int id
) {
}
