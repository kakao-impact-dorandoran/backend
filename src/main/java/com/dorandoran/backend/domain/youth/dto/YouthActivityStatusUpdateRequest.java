package com.dorandoran.backend.domain.youth.dto;

import com.dorandoran.backend.domain.youth.YouthActivityStatus;
import jakarta.validation.constraints.NotNull;

public record YouthActivityStatusUpdateRequest(
        @NotNull(message = "activityStatus는 필수입니다.")
        YouthActivityStatus activityStatus
) {
}
