package com.dorandoran.backend.domain.device.controller;

import com.dorandoran.backend.domain.device.dto.DeviceMainResponse;
import com.dorandoran.backend.domain.device.dto.TodayScheduleResponse;
import com.dorandoran.backend.domain.device.service.DeviceMainService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Device Main", description = "어르신 전용 기기 메인 화면 API")
@RestController
@RequestMapping("/api/v1/device")
@RequiredArgsConstructor
public class DeviceMainController {

    private final DeviceMainService deviceMainService;

    @Operation(summary = "전용 기기 메인 조회",
            description = "Device token 인증으로 어르신 전용 기기 메인 화면 정보를 조회한다.")
    @GetMapping("/main")
    public ResponseEntity<DeviceMainResponse> getMain(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        return ResponseEntity.ok(deviceMainService.getMain(authorization));
    }

    @Operation(summary = "오늘 일정 조회",
            description = "Device token 인증으로 해당 어르신의 오늘 확정 일정을 조회한다.")
    @GetMapping("/elders/{elderId}/today-schedule")
    public ResponseEntity<TodayScheduleResponse> getTodaySchedule(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @PathVariable("elderId") UUID elderId) {
        return ResponseEntity.ok(deviceMainService.getTodaySchedule(authorization, elderId));
    }
}
