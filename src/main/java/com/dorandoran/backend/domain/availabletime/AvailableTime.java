package com.dorandoran.backend.domain.availabletime;

import com.dorandoran.backend.domain.elder.Elder;
import com.dorandoran.backend.domain.user.User;
import com.dorandoran.backend.global.jpa.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
        name = "available_times",
        indexes = {
                @Index(name = "idx_available_times_youth", columnList = "youth_id"),
                @Index(name = "idx_available_times_elder", columnList = "elder_id"),
                @Index(name = "idx_available_times_owner_type", columnList = "owner_type")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AvailableTime extends BaseTimeEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", columnDefinition = "BINARY(16)", updatable = false, nullable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "owner_type", length = 20, nullable = false)
    private AvailableTimeOwnerType ownerType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "youth_id", columnDefinition = "BINARY(16)",
            foreignKey = @jakarta.persistence.ForeignKey(name = "fk_available_times_youth"))
    private User youth;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "elder_id", columnDefinition = "BINARY(16)",
            foreignKey = @jakarta.persistence.ForeignKey(name = "fk_available_times_elder"))
    private Elder elder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registered_by", columnDefinition = "BINARY(16)",
            foreignKey = @jakarta.persistence.ForeignKey(name = "fk_available_times_registered_by"))
    private User registeredBy;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "is_booked", nullable = false)
    private boolean isBooked;

    @Builder
    private AvailableTime(AvailableTimeOwnerType ownerType, User youth, Elder elder,
                          User registeredBy, LocalDateTime startTime, LocalDateTime endTime,
                          Boolean isBooked) {
        this.ownerType = ownerType;
        this.youth = youth;
        this.elder = elder;
        this.registeredBy = registeredBy;
        this.startTime = startTime;
        this.endTime = endTime;
        this.isBooked = isBooked != null && isBooked;
    }

    public void markBooked() {
        this.isBooked = true;
    }

    public UUID getOwnerId() {
        if (ownerType == AvailableTimeOwnerType.YOUTH) {
            return youth != null ? youth.getId() : null;
        }
        return elder != null ? elder.getId() : null;
    }
}
