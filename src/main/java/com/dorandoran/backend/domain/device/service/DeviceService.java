package com.dorandoran.backend.domain.device.service;

import com.dorandoran.backend.domain.device.Device;
import com.dorandoran.backend.domain.device.DeviceRepository;
import com.dorandoran.backend.domain.device.dto.DeviceResponse;
import com.dorandoran.backend.domain.elder.Elder;
import com.dorandoran.backend.domain.elder.ElderRepository;
import com.dorandoran.backend.domain.user.Role;
import com.dorandoran.backend.domain.user.User;
import com.dorandoran.backend.domain.user.UserRepository;
import com.dorandoran.backend.global.error.BusinessException;
import com.dorandoran.backend.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeviceService {

    private final UserRepository userRepository;
    private final ElderRepository elderRepository;
    private final DeviceRepository deviceRepository;

    public DeviceResponse getDevice(UUID actorUserId, UUID deviceId) {
        User actor = loadActor(actorUserId);
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DEVICE_NOT_FOUND));
        verifyAccess(actor, device.getElder());
        return DeviceResponse.from(device);
    }

    public DeviceResponse getDeviceByElder(UUID actorUserId, UUID elderId) {
        User actor = loadActor(actorUserId);
        Elder elder = elderRepository.findById(elderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ELDER_NOT_FOUND));
        verifyAccess(actor, elder);
        Device device = deviceRepository.findByElder_Id(elderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DEVICE_NOT_FOUND_FOR_ELDER));
        return DeviceResponse.from(device);
    }

    private User loadActor(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private void verifyAccess(User actor, Elder elder) {
        Role role = actor.getRole();
        if (role == Role.ADMIN) {
            return;
        }
        if (role == Role.GUARDIAN && elder.getGuardian().getId().equals(actor.getId())) {
            return;
        }
        throw new BusinessException(ErrorCode.DEVICE_ACCESS_DENIED);
    }
}
