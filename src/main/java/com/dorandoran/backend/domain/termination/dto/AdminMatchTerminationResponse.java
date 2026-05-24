package com.dorandoran.backend.domain.termination.dto;

import com.dorandoran.backend.domain.termination.MatchTerminationRequest;
import com.dorandoran.backend.domain.termination.MatchTerminationRequestStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record AdminMatchTerminationResponse(
        UUID requestId,
        UUID matchId,
        UUID youthId,
        String youthName,
        UUID elderId,
        String elderName,
        UUID requesterUserId,
        String requesterUserName,
        String reason,
        MatchTerminationRequestStatus status,
        UUID adminId,
        String adminMemo,
        LocalDateTime createdAt,
        LocalDateTime processedAt
) {
    public static AdminMatchTerminationResponse from(MatchTerminationRequest request) {
        return new AdminMatchTerminationResponse(
                request.getId(),
                request.getMatch().getId(),
                request.getMatch().getYouth().getId(),
                request.getMatch().getYouth().getName(),
                request.getMatch().getElder().getId(),
                request.getMatch().getElder().getName(),
                request.getRequesterUser() != null ? request.getRequesterUser().getId() : null,
                request.getRequesterUser() != null ? request.getRequesterUser().getName() : null,
                request.getReason(),
                request.getStatus(),
                request.getAdmin() != null ? request.getAdmin().getId() : null,
                request.getAdminMemo(),
                request.getCreatedAt(),
                request.getProcessedAt()
        );
    }
}
