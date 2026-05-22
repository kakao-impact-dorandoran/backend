package com.dorandoran.backend.domain.auth.service;

import com.dorandoran.backend.domain.auth.Token;
import com.dorandoran.backend.domain.auth.TokenRepository;
import com.dorandoran.backend.domain.auth.dto.LoginRequest;
import com.dorandoran.backend.domain.auth.dto.LoginResponse;
import com.dorandoran.backend.domain.user.User;
import com.dorandoran.backend.domain.user.UserRepository;
import com.dorandoran.backend.domain.user.UserStatus;
import com.dorandoran.backend.domain.user.dto.AuthUserResponse;
import com.dorandoran.backend.global.error.BusinessException;
import com.dorandoran.backend.global.error.ErrorCode;
import com.dorandoran.backend.global.jwt.JwtProperties;
import com.dorandoran.backend.global.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Inactive or suspended account");
        }
        if (user.getPassword() == null
                || !passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_MISMATCH);
        }

        String role = user.getRole().name();
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), role);
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId(), role);

        LocalDateTime refreshExpiry = LocalDateTime.now()
                .plusNanos(jwtProperties.getRefreshTokenValidityMs() * 1_000_000L);

        tokenRepository.findByUserAndRevokedFalse(user)
                .ifPresentOrElse(
                        existing -> existing.rotate(refreshToken, refreshExpiry),
                        () -> tokenRepository.save(Token.builder()
                                .user(user)
                                .refreshToken(refreshToken)
                                .expiresAt(refreshExpiry)
                                .build())
                );

        return new LoginResponse(accessToken, refreshToken, AuthUserResponse.from(user));
    }
}
