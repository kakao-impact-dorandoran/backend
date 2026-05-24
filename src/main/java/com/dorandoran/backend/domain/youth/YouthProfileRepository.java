package com.dorandoran.backend.domain.youth;

import com.dorandoran.backend.domain.user.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface YouthProfileRepository extends JpaRepository<YouthProfile, UUID> {

    Optional<YouthProfile> findByYouth(User youth);

    boolean existsByYouth(User youth);

    @EntityGraph(attributePaths = "youth")
    List<YouthProfile> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = "youth")
    List<YouthProfile> findAllByApprovalStatusOrderByCreatedAtDesc(YouthApprovalStatus approvalStatus);
}
