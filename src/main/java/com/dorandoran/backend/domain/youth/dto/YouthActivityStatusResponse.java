package com.dorandoran.backend.domain.youth.dto;

import com.dorandoran.backend.domain.youth.YouthActivityStatus;
import com.dorandoran.backend.domain.youth.YouthProfile;

import java.util.UUID;

public record YouthActivityStatusResponse(
        UUID youthId,
        YouthActivityStatus activityStatus
) {
    public static YouthActivityStatusResponse from(YouthProfile profile) {
        return new YouthActivityStatusResponse(
                profile.getYouth().getId(),
                profile.getActivityStatus()
        );
    }
}
