package com.dorandoran.backend.domain.volunteer.dto;

import com.dorandoran.backend.domain.volunteer.YouthVolunteerStats;

import java.util.UUID;

public record YouthVolunteerStatsResponse(
        UUID youthId,
        int totalDurationMinutes,
        int totalHours,
        int totalCertifiedHours,
        int availableCertificateHours
) {
    public static YouthVolunteerStatsResponse from(YouthVolunteerStats stats) {
        return new YouthVolunteerStatsResponse(
                stats.getYouth().getId(),
                stats.getTotalDurationMinutes(),
                stats.totalHours(),
                stats.getTotalCertifiedHours(),
                stats.availableCertificateHours()
        );
    }

    public static YouthVolunteerStatsResponse empty(UUID youthId) {
        return new YouthVolunteerStatsResponse(youthId, 0, 0, 0, 0);
    }
}
