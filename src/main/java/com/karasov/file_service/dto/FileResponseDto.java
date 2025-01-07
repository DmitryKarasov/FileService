package com.karasov.file_service.dto;

public record FileResponseDto(
        String filename,
        long size
) {
}
