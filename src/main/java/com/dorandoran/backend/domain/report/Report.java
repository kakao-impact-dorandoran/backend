package com.dorandoran.backend.domain.report;

import com.dorandoran.backend.domain.elder.Elder;
import com.dorandoran.backend.domain.match.Match;
import com.dorandoran.backend.domain.schedule.Schedule;
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
        name = "reports",
        indexes = {
                @Index(name = "idx_reports_status", columnList = "status"),
                @Index(name = "idx_reports_reporter_user", columnList = "reporter_user_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Report extends BaseTimeEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", columnDefinition = "BINARY(16)", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_user_id", columnDefinition = "BINARY(16)",
            foreignKey = @jakarta.persistence.ForeignKey(name = "fk_reports_reporter_user"))
    private User reporterUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_elder_id", columnDefinition = "BINARY(16)",
            foreignKey = @jakarta.persistence.ForeignKey(name = "fk_reports_reporter_elder"))
    private Elder reporterElder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_user_id", columnDefinition = "BINARY(16)",
            foreignKey = @jakarta.persistence.ForeignKey(name = "fk_reports_target_user"))
    private User targetUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_elder_id", columnDefinition = "BINARY(16)",
            foreignKey = @jakarta.persistence.ForeignKey(name = "fk_reports_target_elder"))
    private Elder targetElder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", columnDefinition = "BINARY(16)",
            foreignKey = @jakarta.persistence.ForeignKey(name = "fk_reports_match"))
    private Match match;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", columnDefinition = "BINARY(16)",
            foreignKey = @jakarta.persistence.ForeignKey(name = "fk_reports_schedule"))
    private Schedule schedule;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", length = 30, nullable = false)
    private ReportType reportType;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private ReportStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", columnDefinition = "BINARY(16)",
            foreignKey = @jakarta.persistence.ForeignKey(name = "fk_reports_admin"))
    private User admin;

    @Column(name = "admin_memo", columnDefinition = "TEXT")
    private String adminMemo;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Builder
    private Report(User reporterUser, Elder reporterElder, User targetUser, Elder targetElder,
                   Match match, Schedule schedule, ReportType reportType, String content) {
        this.reporterUser = reporterUser;
        this.reporterElder = reporterElder;
        this.targetUser = targetUser;
        this.targetElder = targetElder;
        this.match = match;
        this.schedule = schedule;
        this.reportType = reportType;
        this.content = content;
        this.status = ReportStatus.PENDING;
    }

    public void process(ReportStatus newStatus, User admin, String adminMemo) {
        this.status = newStatus;
        this.admin = admin;
        this.adminMemo = adminMemo;
        if (newStatus == ReportStatus.RESOLVED || newStatus == ReportStatus.REJECTED) {
            this.resolvedAt = LocalDateTime.now();
        }
    }
}
