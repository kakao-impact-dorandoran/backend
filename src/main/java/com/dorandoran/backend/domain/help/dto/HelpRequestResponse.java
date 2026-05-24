package com.dorandoran.backend.domain.help.dto;

import com.dorandoran.backend.domain.help.HelpRequest;
import com.dorandoran.backend.domain.help.HelpRequestStatus;
import com.dorandoran.backend.domain.help.HelpRequestType;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public record HelpRequestResponse(
        UUID helpRequestId,
        UUID elderId,
        UUID deviceId,
        HelpRequestType requestType,
        Map<String, Object> deviceStatus,
        HelpRequestStatus handledStatus,
        LocalDateTime createdAt
) {
    public static HelpRequestResponse from(HelpRequest helpRequest) {
        return new HelpRequestResponse(
                helpRequest.getId(),
                helpRequest.getElder().getId(),
                helpRequest.getDevice() != null ? helpRequest.getDevice().getId() : null,
                helpRequest.getRequestType(),
                helpRequest.getDeviceStatus(),
                helpRequest.getHandledStatus(),
                helpRequest.getCreatedAt()
        );
    }
}
