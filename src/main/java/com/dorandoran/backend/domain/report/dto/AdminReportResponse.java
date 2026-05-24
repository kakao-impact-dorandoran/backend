package com.dorandoran.backend.domain.report.dto;

import com.dorandoran.backend.domain.report.Report;
import com.dorandoran.backend.domain.report.ReportStatus;
import com.dorandoran.backend.domain.report.ReportType;

import java.time.LocalDateTime;
import java.util.UUID;

public record AdminReportResponse(
        UUID reportId,
        UUID reporterUserId,
        String reporterUserName,
        UUID targetUserId,
        String targetUserName,
        UUID targetElderId,
        String targetElderName,
        UUID matchId,
        UUID scheduleId,
        ReportType reportType,
        String content,
        ReportStatus status,
        UUID adminId,
        String adminMemo,
        LocalDateTime createdAt,
        LocalDateTime resolvedAt
) {
    public static AdminReportResponse from(Report report) {
        return new AdminReportResponse(
                report.getId(),
                report.getReporterUser() != null ? report.getReporterUser().getId() : null,
                report.getReporterUser() != null ? report.getReporterUser().getName() : null,
                report.getTargetUser() != null ? report.getTargetUser().getId() : null,
                report.getTargetUser() != null ? report.getTargetUser().getName() : null,
                report.getTargetElder() != null ? report.getTargetElder().getId() : null,
                report.getTargetElder() != null ? report.getTargetElder().getName() : null,
                report.getMatch() != null ? report.getMatch().getId() : null,
                report.getSchedule() != null ? report.getSchedule().getId() : null,
                report.getReportType(),
                report.getContent(),
                report.getStatus(),
                report.getAdmin() != null ? report.getAdmin().getId() : null,
                report.getAdminMemo(),
                report.getCreatedAt(),
                report.getResolvedAt()
        );
    }
}
