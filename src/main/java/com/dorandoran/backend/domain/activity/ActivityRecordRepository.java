package com.dorandoran.backend.domain.activity;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ActivityRecordRepository extends JpaRepository<ActivityRecord, UUID> {

    @EntityGraph(attributePaths = {"match", "youth", "elder", "schedule", "callLog"})
    List<ActivityRecord> findAllByYouth_IdOrderByCreatedAtDesc(UUID youthId);

    @EntityGraph(attributePaths = {"match", "youth", "elder", "schedule", "callLog"})
    List<ActivityRecord> findAllByElder_Guardian_IdOrderByCreatedAtDesc(UUID guardianId);

    @EntityGraph(attributePaths = {"match", "youth", "elder", "schedule", "callLog"})
    List<ActivityRecord> findAllByOrderByCreatedAtDesc();

    boolean existsBySchedule_Id(UUID scheduleId);

    boolean existsByCallLog_Id(UUID callLogId);
}
