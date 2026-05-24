package com.dorandoran.backend.domain.schedule;

import com.dorandoran.backend.domain.match.Match;
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
        name = "schedules",
        indexes = {
                @Index(name = "idx_schedules_match", columnList = "match_id"),
                @Index(name = "idx_schedules_status", columnList = "status"),
                @Index(name = "idx_schedules_start", columnList = "scheduled_start_at")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Schedule extends BaseTimeEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", columnDefinition = "BINARY(16)", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "match_id", columnDefinition = "BINARY(16)", nullable = false,
            foreignKey = @jakarta.persistence.ForeignKey(name = "fk_schedules_match"))
    private Match match;

    @Column(name = "scheduled_start_at", nullable = false)
    private LocalDateTime scheduledStartAt;

    @Column(name = "scheduled_end_at", nullable = false)
    private LocalDateTime scheduledEndAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private ScheduleStatus status;

    @Column(name = "remind_sent", nullable = false)
    private boolean remindSent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", columnDefinition = "BINARY(16)",
            foreignKey = @jakarta.persistence.ForeignKey(name = "fk_schedules_created_by"))
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "canceled_by", columnDefinition = "BINARY(16)",
            foreignKey = @jakarta.persistence.ForeignKey(name = "fk_schedules_canceled_by"))
    private User canceledBy;

    @Column(name = "cancel_reason", columnDefinition = "TEXT")
    private String cancelReason;

    @Builder
    private Schedule(Match match, LocalDateTime scheduledStartAt, LocalDateTime scheduledEndAt,
                     ScheduleStatus status, User createdBy) {
        this.match = match;
        this.scheduledStartAt = scheduledStartAt;
        this.scheduledEndAt = scheduledEndAt;
        this.status = status == null ? ScheduleStatus.CONFIRMED : status;
        this.remindSent = false;
        this.createdBy = createdBy;
    }

    public void cancel(User canceledBy, String cancelReason) {
        this.status = ScheduleStatus.CANCELED;
        this.canceledBy = canceledBy;
        this.cancelReason = cancelReason;
    }

    public void complete() {
        this.status = ScheduleStatus.COMPLETED;
    }

    public boolean isCanceled() {
        return status == ScheduleStatus.CANCELED;
    }

    public boolean isCompleted() {
        return status == ScheduleStatus.COMPLETED;
    }
}
