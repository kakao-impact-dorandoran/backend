package com.dorandoran.backend.domain.report.dto;

import com.dorandoran.backend.domain.report.Report;
import com.dorandoran.backend.domain.report.ReportStatus;
import com.dorandoran.backend.domain.report.ReportType;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReportResponse(
        UUID reportId,
        UUID reporterUserId,
        UUID targetUserId,
        UUID targetElderId,
        UUID matchId,
        UUID scheduleId,
        ReportType reportType,
        String content,
        ReportStatus status,
        LocalDateTime createdAt
) {
    public static ReportResponse from(Report report) {
        return new ReportResponse(
                report.getId(),
                report.getReporterUser() != null ? report.getReporterUser().getId() : null,
                report.getTargetUser() != null ? report.getTargetUser().getId() : null,
                report.getTargetElder() != null ? report.getTargetElder().getId() : null,
                report.getMatch() != null ? report.getMatch().getId() : null,
                report.getSchedule() != null ? report.getSchedule().getId() : null,
                report.getReportType(),
                report.getContent(),
                report.getStatus(),
                report.getCreatedAt()
        );
    }
}
