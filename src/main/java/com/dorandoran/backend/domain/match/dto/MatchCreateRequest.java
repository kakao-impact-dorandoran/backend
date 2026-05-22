package com.dorandoran.backend.domain.match.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record MatchCreateRequest(
        @NotNull(message = "elderId는 필수입니다.")
        UUID elderId,
        @NotBlank(message = "사전 인사말은 필수입니다.")
        String icebreakingMessage
) {
}
