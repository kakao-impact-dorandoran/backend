package com.dorandoran.backend.domain.admin.dto;

import com.dorandoran.backend.domain.youth.YouthApprovalStatus;
import com.dorandoran.backend.domain.youth.YouthProfile;

import java.util.UUID;

public record AdminYouthApprovalResponse(
        UUID youthId,
        YouthApprovalStatus approvalStatus,
        String rejectionReason
) {
    public static AdminYouthApprovalResponse from(YouthProfile profile) {
        return new AdminYouthApprovalResponse(
                profile.getYouth().getId(),
                profile.getApprovalStatus(),
                profile.getRejectionReason()
        );
    }
}
