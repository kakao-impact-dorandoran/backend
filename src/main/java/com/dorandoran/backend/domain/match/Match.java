package com.dorandoran.backend.domain.match;

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
        name = "matches",
        indexes = {
                @Index(name = "idx_matches_youth", columnList = "youth_id"),
                @Index(name = "idx_matches_elder", columnList = "elder_id"),
                @Index(name = "idx_matches_status", columnList = "status")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Match extends BaseTimeEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", columnDefinition = "BINARY(16)", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "youth_id", columnDefinition = "BINARY(16)", nullable = false,
            foreignKey = @jakarta.persistence.ForeignKey(name = "fk_matches_youth"))
    private User youth;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "elder_id", columnDefinition = "BINARY(16)", nullable = false,
            foreignKey = @jakarta.persistence.ForeignKey(name = "fk_matches_elder"))
    private Elder elder;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false)
    private MatchStatus status;

    @Column(name = "selected_at", nullable = false)
    private LocalDateTime selectedAt;

    @Column(name = "matched_at", nullable = false)
    private LocalDateTime matchedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Builder
    private Match(User youth, Elder elder) {
        this.youth = youth;
        this.elder = elder;
        this.status = MatchStatus.MATCHED;
        LocalDateTime now = LocalDateTime.now();
        this.selectedAt = now;
        this.matchedAt = now;
    }

    public void start() {
        this.status = MatchStatus.IN_PROGRESS;
    }

    public void requestTermination() {
        this.status = MatchStatus.TERMINATION_REQUESTED;
    }

    public void end() {
        this.status = MatchStatus.ENDED;
        this.endedAt = LocalDateTime.now();
    }

    public boolean isActive() {
        return status == MatchStatus.MATCHED || status == MatchStatus.IN_PROGRESS;
    }
}
