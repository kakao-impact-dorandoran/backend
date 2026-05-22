package com.dorandoran.backend.domain.device.dto;

import com.dorandoran.backend.domain.elder.Elder;

import java.util.UUID;

public record DeviceElderSummary(
        UUID elderId,
        UUID guardianId,
        String name
) {
    public static DeviceElderSummary from(Elder elder) {
        return new DeviceElderSummary(
                elder.getId(),
                elder.getGuardian().getId(),
                elder.getName()
        );
    }
}
