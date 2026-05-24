package com.dorandoran.backend.domain.call;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CallLogRepository extends JpaRepository<CallLog, UUID> {

    @Override
    @EntityGraph(attributePaths = {"match", "match.youth", "match.elder", "match.elder.guardian", "schedule"})
    Optional<CallLog> findById(UUID id);

    @EntityGraph(attributePaths = {"match", "schedule"})
    List<CallLog> findAllByMatch_IdOrderByCreatedAtDesc(UUID matchId);

    @EntityGraph(attributePaths = {"match", "schedule"})
    List<CallLog> findAllBySchedule_IdOrderByCreatedAtDesc(UUID scheduleId);
}
