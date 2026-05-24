package com.dorandoran.backend.domain.help;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HelpRequestRepository extends JpaRepository<HelpRequest, UUID> {

    @EntityGraph(attributePaths = {"elder", "device", "handler"})
    List<HelpRequest> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"elder", "device", "handler"})
    List<HelpRequest> findAllByHandledStatusOrderByCreatedAtDesc(HelpRequestStatus handledStatus);

    @EntityGraph(attributePaths = {"elder", "device", "handler"})
    Optional<HelpRequest> findById(UUID id);
}
