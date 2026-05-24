package com.dorandoran.backend.domain.termination.dto;

import com.dorandoran.backend.domain.termination.MatchTerminationRequest;
import com.dorandoran.backend.domain.termination.MatchTerminationRequestStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record MatchTerminationResponse(
        UUID requestId,
        UUID matchId,
        UUID requesterUserId,
        String reason,
        MatchTerminationRequestStatus status,
        LocalDateTime createdAt
) {
    public static MatchTerminationResponse from(MatchTerminationRequest request) {
        return new MatchTerminationResponse(
                request.getId(),
                request.getMatch().getId(),
                request.getRequesterUser() != null ? request.getRequesterUser().getId() : null,
                request.getReason(),
                request.getStatus(),
                request.getCreatedAt()
        );
    }
}
