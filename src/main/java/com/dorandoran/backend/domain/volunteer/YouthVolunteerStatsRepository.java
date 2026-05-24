package com.dorandoran.backend.domain.volunteer;

import com.dorandoran.backend.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface YouthVolunteerStatsRepository extends JpaRepository<YouthVolunteerStats, UUID> {

    Optional<YouthVolunteerStats> findByYouth(User youth);

    Optional<YouthVolunteerStats> findByYouth_Id(UUID youthId);
}
