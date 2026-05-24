package com.dorandoran.backend.domain.schedule;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ScheduleRepository extends JpaRepository<Schedule, UUID> {

    @EntityGraph(attributePaths = {"match", "match.youth", "match.elder", "match.elder.guardian"})
    List<Schedule> findAllByMatch_Youth_IdOrderByScheduledStartAtAsc(UUID youthId);

    @EntityGraph(attributePaths = {"match", "match.youth", "match.elder", "match.elder.guardian"})
    List<Schedule> findAllByMatch_Elder_Guardian_IdOrderByScheduledStartAtAsc(UUID guardianId);

    @EntityGraph(attributePaths = {"match", "match.youth", "match.elder", "match.elder.guardian"})
    List<Schedule> findAllByOrderByScheduledStartAtAsc();

    @Query("""
            SELECT COUNT(s) > 0 FROM Schedule s
            WHERE s.match.youth.id = :youthId
              AND s.status IN (com.dorandoran.backend.domain.schedule.ScheduleStatus.PENDING,
                               com.dorandoran.backend.domain.schedule.ScheduleStatus.CONFIRMED)
              AND s.scheduledStartAt < :endAt
              AND s.scheduledEndAt > :startAt
            """)
    boolean existsConflictForYouth(@Param("youthId") UUID youthId,
                                   @Param("startAt") LocalDateTime startAt,
                                   @Param("endAt") LocalDateTime endAt);

    @Query("""
            SELECT COUNT(s) > 0 FROM Schedule s
            WHERE s.match.elder.id = :elderId
              AND s.status IN (com.dorandoran.backend.domain.schedule.ScheduleStatus.PENDING,
                               com.dorandoran.backend.domain.schedule.ScheduleStatus.CONFIRMED)
              AND s.scheduledStartAt < :endAt
              AND s.scheduledEndAt > :startAt
            """)
    boolean existsConflictForElder(@Param("elderId") UUID elderId,
                                   @Param("startAt") LocalDateTime startAt,
                                   @Param("endAt") LocalDateTime endAt);
}
