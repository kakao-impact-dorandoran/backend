package com.dorandoran.backend.domain.device.dto;

import com.dorandoran.backend.domain.device.Device;
import com.dorandoran.backend.domain.elder.Elder;

public record DeviceAuthContext(Device device, Elder elder) {
}
