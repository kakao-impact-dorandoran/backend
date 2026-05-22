package com.dorandoran.backend.domain.device;

import com.dorandoran.backend.domain.elder.Elder;
import com.dorandoran.backend.global.jpa.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Entity
@Table(
        name = "devices",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_devices_elder_id", columnNames = "elder_id"),
                @UniqueConstraint(name = "uk_devices_serial_number", columnNames = "serial_number")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Device extends BaseTimeEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", columnDefinition = "BINARY(16)", updatable = false, nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "elder_id", columnDefinition = "BINARY(16)", nullable = false,
            foreignKey = @jakarta.persistence.ForeignKey(name = "fk_devices_elder"))
    private Elder elder;

    @Enumerated(EnumType.STRING)
    @Column(name = "device_type", length = 30, nullable = false)
    private DeviceType deviceType;

    @Column(name = "serial_number", length = 100)
    private String serialNumber;

    @Column(name = "device_token", columnDefinition = "TEXT")
    private String deviceToken;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_status", length = 20, nullable = false)
    private DeliveryStatus deliveryStatus;

    @Column(name = "tracking_number", length = 100)
    private String trackingNumber;

    @Column(name = "delivery_address", columnDefinition = "TEXT", nullable = false)
    private String deliveryAddress;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "device_status", length = 20, nullable = false)
    private DeviceStatus deviceStatus;

    @Column(name = "registered_at")
    private LocalDateTime registeredAt;

    @Column(name = "last_connected_at")
    private LocalDateTime lastConnectedAt;

    @Builder
    private Device(Elder elder, DeviceType deviceType, String serialNumber, String deviceToken,
                   DeliveryStatus deliveryStatus, String trackingNumber, String deliveryAddress,
                   LocalDateTime deliveredAt, DeviceStatus deviceStatus,
                   LocalDateTime registeredAt, LocalDateTime lastConnectedAt) {
        this.elder = elder;
        this.deviceType = deviceType == null ? DeviceType.TABLET : deviceType;
        this.serialNumber = serialNumber;
        this.deviceToken = deviceToken;
        this.deliveryStatus = deliveryStatus == null ? DeliveryStatus.READY : deliveryStatus;
        this.trackingNumber = trackingNumber;
        this.deliveryAddress = deliveryAddress;
        this.deliveredAt = deliveredAt;
        this.deviceStatus = deviceStatus == null ? DeviceStatus.UNREGISTERED : deviceStatus;
        this.registeredAt = registeredAt;
        this.lastConnectedAt = lastConnectedAt;
    }
}
