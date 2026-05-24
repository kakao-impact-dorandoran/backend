package com.dorandoran.backend.domain.device.dto;

import com.dorandoran.backend.domain.device.DeviceStatus;

import java.util.List;
import java.util.UUID;

public record DeviceMainResponse(
        UUID elderId,
        String elderName,
        TodayScheduleResponse todaySchedule,
        List<DeviceButtonType> buttons,
        DeviceStatus deviceStatus
) {
}
