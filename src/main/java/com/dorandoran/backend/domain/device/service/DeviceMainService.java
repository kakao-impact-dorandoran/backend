package com.dorandoran.backend.domain.device.service;

import com.dorandoran.backend.domain.device.Device;
import com.dorandoran.backend.domain.device.dto.DeviceAuthContext;
import com.dorandoran.backend.domain.device.dto.DeviceButtonType;
import com.dorandoran.backend.domain.device.dto.DeviceMainResponse;
import com.dorandoran.backend.domain.device.dto.TodayScheduleResponse;
import com.dorandoran.backend.domain.elder.Elder;
import com.dorandoran.backend.domain.schedule.Schedule;
import com.dorandoran.backend.domain.schedule.ScheduleRepository;
import com.dorandoran.backend.global.error.BusinessException;
import com.dorandoran.backend.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeviceMainService {

    private static final ZoneId SERVICE_ZONE = ZoneId.of("Asia/Seoul");
    private static final List<DeviceButtonType> DEFAULT_BUTTONS = List.of(
            DeviceButtonType.VIDEO_CALL,
            DeviceButtonType.AUDIO_CALL,
            DeviceButtonType.HELP_REQUEST
    );

    private final DeviceAuthService deviceAuthService;
    private final ScheduleRepository scheduleRepository;

    public DeviceMainResponse getMain(String authorizationHeader) {
        DeviceAuthContext context = deviceAuthService.authenticate(authorizationHeader);
        Elder elder = context.elder();
        Device device = context.device();

        TodayScheduleResponse todaySchedule = findNextTodayConfirmedSchedule(elder.getId())
                .map(TodayScheduleResponse::from)
                .orElse(null);

        return new DeviceMainResponse(
                elder.getId(),
                elder.getName(),
                todaySchedule,
                DEFAULT_BUTTONS,
                device.getDeviceStatus()
        );
    }

    public TodayScheduleResponse getTodaySchedule(String authorizationHeader, UUID elderId) {
        DeviceAuthContext context = deviceAuthService.authenticate(authorizationHeader);
        if (!context.elder().getId().equals(elderId)) {
            throw new BusinessException(ErrorCode.DEVICE_ACCESS_DENIED);
        }
        return findNextTodayConfirmedSchedule(elderId)
                .map(TodayScheduleResponse::from)
                .orElse(null);
    }

    private java.util.Optional<Schedule> findNextTodayConfirmedSchedule(UUID elderId) {
        LocalDate today = LocalDate.now(SERVICE_ZONE);
        LocalDateTime dayStart = today.atStartOfDay();
        LocalDateTime dayEnd = today.plusDays(1).atStartOfDay();
        LocalDateTime now = LocalDateTime.now(SERVICE_ZONE);

        List<Schedule> todays = scheduleRepository.findConfirmedTodayByElderId(elderId, dayStart, dayEnd);
        return todays.stream()
                .filter(s -> !s.getScheduledEndAt().isBefore(now))
                .findFirst()
                .or(() -> todays.stream().findFirst());
    }
}
