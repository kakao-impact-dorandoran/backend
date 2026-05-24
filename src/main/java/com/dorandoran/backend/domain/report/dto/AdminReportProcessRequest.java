package com.dorandoran.backend.domain.report.dto;

import com.dorandoran.backend.domain.report.ReportStatus;
import jakarta.validation.constraints.NotNull;

public record AdminReportProcessRequest(
        @NotNull ReportStatus status,
        String adminMemo
) {
}
