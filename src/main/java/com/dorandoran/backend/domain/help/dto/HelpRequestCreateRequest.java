package com.dorandoran.backend.domain.help.dto;

import com.dorandoran.backend.domain.help.HelpRequestType;

import java.util.Map;

public record HelpRequestCreateRequest(
        HelpRequestType requestType,
        Map<String, Object> deviceStatus
) {
}
