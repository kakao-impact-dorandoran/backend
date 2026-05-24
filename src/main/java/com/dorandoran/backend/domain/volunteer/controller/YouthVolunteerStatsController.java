package com.dorandoran.backend.domain.volunteer.controller;

import com.dorandoran.backend.domain.volunteer.dto.YouthVolunteerStatsResponse;
import com.dorandoran.backend.domain.volunteer.service.YouthVolunteerStatsService;
import com.dorandoran.backend.global.security.auth.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "YouthVolunteerStats", description = "청년 누적 활동 통계 API")
@RestController
@RequestMapping("/api/v1/youth/volunteer-stats")
@RequiredArgsConstructor
public class YouthVolunteerStatsController {

    private final YouthVolunteerStatsService statsService;

    @Operation(summary = "내 누적 활동 통계 조회",
            description = "청년 본인의 누적 대화 시간과 증명서 발급 완료 시간, 발급 가능 시간을 조회한다.")
    @GetMapping("/me")
    @PreAuthorize("hasRole('YOUTH')")
    public ResponseEntity<YouthVolunteerStatsResponse> getMyStats(@AuthenticatedUser UUID youthUserId) {
        return ResponseEntity.ok(statsService.getMyStats(youthUserId));
    }
}
