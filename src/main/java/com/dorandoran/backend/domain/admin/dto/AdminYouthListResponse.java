package com.dorandoran.backend.domain.admin.dto;

import com.dorandoran.backend.domain.user.User;
import com.dorandoran.backend.domain.user.UserStatus;
import com.dorandoran.backend.domain.youth.YouthActivityStatus;
import com.dorandoran.backend.domain.youth.YouthApprovalStatus;
import com.dorandoran.backend.domain.youth.YouthProfile;

import java.time.LocalDateTime;
import java.util.UUID;

public record AdminYouthListResponse(
        UUID profileId,
        UUID youthId,
        String name,
        String email,
        String partnerCode,
        UserStatus status,
        YouthApprovalStatus approvalStatus,
        String rejectionReason,
        YouthActivityStatus activityStatus,
        boolean isCompleted,
        LocalDateTime createdAt
) {
    public static AdminYouthListResponse from(YouthProfile profile) {
        User youth = profile.getYouth();
        return new AdminYouthListResponse(
                profile.getId(),
                youth.getId(),
                youth.getName(),
                youth.getEmail(),
                youth.getPartnerCode(),
                youth.getStatus(),
                profile.getApprovalStatus(),
                profile.getRejectionReason(),
                profile.getActivityStatus(),
                profile.isCompleted(),
                profile.getCreatedAt()
        );
    }
}
