package com.dorandoran.backend.domain.activity.dto;

import com.dorandoran.backend.domain.activity.ActivityRecord;

import java.time.LocalDateTime;
import java.util.UUID;

public record ActivityRecordSummaryResponse(
        UUID activityRecordId,
        UUID matchId,
        UUID scheduleId,
        UUID callLogId,
        UUID youthId,
        String youthName,
        UUID elderId,
        String elderName,
        boolean isCompleted,
        LocalDateTime actualStartAt,
        LocalDateTime actualEndAt,
        Integer durationMinutes,
        String notes,
        LocalDateTime createdAt
) {
    public static ActivityRecordSummaryResponse from(ActivityRecord record) {
        return new ActivityRecordSummaryResponse(
                record.getId(),
                record.getMatch().getId(),
                record.getSchedule().getId(),
                record.getCallLog() == null ? null : record.getCallLog().getId(),
                record.getYouth().getId(),
                record.getYouth().getName(),
                record.getElder().getId(),
                record.getElder().getName(),
                record.isCompleted(),
                record.getActualStartAt(),
                record.getActualEndAt(),
                record.getDurationMinutes(),
                record.getNotes(),
                record.getCreatedAt()
        );
    }

    public static ActivityRecordSummaryResponse forGuardian(ActivityRecord record) {
        return new ActivityRecordSummaryResponse(
                record.getId(),
                record.getMatch().getId(),
                record.getSchedule().getId(),
                record.getCallLog() == null ? null : record.getCallLog().getId(),
                record.getYouth().getId(),
                null,
                record.getElder().getId(),
                record.getElder().getName(),
                record.isCompleted(),
                record.getActualStartAt(),
                record.getActualEndAt(),
                record.getDurationMinutes(),
                null,
                record.getCreatedAt()
        );
    }
}
