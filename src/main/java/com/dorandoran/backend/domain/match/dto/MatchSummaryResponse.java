package com.dorandoran.backend.domain.match.dto;

import com.dorandoran.backend.domain.match.Match;
import com.dorandoran.backend.domain.match.MatchStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record MatchSummaryResponse(
        UUID matchId,
        UUID youthId,
        String youthName,
        UUID elderId,
        String elderName,
        MatchStatus status,
        String icebreakingMessage,
        LocalDateTime selectedAt,
        LocalDateTime matchedAt,
        LocalDateTime endedAt
) {
    public static MatchSummaryResponse from(Match match) {
        return new MatchSummaryResponse(
                match.getId(),
                match.getYouth().getId(),
                match.getYouth().getName(),
                match.getElder().getId(),
                match.getElder().getName(),
                match.getStatus(),
                match.getIcebreakingMessage(),
                match.getSelectedAt(),
                match.getMatchedAt(),
                match.getEndedAt()
        );
    }
}
