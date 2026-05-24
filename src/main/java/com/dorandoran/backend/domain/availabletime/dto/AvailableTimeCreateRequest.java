package com.dorandoran.backend.domain.availabletime.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record AvailableTimeCreateRequest(
        @NotNull(message = "startTime은 필수입니다.")
        LocalDateTime startTime,
        @NotNull(message = "endTime은 필수입니다.")
        LocalDateTime endTime
) {
}
