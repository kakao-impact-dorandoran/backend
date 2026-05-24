package com.dorandoran.backend.domain.report.dto;

import com.dorandoran.backend.domain.report.ReportType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ReportCreateRequest(
        UUID matchId,
        UUID scheduleId,
        UUID targetUserId,
        UUID targetElderId,
        @NotNull ReportType reportType,
        @NotBlank String content
) {
}
