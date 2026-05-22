package com.dorandoran.backend.domain.device.controller;

import com.dorandoran.backend.domain.device.dto.DeviceResponse;
import com.dorandoran.backend.domain.device.service.DeviceService;
import com.dorandoran.backend.global.security.auth.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Device", description = "전용 기기 조회 API")
@RestController
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('GUARDIAN','ADMIN')")
public class DeviceController {

    private final DeviceService deviceService;

    @Operation(summary = "기기 단건 조회", description = "관리자 또는 해당 어르신의 보호자만 조회 가능. deviceToken은 응답에 포함되지 않는다.")
    @GetMapping("/api/v1/devices/{deviceId}")
    public ResponseEntity<DeviceResponse> getDevice(
            @AuthenticatedUser UUID userId,
            @PathVariable("deviceId") UUID deviceId) {
        return ResponseEntity.ok(deviceService.getDevice(userId, deviceId));
    }

    @Operation(summary = "어르신 기준 기기 조회",
            description = "특정 어르신에 연결된 기기를 조회한다. 관리자 또는 해당 어르신의 보호자만 가능.")
    @GetMapping("/api/v1/elders/{elderId}/device")
    public ResponseEntity<DeviceResponse> getDeviceByElder(
            @AuthenticatedUser UUID userId,
            @PathVariable("elderId") UUID elderId) {
        return ResponseEntity.ok(deviceService.getDeviceByElder(userId, elderId));
    }
}
