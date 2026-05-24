package com.dorandoran.backend.domain.elder.dto;

import com.dorandoran.backend.domain.elder.CallType;
import com.dorandoran.backend.domain.elder.DifficultyLevel;
import com.dorandoran.backend.domain.elder.Elder;
import com.dorandoran.backend.domain.elder.ElderStatus;
import com.dorandoran.backend.domain.elder.Gender;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ElderResponse(
        UUID elderId,
        UUID guardianId,
        String name,
        String ageGroup,
        Gender gender,
        String profileImageUrl,
        String greetingComment,
        String phoneNumber,
        String address,
        List<String> interests,
        CallType preferredCallType,
        DifficultyLevel difficultyLevel,
        String requestNotes,
        ElderStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ElderResponse from(Elder elder) {
        return new ElderResponse(
                elder.getId(),
                elder.getGuardian().getId(),
                elder.getName(),
                elder.getAgeGroup(),
                elder.getGender(),
                elder.getProfileImageUrl(),
                elder.getGreetingComment(),
                elder.getPhoneNumber(),
                elder.getAddress(),
                elder.getInterests(),
                elder.getPreferredCallType(),
                elder.getDifficultyLevel(),
                elder.getRequestNotes(),
                elder.getStatus(),
                elder.getCreatedAt(),
                elder.getUpdatedAt()
        );
    }
}
