package com.dorandoran.backend.domain.admin.dto;

import com.dorandoran.backend.domain.user.User;
import com.dorandoran.backend.domain.user.UserStatus;
import com.dorandoran.backend.domain.youth.YouthActivityStatus;
import com.dorandoran.backend.domain.youth.YouthApprovalStatus;
import com.dorandoran.backend.domain.youth.YouthProfile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record AdminYouthDetailResponse(
        UUID profileId,
        UUID youthId,
        String name,
        String email,
        String phoneNumber,
        String partnerCode,
        UserStatus status,
        String profileImageUrl,
        List<String> keywords,
        String greetingComment,
        String voiceSampleUrl,
        YouthApprovalStatus approvalStatus,
        String rejectionReason,
        YouthActivityStatus activityStatus,
        boolean isCompleted,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static AdminYouthDetailResponse from(YouthProfile profile) {
        User youth = profile.getYouth();
        return new AdminYouthDetailResponse(
                profile.getId(),
                youth.getId(),
                youth.getName(),
                youth.getEmail(),
                youth.getPhoneNumber(),
                youth.getPartnerCode(),
                youth.getStatus(),
                profile.getProfileImageUrl(),
                profile.getKeywords(),
                profile.getGreetingComment(),
                profile.getVoiceSampleUrl(),
                profile.getApprovalStatus(),
                profile.getRejectionReason(),
                profile.getActivityStatus(),
                profile.isCompleted(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }
}
