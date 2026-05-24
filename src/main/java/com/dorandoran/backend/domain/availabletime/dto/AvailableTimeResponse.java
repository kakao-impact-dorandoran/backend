package com.dorandoran.backend.domain.availabletime.dto;

import com.dorandoran.backend.domain.availabletime.AvailableTime;
import com.dorandoran.backend.domain.availabletime.AvailableTimeOwnerType;

import java.time.LocalDateTime;
import java.util.UUID;

public record AvailableTimeResponse(
        UUID availableTimeId,
        AvailableTimeOwnerType ownerType,
        UUID ownerId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        boolean isBooked
) {
    public static AvailableTimeResponse from(AvailableTime at) {
        return new AvailableTimeResponse(
                at.getId(),
                at.getOwnerType(),
                at.getOwnerId(),
                at.getStartTime(),
                at.getEndTime(),
                at.isBooked()
        );
    }
}
