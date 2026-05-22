package com.dorandoran.backend.domain.match.dto;

import com.dorandoran.backend.domain.elder.CallType;
import com.dorandoran.backend.domain.elder.DifficultyLevel;
import com.dorandoran.backend.domain.elder.Elder;
import com.dorandoran.backend.domain.match.Match;
import com.dorandoran.backend.domain.match.MatchStatus;
import com.dorandoran.backend.domain.user.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record MatchDetailResponse(
        UUID matchId,
        MatchStatus status,
        String icebreakingMessage,
        LocalDateTime selectedAt,
        LocalDateTime matchedAt,
        LocalDateTime endedAt,
        YouthSummary youth,
        ElderSummary elder
) {
    public record YouthSummary(UUID youthId, String name) {
        public static YouthSummary from(User user) {
            return new YouthSummary(user.getId(), user.getName());
        }
    }

    public record ElderSummary(
            UUID elderId,
            String name,
            String ageGroup,
            List<String> interests,
            CallType preferredCallType,
            DifficultyLevel difficultyLevel
    ) {
        public static ElderSummary from(Elder elder) {
            return new ElderSummary(
                    elder.getId(),
                    elder.getName(),
                    elder.getAgeGroup(),
                    elder.getInterests(),
                    elder.getPreferredCallType(),
                    elder.getDifficultyLevel()
            );
        }
    }

    public static MatchDetailResponse from(Match match) {
        return new MatchDetailResponse(
                match.getId(),
                match.getStatus(),
                match.getIcebreakingMessage(),
                match.getSelectedAt(),
                match.getMatchedAt(),
                match.getEndedAt(),
                YouthSummary.from(match.getYouth()),
                ElderSummary.from(match.getElder())
        );
    }
}
