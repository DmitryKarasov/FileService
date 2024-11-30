package com.karasov.file_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LoginResponseDto(
        @JsonProperty("auth-token") String authToken
) {
}
