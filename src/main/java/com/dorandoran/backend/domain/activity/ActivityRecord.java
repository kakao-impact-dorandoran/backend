package com.dorandoran.backend.domain.activity;

import com.dorandoran.backend.domain.call.CallLog;
import com.dorandoran.backend.domain.elder.Elder;
import com.dorandoran.backend.domain.match.Match;
import com.dorandoran.backend.domain.schedule.Schedule;
import com.dorandoran.backend.domain.user.User;
import com.dorandoran.backend.global.jpa.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
        name = "activity_records",
        indexes = {
                @Index(name = "idx_activity_records_youth", columnList = "youth_id"),
                @Index(name = "idx_activity_records_match", columnList = "match_id"),
                @Index(name = "idx_activity_records_schedule", columnList = "schedule_id"),
                @Index(name = "idx_activity_records_call_log", columnList = "call_log_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ActivityRecord extends BaseTimeEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", columnDefinition = "BINARY(16)", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "match_id", columnDefinition = "BINARY(16)", nullable = false,
            foreignKey = @jakarta.persistence.ForeignKey(name = "fk_activity_records_match"))
    private Match match;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "schedule_id", columnDefinition = "BINARY(16)", nullable = false,
            foreignKey = @jakarta.persistence.ForeignKey(name = "fk_activity_records_schedule"))
    private Schedule schedule;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "youth_id", columnDefinition = "BINARY(16)", nullable = false,
            foreignKey = @jakarta.persistence.ForeignKey(name = "fk_activity_records_youth"))
    private User youth;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "elder_id", columnDefinition = "BINARY(16)", nullable = false,
            foreignKey = @jakarta.persistence.ForeignKey(name = "fk_activity_records_elder"))
    private Elder elder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "call_log_id", columnDefinition = "BINARY(16)",
            foreignKey = @jakarta.persistence.ForeignKey(name = "fk_activity_records_call_log"))
    private CallLog callLog;

    @Column(name = "is_completed", nullable = false)
    private boolean isCompleted;

    @Column(name = "actual_start_at")
    private LocalDateTime actualStartAt;

    @Column(name = "actual_end_at")
    private LocalDateTime actualEndAt;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Builder
    private ActivityRecord(Match match, Schedule schedule, User youth, Elder elder, CallLog callLog,
                           boolean isCompleted, LocalDateTime actualStartAt, LocalDateTime actualEndAt,
                           Integer durationMinutes, String notes) {
        this.match = match;
        this.schedule = schedule;
        this.youth = youth;
        this.elder = elder;
        this.callLog = callLog;
        this.isCompleted = isCompleted;
        this.actualStartAt = actualStartAt;
        this.actualEndAt = actualEndAt;
        this.durationMinutes = durationMinutes;
        this.notes = notes;
    }
}
