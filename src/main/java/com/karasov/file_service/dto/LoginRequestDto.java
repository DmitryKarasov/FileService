package com.karasov.file_service.dto;

public record LoginRequestDto(
        String login,
        String password
) {
}
