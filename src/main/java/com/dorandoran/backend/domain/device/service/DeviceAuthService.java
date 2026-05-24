package com.dorandoran.backend.domain.device.service;

import com.dorandoran.backend.domain.device.Device;
import com.dorandoran.backend.domain.device.DeviceRepository;
import com.dorandoran.backend.domain.device.DeviceStatus;
import com.dorandoran.backend.domain.device.dto.DeviceAuthContext;
import com.dorandoran.backend.domain.elder.Elder;
import com.dorandoran.backend.global.error.BusinessException;
import com.dorandoran.backend.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeviceAuthService {

    private static final String DEVICE_SCHEME = "Device ";

    private final DeviceRepository deviceRepository;

    public DeviceAuthContext authenticate(String authorizationHeader) {
        if (!StringUtils.hasText(authorizationHeader)) {
            throw new BusinessException(ErrorCode.DEVICE_AUTH_REQUIRED);
        }
        if (!authorizationHeader.startsWith(DEVICE_SCHEME)) {
            throw new BusinessException(ErrorCode.INVALID_DEVICE_AUTHORIZATION);
        }
        String token = authorizationHeader.substring(DEVICE_SCHEME.length()).trim();
        if (!StringUtils.hasText(token)) {
            throw new BusinessException(ErrorCode.INVALID_DEVICE_AUTHORIZATION);
        }

        Device device = deviceRepository.findByDeviceToken(token)
                .orElseThrow(() -> new BusinessException(ErrorCode.DEVICE_NOT_FOUND));

        if (device.getDeviceStatus() != DeviceStatus.REGISTERED) {
            throw new BusinessException(ErrorCode.DEVICE_NOT_REGISTERED);
        }

        Elder elder = device.getElder();
        if (elder == null) {
            throw new BusinessException(ErrorCode.DEVICE_ACCESS_DENIED);
        }

        return new DeviceAuthContext(device, elder);
    }
}
