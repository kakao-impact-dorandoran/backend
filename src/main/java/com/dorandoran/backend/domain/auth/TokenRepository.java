package com.dorandoran.backend.domain.auth;

import com.dorandoran.backend.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TokenRepository extends JpaRepository<Token, UUID> {

    Optional<Token> findByUserAndRevokedFalse(User user);

    Optional<Token> findByRefreshTokenAndRevokedFalse(String refreshToken);
}
