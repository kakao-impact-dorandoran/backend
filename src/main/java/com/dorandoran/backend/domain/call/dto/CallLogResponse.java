package com.dorandoran.backend.domain.call.dto;

import com.dorandoran.backend.domain.call.CallLog;
import com.dorandoran.backend.domain.call.CallLogStatus;
import com.dorandoran.backend.domain.elder.CallType;
import com.dorandoran.backend.domain.schedule.Schedule;

import java.time.LocalDateTime;
import java.util.UUID;

public record CallLogResponse(
        UUID callLogId,
        UUID matchId,
        UUID scheduleId,
        CallType callType,
        CallLogStatus status,
        LocalDateTime startAt,
        LocalDateTime endAt,
        LocalDateTime createdAt
) {
    public static CallLogResponse from(CallLog callLog) {
        Schedule schedule = callLog.getSchedule();
        return new CallLogResponse(
                callLog.getId(),
                callLog.getMatch().getId(),
                schedule == null ? null : schedule.getId(),
                callLog.getCallType(),
                callLog.getStatus(),
                callLog.getStartAt(),
                callLog.getEndAt(),
                callLog.getCreatedAt()
        );
    }
}
