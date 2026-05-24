package com.dorandoran.backend.domain.match.dto;

import com.dorandoran.backend.domain.youth.YouthMatchLimit;

import java.util.UUID;

public record YouthMatchLimitResponse(
        UUID youthId,
        int currentMatchCount,
        int maxMatchCount,
        int remainingMatchCount,
        boolean canMatch
) {
    public static YouthMatchLimitResponse from(YouthMatchLimit limit) {
        int max = limit.getMaxMatchCount();
        int current = limit.getCurrentMatchCount();
        int remaining = Math.max(0, max - current);
        return new YouthMatchLimitResponse(
                limit.getYouth().getId(),
                current,
                max,
                remaining,
                current < max
        );
    }
}
