package com.dorandoran.backend.domain.youth.dto;

import com.dorandoran.backend.domain.youth.YouthApprovalStatus;
import com.dorandoran.backend.domain.youth.YouthProfile;

import java.util.UUID;

public record YouthProfileCreateResponse(
        UUID profileId,
        boolean isCompleted,
        YouthApprovalStatus approvalStatus,
        String rejectionReason
) {
    public static YouthProfileCreateResponse from(YouthProfile profile) {
        return new YouthProfileCreateResponse(
                profile.getId(),
                profile.isCompleted(),
                profile.getApprovalStatus(),
                profile.getRejectionReason()
        );
    }
}
