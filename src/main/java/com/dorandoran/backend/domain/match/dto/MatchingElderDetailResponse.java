package com.dorandoran.backend.domain.match.dto;

import com.dorandoran.backend.domain.elder.CallType;
import com.dorandoran.backend.domain.elder.DifficultyLevel;
import com.dorandoran.backend.domain.elder.Elder;
import com.dorandoran.backend.domain.elder.ElderStatus;
import com.dorandoran.backend.domain.elder.Gender;

import java.util.List;
import java.util.UUID;

public record MatchingElderDetailResponse(
        UUID elderId,
        String name,
        String ageGroup,
        Gender gender,
        String profileImageUrl,
        String greetingComment,
        List<String> interests,
        CallType preferredCallType,
        DifficultyLevel difficultyLevel,
        String requestNotes,
        ElderStatus status
) {
    public static MatchingElderDetailResponse from(Elder elder) {
        return new MatchingElderDetailResponse(
                elder.getId(),
                elder.getName(),
                elder.getAgeGroup(),
                elder.getGender(),
                elder.getProfileImageUrl(),
                elder.getGreetingComment(),
                elder.getInterests(),
                elder.getPreferredCallType(),
                elder.getDifficultyLevel(),
                elder.getRequestNotes(),
                elder.getStatus()
        );
    }
}
