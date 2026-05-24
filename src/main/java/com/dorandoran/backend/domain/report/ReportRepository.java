package com.dorandoran.backend.domain.report;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReportRepository extends JpaRepository<Report, UUID> {

    @EntityGraph(attributePaths = {"reporterUser", "targetUser", "targetElder", "match", "schedule", "admin"})
    List<Report> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"reporterUser", "targetUser", "targetElder", "match", "schedule", "admin"})
    List<Report> findAllByStatusOrderByCreatedAtDesc(ReportStatus status);

    @EntityGraph(attributePaths = {"reporterUser", "targetUser", "targetElder", "match", "schedule", "admin"})
    Optional<Report> findById(UUID id);
}
