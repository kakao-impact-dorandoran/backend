package com.dorandoran.backend.domain.elder;

import com.dorandoran.backend.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ElderRepository extends JpaRepository<Elder, UUID> {

    List<Elder> findAllByGuardian(User guardian);

    List<Elder> findAllByGuardianOrderByCreatedAtDesc(User guardian);

    Optional<Elder> findByIdAndGuardian(UUID id, User guardian);

    List<Elder> findAllByStatus(ElderStatus status);
}
