package com.dorandoran.backend.domain.match.dto;

import com.dorandoran.backend.domain.match.Match;
import com.dorandoran.backend.domain.match.MatchStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record MatchResponse(
        UUID matchId,
        UUID youthId,
        UUID elderId,
        MatchStatus status,
        String icebreakingMessage,
        LocalDateTime selectedAt,
        LocalDateTime matchedAt,
        LocalDateTime endedAt
) {
    public static MatchResponse from(Match match) {
        return new MatchResponse(
                match.getId(),
                match.getYouth().getId(),
                match.getElder().getId(),
                match.getStatus(),
                match.getIcebreakingMessage(),
                match.getSelectedAt(),
                match.getMatchedAt(),
                match.getEndedAt()
        );
    }
}
