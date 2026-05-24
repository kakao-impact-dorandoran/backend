package com.dorandoran.backend.domain.termination.dto;

import jakarta.validation.constraints.NotBlank;

public record MatchTerminationCreateRequest(
        @NotBlank String reason
) {
}
