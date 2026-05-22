package com.dorandoran.backend.domain.device.dto;

import com.dorandoran.backend.domain.device.DeliveryStatus;
import com.dorandoran.backend.domain.device.Device;
import com.dorandoran.backend.domain.device.DeviceStatus;
import com.dorandoran.backend.domain.device.DeviceType;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 기기 정보 응답 DTO. {@code deviceToken}은 의도적으로 노출하지 않는다.
 */
public record DeviceResponse(
        UUID deviceId,
        DeviceElderSummary elder,
        DeviceType deviceType,
        String serialNumber,
        DeliveryStatus deliveryStatus,
        String trackingNumber,
        String deliveryAddress,
        LocalDateTime deliveredAt,
        DeviceStatus deviceStatus,
        LocalDateTime registeredAt,
        LocalDateTime lastConnectedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static DeviceResponse from(Device device) {
        return new DeviceResponse(
                device.getId(),
                DeviceElderSummary.from(device.getElder()),
                device.getDeviceType(),
                device.getSerialNumber(),
                device.getDeliveryStatus(),
                device.getTrackingNumber(),
                device.getDeliveryAddress(),
                device.getDeliveredAt(),
                device.getDeviceStatus(),
                device.getRegisteredAt(),
                device.getLastConnectedAt(),
                device.getCreatedAt(),
                device.getUpdatedAt()
        );
    }
}
