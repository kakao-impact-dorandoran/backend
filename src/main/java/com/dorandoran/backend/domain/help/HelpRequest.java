package com.dorandoran.backend.domain.help;

import com.dorandoran.backend.domain.device.Device;
import com.dorandoran.backend.domain.elder.Elder;
import com.dorandoran.backend.domain.user.User;
import com.dorandoran.backend.global.converter.MapJsonConverter;
import com.dorandoran.backend.global.jpa.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Getter
@Entity
@Table(name = "help_requests")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HelpRequest extends BaseTimeEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", columnDefinition = "BINARY(16)", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "elder_id", columnDefinition = "BINARY(16)", nullable = false,
            foreignKey = @jakarta.persistence.ForeignKey(name = "fk_help_requests_elder"))
    private Elder elder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", columnDefinition = "BINARY(16)",
            foreignKey = @jakarta.persistence.ForeignKey(name = "fk_help_requests_device"))
    private Device device;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_type", length = 30, nullable = false)
    private HelpRequestType requestType;

    @Convert(converter = MapJsonConverter.class)
    @Column(name = "device_status", columnDefinition = "JSON")
    private Map<String, Object> deviceStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "handled_status", length = 20, nullable = false)
    private HelpRequestStatus handledStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "handler_id", columnDefinition = "BINARY(16)",
            foreignKey = @jakarta.persistence.ForeignKey(name = "fk_help_requests_handler"))
    private User handler;

    @Column(name = "handled_at")
    private LocalDateTime handledAt;

    @Builder
    private HelpRequest(Elder elder, Device device, HelpRequestType requestType,
                        Map<String, Object> deviceStatus) {
        this.elder = elder;
        this.device = device;
        this.requestType = requestType == null ? HelpRequestType.DEVICE_HELP : requestType;
        this.deviceStatus = deviceStatus;
        this.handledStatus = HelpRequestStatus.PENDING;
    }

    public void handle(User handler) {
        this.handledStatus = HelpRequestStatus.HANDLED;
        this.handler = handler;
        this.handledAt = LocalDateTime.now();
    }
}
