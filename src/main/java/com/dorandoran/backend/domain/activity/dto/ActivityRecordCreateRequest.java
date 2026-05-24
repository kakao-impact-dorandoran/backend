package com.dorandoran.backend.domain.activity.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record ActivityRecordCreateRequest(
        @NotNull(message = "matchId는 필수입니다.")
        UUID matchId,
        @NotNull(message = "scheduleId는 필수입니다.")
        UUID scheduleId,
        UUID callLogId,
        boolean isCompleted,
        LocalDateTime actualStartAt,
        LocalDateTime actualEndAt,
        Integer durationMinutes,
        String notes
) {
}
