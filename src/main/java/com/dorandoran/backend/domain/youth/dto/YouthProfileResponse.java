package com.dorandoran.backend.domain.youth.dto;

import com.dorandoran.backend.domain.youth.YouthActivityStatus;
import com.dorandoran.backend.domain.youth.YouthApprovalStatus;
import com.dorandoran.backend.domain.youth.YouthProfile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record YouthProfileResponse(
        UUID profileId,
        UUID youthId,
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
    public static YouthProfileResponse from(YouthProfile profile) {
        return new YouthProfileResponse(
                profile.getId(),
                profile.getYouth().getId(),
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
