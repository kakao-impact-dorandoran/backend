package com.dorandoran.backend.domain.youth;

import com.dorandoran.backend.domain.user.User;
import com.dorandoran.backend.global.error.BusinessException;
import com.dorandoran.backend.global.error.ErrorCode;
import com.dorandoran.backend.global.jpa.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Getter
@Entity
@Table(
        name = "youth_match_limits",
        uniqueConstraints = @UniqueConstraint(name = "uk_youth_match_limits_youth_id", columnNames = "youth_id")
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class YouthMatchLimit extends BaseTimeEntity {

    private static final int DEFAULT_MAX_MATCH_COUNT = 3;

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", columnDefinition = "BINARY(16)", updatable = false, nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "youth_id", columnDefinition = "BINARY(16)", nullable = false,
            foreignKey = @jakarta.persistence.ForeignKey(name = "fk_youth_match_limits_user"))
    private User youth;

    @Column(name = "max_match_count", nullable = false)
    private int maxMatchCount;

    @Column(name = "current_match_count", nullable = false)
    private int currentMatchCount;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Builder
    private YouthMatchLimit(User youth, Integer maxMatchCount) {
        this.youth = youth;
        this.maxMatchCount = maxMatchCount == null ? DEFAULT_MAX_MATCH_COUNT : maxMatchCount;
        this.currentMatchCount = 0;
    }

    public void increment() {
        if (currentMatchCount >= maxMatchCount) {
            throw new BusinessException(ErrorCode.MATCH_LIMIT_EXCEEDED);
        }
        this.currentMatchCount++;
    }

    public void decrement() {
        if (currentMatchCount > 0) {
            this.currentMatchCount--;
        }
    }
}
