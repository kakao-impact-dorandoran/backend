package com.dorandoran.backend.domain.help.dto;

import com.dorandoran.backend.domain.help.HelpRequestStatus;
import jakarta.validation.constraints.NotNull;

public record AdminHelpRequestProcessRequest(
        @NotNull HelpRequestStatus status
) {
}
