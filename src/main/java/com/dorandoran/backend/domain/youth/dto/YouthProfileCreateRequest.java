package com.dorandoran.backend.domain.youth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record YouthProfileCreateRequest(
        String profileImageUrl,
        @Size(max = 5, message = "키워드는 최대 5개까지 등록할 수 있습니다.")
        List<@NotBlank String> keywords,
        @NotBlank
        @Size(max = 50, message = "한 줄 인사말은 최대 50자까지 입력할 수 있습니다.")
        String greetingComment,
        String voiceSampleUrl
) {
}
