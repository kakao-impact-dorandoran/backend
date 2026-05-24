package com.dorandoran.backend.domain.termination;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MatchTerminationRequestRepository extends JpaRepository<MatchTerminationRequest, UUID> {

    @EntityGraph(attributePaths = {"match", "match.youth", "match.elder", "requesterUser", "admin"})
    List<MatchTerminationRequest> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"match", "match.youth", "match.elder", "requesterUser", "admin"})
    List<MatchTerminationRequest> findAllByStatusOrderByCreatedAtDesc(MatchTerminationRequestStatus status);

    @EntityGraph(attributePaths = {"match", "match.youth", "match.elder", "requesterUser", "admin"})
    Optional<MatchTerminationRequest> findById(UUID id);

    boolean existsByMatch_IdAndStatus(UUID matchId, MatchTerminationRequestStatus status);
}
