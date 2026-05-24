package com.dorandoran.backend.domain.help.dto;

import com.dorandoran.backend.domain.help.HelpRequest;
import com.dorandoran.backend.domain.help.HelpRequestStatus;
import com.dorandoran.backend.domain.help.HelpRequestType;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public record AdminHelpRequestResponse(
        UUID helpRequestId,
        UUID elderId,
        String elderName,
        UUID deviceId,
        HelpRequestType requestType,
        Map<String, Object> deviceStatus,
        HelpRequestStatus handledStatus,
        UUID handlerId,
        String handlerName,
        LocalDateTime handledAt,
        LocalDateTime createdAt
) {
    public static AdminHelpRequestResponse from(HelpRequest helpRequest) {
        return new AdminHelpRequestResponse(
                helpRequest.getId(),
                helpRequest.getElder().getId(),
                helpRequest.getElder().getName(),
                helpRequest.getDevice() != null ? helpRequest.getDevice().getId() : null,
                helpRequest.getRequestType(),
                helpRequest.getDeviceStatus(),
                helpRequest.getHandledStatus(),
                helpRequest.getHandler() != null ? helpRequest.getHandler().getId() : null,
                helpRequest.getHandler() != null ? helpRequest.getHandler().getName() : null,
                helpRequest.getHandledAt(),
                helpRequest.getCreatedAt()
        );
    }
}
