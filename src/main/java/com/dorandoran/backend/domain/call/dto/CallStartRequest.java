package com.dorandoran.backend.domain.call.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CallStartRequest(
        @NotNull UUID matchId,
        UUID scheduleId
) {
}
