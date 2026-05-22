package com.dorandoran.backend.domain.user.dto;

import com.dorandoran.backend.domain.user.Role;
import com.dorandoran.backend.domain.user.User;
import com.dorandoran.backend.domain.user.UserStatus;
import com.dorandoran.backend.domain.youth.YouthActivityStatus;
import com.dorandoran.backend.domain.youth.YouthApprovalStatus;
import com.dorandoran.backend.domain.youth.YouthProfile;

import java.util.UUID;

public record AuthUserResponse(
        UUID id,
        String email,
        String name,
        Role role,
        String profileUrl,
        UserStatus status,
        String partnerCode,
        YouthApprovalStatus approvalStatus,
        String rejectionReason,
        YouthActivityStatus activityStatus
) {
    public static AuthUserResponse from(User user) {
        return from(user, null);
    }

    public static AuthUserResponse from(User user, YouthProfile youthProfile) {
        YouthApprovalStatus approvalStatus = youthProfile != null ? youthProfile.getApprovalStatus() : null;
        String rejectionReason = youthProfile != null ? youthProfile.getRejectionReason() : null;
        YouthActivityStatus activityStatus = youthProfile != null ? youthProfile.getActivityStatus() : null;
        return new AuthUserResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole(),
                user.getProfileUrl(),
                user.getStatus(),
                user.getPartnerCode(),
                approvalStatus,
                rejectionReason,
                activityStatus
        );
    }
}
