package com.dorandoran.backend.domain.device;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DeviceRepository extends JpaRepository<Device, UUID> {

    Optional<Device> findByElder_Id(UUID elderId);

    @EntityGraph(attributePaths = {"elder", "elder.guardian"})
    Optional<Device> findByDeviceToken(String deviceToken);
}
