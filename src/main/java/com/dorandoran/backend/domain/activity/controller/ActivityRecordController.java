package com.dorandoran.backend.domain.activity.controller;

import com.dorandoran.backend.domain.activity.dto.ActivityRecordCreateRequest;
import com.dorandoran.backend.domain.activity.dto.ActivityRecordResponse;
import com.dorandoran.backend.domain.activity.dto.ActivityRecordSummaryResponse;
import com.dorandoran.backend.domain.activity.service.ActivityRecordService;
import com.dorandoran.backend.global.security.auth.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = "ActivityRecord", description = "청년 활동 기록 API")
@RestController
@RequestMapping("/api/v1/activity-records")
@RequiredArgsConstructor
public class ActivityRecordController {

    private final ActivityRecordService activityRecordService;

    @Operation(summary = "활동 기록 작성",
            description = "청년이 대화 종료 후 활동 기록을 작성한다. 제출 시 누적 활동 시간이 즉시 증가한다.")
    @PostMapping
    @PreAuthorize("hasRole('YOUTH')")
    public ResponseEntity<ActivityRecordResponse> createActivityRecord(
            @AuthenticatedUser UUID youthUserId,
            @Valid @RequestBody ActivityRecordCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(activityRecordService.createActivityRecord(youthUserId, request));
    }

    @Operation(summary = "활동 기록 목록 조회",
            description = "청년은 본인 기록, 보호자는 본인이 등록한 어르신과 연관된 기록, 관리자는 전체 기록을 조회한다.")
    @GetMapping
    @PreAuthorize("hasAnyRole('YOUTH','GUARDIAN','ADMIN')")
    public ResponseEntity<List<ActivityRecordSummaryResponse>> findActivityRecords(
            @AuthenticatedUser UUID userId) {
        return ResponseEntity.ok(activityRecordService.findActivityRecords(userId));
    }
}
