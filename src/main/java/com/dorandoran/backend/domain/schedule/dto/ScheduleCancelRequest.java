package com.dorandoran.backend.domain.schedule.dto;

import jakarta.validation.constraints.Size;

public record ScheduleCancelRequest(
        @Size(max = 500, message = "취소 사유는 최대 500자까지 입력할 수 있습니다.")
        String cancelReason
) {
}
