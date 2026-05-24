package com.dorandoran.backend.domain.auth;

import com.dorandoran.backend.domain.auth.dto.LoginRequest;
import com.dorandoran.backend.domain.user.User;
import com.dorandoran.backend.support.IntegrationTestSupport;
import com.dorandoran.backend.support.TestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthIntegrationTest extends IntegrationTestSupport {

    @Test
    @DisplayName("TC-001: 승인 완료 청년이 로그인하면 200 OK와 accessToken을 받는다")
    void approvedYouthLoginSucceeds() throws Exception {
        User youth = dataFactory.createApprovedYouth();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload(youth.getEmail())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value(notNullValue()))
                .andExpect(jsonPath("$.refreshToken").value(notNullValue()))
                .andExpect(jsonPath("$.user.email").value(youth.getEmail()))
                .andExpect(jsonPath("$.user.role").value("YOUTH"));
    }

    @Test
    @DisplayName("TC-002: 승인 대기 청년 로그인은 403 + U005 코드로 차단된다")
    void pendingYouthLoginBlocked() throws Exception {
        User youth = dataFactory.createPendingYouth();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload(youth.getEmail())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("U005"));
    }

    @Test
    @DisplayName("TC-005: 반려된 청년 로그인은 403 + U006 코드로 차단된다")
    void rejectedYouthLoginBlocked() throws Exception {
        User youth = dataFactory.createRejectedYouth("프로필 정보가 부족합니다.");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload(youth.getEmail())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("U006"));
    }

    @Test
    @DisplayName("TC-003: SUSPENDED 유저 로그인은 403 + U004 코드로 차단된다")
    void suspendedUserLoginBlocked() throws Exception {
        User suspended = dataFactory.createSuspendedYouth();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload(suspended.getEmail())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("U004"));
    }

    private String loginPayload(String email) throws Exception {
        return objectMapper.writeValueAsString(new LoginRequest(email, TestDataFactory.DEFAULT_PASSWORD));
    }
}
