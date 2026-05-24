package com.dorandoran.backend.domain.schedule.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record ScheduleCreateRequest(
        @NotNull(message = "matchId는 필수입니다.")
        UUID matchId,
        @NotNull(message = "scheduledStartAt은 필수입니다.")
        LocalDateTime scheduledStartAt,
        @NotNull(message = "scheduledEndAt은 필수입니다.")
        LocalDateTime scheduledEndAt
) {
}
