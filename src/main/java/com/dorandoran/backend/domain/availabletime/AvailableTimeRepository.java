package com.dorandoran.backend.domain.availabletime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface AvailableTimeRepository extends JpaRepository<AvailableTime, UUID> {

    List<AvailableTime> findAllByYouth_IdOrderByStartTimeAsc(UUID youthId);

    List<AvailableTime> findAllByElder_IdOrderByStartTimeAsc(UUID elderId);

    @Query("""
            SELECT COUNT(a) > 0 FROM AvailableTime a
            WHERE a.youth.id = :youthId
              AND a.startTime < :endTime
              AND a.endTime > :startTime
            """)
    boolean existsOverlapForYouth(@Param("youthId") UUID youthId,
                                  @Param("startTime") LocalDateTime startTime,
                                  @Param("endTime") LocalDateTime endTime);

    @Query("""
            SELECT COUNT(a) > 0 FROM AvailableTime a
            WHERE a.elder.id = :elderId
              AND a.startTime < :endTime
              AND a.endTime > :startTime
            """)
    boolean existsOverlapForElder(@Param("elderId") UUID elderId,
                                  @Param("startTime") LocalDateTime startTime,
                                  @Param("endTime") LocalDateTime endTime);

    @Query("""
            SELECT COUNT(a) > 0 FROM AvailableTime a
            WHERE a.youth.id = :youthId
              AND a.startTime <= :startTime
              AND a.endTime >= :endTime
            """)
    boolean existsContainingForYouth(@Param("youthId") UUID youthId,
                                     @Param("startTime") LocalDateTime startTime,
                                     @Param("endTime") LocalDateTime endTime);

    @Query("""
            SELECT COUNT(a) > 0 FROM AvailableTime a
            WHERE a.elder.id = :elderId
              AND a.startTime <= :startTime
              AND a.endTime >= :endTime
            """)
    boolean existsContainingForElder(@Param("elderId") UUID elderId,
                                     @Param("startTime") LocalDateTime startTime,
                                     @Param("endTime") LocalDateTime endTime);

    @Query("""
            SELECT DISTINCT a.elder.id FROM AvailableTime a
            WHERE a.elder IS NOT NULL
              AND a.startTime < :availableTo
              AND a.endTime > :availableFrom
            """)
    List<UUID> findElderIdsWithOverlap(@Param("availableFrom") LocalDateTime availableFrom,
                                       @Param("availableTo") LocalDateTime availableTo);
}
