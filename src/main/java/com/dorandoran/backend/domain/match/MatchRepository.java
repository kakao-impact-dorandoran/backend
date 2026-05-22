package com.dorandoran.backend.domain.match;

import com.dorandoran.backend.domain.elder.Elder;
import com.dorandoran.backend.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MatchRepository extends JpaRepository<Match, UUID> {

    List<Match> findAllByYouth(User youth);

    List<Match> findAllByElder(Elder elder);

    long countByYouthAndStatusIn(User youth, List<MatchStatus> statuses);

    boolean existsByYouthAndElderAndStatusIn(User youth, Elder elder, List<MatchStatus> statuses);
}
