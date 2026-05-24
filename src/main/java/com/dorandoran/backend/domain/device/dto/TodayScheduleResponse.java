package com.dorandoran.backend.domain.device.dto;

import com.dorandoran.backend.domain.elder.CallType;
import com.dorandoran.backend.domain.schedule.Schedule;

import java.time.LocalDateTime;
import java.util.UUID;

public record TodayScheduleResponse(
        UUID scheduleId,
        UUID matchId,
        LocalDateTime scheduledStartAt,
        LocalDateTime scheduledEndAt,
        CallType callType,
        String youthName
) {
    public static TodayScheduleResponse from(Schedule schedule) {
        return new TodayScheduleResponse(
                schedule.getId(),
                schedule.getMatch().getId(),
                schedule.getScheduledStartAt(),
                schedule.getScheduledEndAt(),
                schedule.getMatch().getElder().getPreferredCallType(),
                schedule.getMatch().getYouth().getName()
        );
    }
}
