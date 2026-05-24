package com.dorandoran.backend.domain.termination.dto;

import com.dorandoran.backend.domain.termination.MatchTerminationRequestStatus;
import jakarta.validation.constraints.NotNull;

public record AdminMatchTerminationProcessRequest(
        @NotNull MatchTerminationRequestStatus status,
        String adminMemo
) {
}
