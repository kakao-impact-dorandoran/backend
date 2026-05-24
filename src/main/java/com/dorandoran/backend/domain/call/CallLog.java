package com.dorandoran.backend.domain.call;

import com.dorandoran.backend.domain.elder.CallType;
import com.dorandoran.backend.domain.match.Match;
import com.dorandoran.backend.domain.schedule.Schedule;
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
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Entity
@Table(
        name = "call_logs",
        indexes = {
                @Index(name = "idx_call_logs_match", columnList = "match_id"),
                @Index(name = "idx_call_logs_schedule", columnList = "schedule_id"),
                @Index(name = "idx_call_logs_status", columnList = "status")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CallLog {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", columnDefinition = "BINARY(16)", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "match_id", columnDefinition = "BINARY(16)", nullable = false,
            foreignKey = @jakarta.persistence.ForeignKey(name = "fk_call_logs_match"))
    private Match match;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", columnDefinition = "BINARY(16)",
            foreignKey = @jakarta.persistence.ForeignKey(name = "fk_call_logs_schedule"))
    private Schedule schedule;

    @Enumerated(EnumType.STRING)
    @Column(name = "call_type", length = 20, nullable = false)
    private CallType callType;

    @Column(name = "start_at")
    private LocalDateTime startAt;

    @Column(name = "end_at")
    private LocalDateTime endAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private CallLogStatus status;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    private CallLog(Match match, Schedule schedule, CallType callType,
                    LocalDateTime startAt, CallLogStatus status) {
        this.match = match;
        this.schedule = schedule;
        this.callType = callType;
        this.startAt = startAt == null ? LocalDateTime.now() : startAt;
        this.status = status == null ? CallLogStatus.PENDING : status;
    }

    public void complete() {
        this.status = CallLogStatus.COMPLETED;
        this.endAt = LocalDateTime.now();
    }

    public boolean isEnded() {
        return status == CallLogStatus.COMPLETED
                || status == CallLogStatus.MISSED
                || status == CallLogStatus.FAILED;
    }
}
