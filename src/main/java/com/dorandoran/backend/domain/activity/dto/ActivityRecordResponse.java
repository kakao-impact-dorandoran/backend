package com.dorandoran.backend.domain.activity.dto;

import com.dorandoran.backend.domain.activity.ActivityRecord;

import java.util.UUID;

public record ActivityRecordResponse(
        UUID activityRecordId,
        Integer durationMinutes,
        int totalDurationMinutes
) {
    public static ActivityRecordResponse from(ActivityRecord record, int totalDurationMinutes) {
        return new ActivityRecordResponse(
                record.getId(),
                record.getDurationMinutes(),
                totalDurationMinutes
        );
    }
}
