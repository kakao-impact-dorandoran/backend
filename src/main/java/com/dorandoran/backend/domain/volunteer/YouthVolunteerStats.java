package com.dorandoran.backend.domain.volunteer;

import com.dorandoran.backend.domain.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Entity
@Table(
        name = "youth_volunteer_stats",
        uniqueConstraints = @UniqueConstraint(name = "uk_youth_volunteer_stats_youth", columnNames = "youth_id")
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class YouthVolunteerStats {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", columnDefinition = "BINARY(16)", updatable = false, nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "youth_id", columnDefinition = "BINARY(16)", nullable = false,
            foreignKey = @jakarta.persistence.ForeignKey(name = "fk_youth_volunteer_stats_youth"))
    private User youth;

    @Column(name = "total_duration_minutes", nullable = false)
    private int totalDurationMinutes;

    @Column(name = "total_certified_hours", nullable = false)
    private int totalCertifiedHours;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    private YouthVolunteerStats(User youth) {
        this.youth = youth;
        this.totalDurationMinutes = 0;
        this.totalCertifiedHours = 0;
    }

    public void addDurationMinutes(int minutes) {
        if (minutes <= 0) {
            return;
        }
        this.totalDurationMinutes += minutes;
    }

    public void addCertifiedHours(int hours) {
        if (hours <= 0) {
            return;
        }
        this.totalCertifiedHours += hours;
    }

    public int totalHours() {
        return this.totalDurationMinutes / 60;
    }

    public int availableCertificateHours() {
        int total = totalHours() - totalCertifiedHours;
        return Math.max(total, 0);
    }
}
