package com.dorandoran.backend.domain.schedule.dto;

import com.dorandoran.backend.domain.match.Match;
import com.dorandoran.backend.domain.schedule.Schedule;
import com.dorandoran.backend.domain.schedule.ScheduleStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record ScheduleResponse(
        UUID scheduleId,
        UUID matchId,
        UUID youthId,
        String youthName,
        UUID elderId,
        String elderName,
        LocalDateTime scheduledStartAt,
        LocalDateTime scheduledEndAt,
        ScheduleStatus status,
        String cancelReason
) {
    public static ScheduleResponse from(Schedule schedule) {
        Match match = schedule.getMatch();
        return new ScheduleResponse(
                schedule.getId(),
                match.getId(),
                match.getYouth().getId(),
                match.getYouth().getName(),
                match.getElder().getId(),
                match.getElder().getName(),
                schedule.getScheduledStartAt(),
                schedule.getScheduledEndAt(),
                schedule.getStatus(),
                schedule.getCancelReason()
        );
    }
}
