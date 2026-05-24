package com.dorandoran.backend.domain.termination;

import com.dorandoran.backend.domain.elder.Elder;
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
        name = "match_termination_requests",
        indexes = {
                @Index(name = "idx_termination_match", columnList = "match_id"),
                @Index(name = "idx_termination_status", columnList = "status")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MatchTerminationRequest extends BaseTimeEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", columnDefinition = "BINARY(16)", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "match_id", columnDefinition = "BINARY(16)", nullable = false,
            foreignKey = @jakarta.persistence.ForeignKey(name = "fk_termination_match"))
    private Match match;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_user_id", columnDefinition = "BINARY(16)",
            foreignKey = @jakarta.persistence.ForeignKey(name = "fk_termination_requester_user"))
    private User requesterUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_elder_id", columnDefinition = "BINARY(16)",
            foreignKey = @jakarta.persistence.ForeignKey(name = "fk_termination_requester_elder"))
    private Elder requesterElder;

    @Column(name = "reason", columnDefinition = "TEXT", nullable = false)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private MatchTerminationRequestStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", columnDefinition = "BINARY(16)",
            foreignKey = @jakarta.persistence.ForeignKey(name = "fk_termination_admin"))
    private User admin;

    @Column(name = "admin_memo", columnDefinition = "TEXT")
    private String adminMemo;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Builder
    private MatchTerminationRequest(Match match, User requesterUser, Elder requesterElder, String reason) {
        this.match = match;
        this.requesterUser = requesterUser;
        this.requesterElder = requesterElder;
        this.reason = reason;
        this.status = MatchTerminationRequestStatus.REQUESTED;
    }

    public void process(MatchTerminationRequestStatus newStatus, User admin, String adminMemo) {
        this.status = newStatus;
        this.admin = admin;
        this.adminMemo = adminMemo;
        this.processedAt = LocalDateTime.now();
    }
}
