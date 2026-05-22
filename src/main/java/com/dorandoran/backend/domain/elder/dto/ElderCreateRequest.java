package com.dorandoran.backend.domain.elder.dto;

import com.dorandoran.backend.domain.elder.CallType;
import com.dorandoran.backend.domain.elder.DifficultyLevel;
import com.dorandoran.backend.domain.elder.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ElderCreateRequest(
        @NotBlank(message = "어르신 이름은 필수입니다.")
        @Size(max = 30)
        String name,
        @Size(max = 20)
        String ageGroup,
        Gender gender,
        String profileImageUrl,
        @Size(max = 50, message = "한 줄 소개는 최대 50자까지 입력할 수 있습니다.")
        String greetingComment,
        @Size(max = 20)
        String phoneNumber,
        @NotBlank(message = "전용 기기 배송 주소는 필수입니다.")
        String address,
        List<@NotBlank String> interests,
        CallType preferredCallType,
        DifficultyLevel difficultyLevel,
        String requestNotes
) {
}
