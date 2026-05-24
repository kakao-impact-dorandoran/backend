package com.dorandoran.backend.domain.match;

import com.dorandoran.backend.domain.elder.Elder;
import com.dorandoran.backend.domain.match.dto.MatchCreateRequest;
import com.dorandoran.backend.domain.user.Role;
import com.dorandoran.backend.domain.user.User;
import com.dorandoran.backend.global.jwt.JwtTokenProvider;
import com.dorandoran.backend.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MatchIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("TC-020: 사전 인사말이 포함된 매칭 요청은 201 + MATCHED 상태로 생성된다")
    void createMatchWithIcebreakingMessageSucceeds() throws Exception {
        User youth = dataFactory.createApprovedYouth();
        User guardian = dataFactory.createActiveUser(Role.GUARDIAN);
        Elder elder = dataFactory.createElder(guardian);
        String token = jwtTokenProvider.createAccessToken(youth.getId(), Role.YOUTH.name());

        MatchCreateRequest body = new MatchCreateRequest(elder.getId(), "안녕하세요! 잘 부탁드립니다.");

        mockMvc.perform(post("/api/v1/matches")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.matchId").exists())
                .andExpect(jsonPath("$.youthId").value(youth.getId().toString()))
                .andExpect(jsonPath("$.elderId").value(elder.getId().toString()))
                .andExpect(jsonPath("$.status").value("MATCHED"))
                .andExpect(jsonPath("$.icebreakingMessage").value("안녕하세요! 잘 부탁드립니다."));
    }

    @Test
    @DisplayName("TC-021: 사전 인사말이 누락(blank)된 매칭 요청은 400 + C001 으로 차단된다")
    void createMatchWithoutIcebreakingMessageFails() throws Exception {
        User youth = dataFactory.createApprovedYouth();
        User guardian = dataFactory.createActiveUser(Role.GUARDIAN);
        Elder elder = dataFactory.createElder(guardian);
        String token = jwtTokenProvider.createAccessToken(youth.getId(), Role.YOUTH.name());

        MatchCreateRequest body = new MatchCreateRequest(elder.getId(), "   ");

        mockMvc.perform(post("/api/v1/matches")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("C001"));
    }

    @Test
    @DisplayName("사전 인사말 null인 매칭 요청은 400 + C001 으로 차단된다")
    void createMatchWithNullIcebreakingMessageFails() throws Exception {
        User youth = dataFactory.createApprovedYouth();
        User guardian = dataFactory.createActiveUser(Role.GUARDIAN);
        Elder elder = dataFactory.createElder(guardian);
        String token = jwtTokenProvider.createAccessToken(youth.getId(), Role.YOUTH.name());

        String body = """
                { "elderId": "%s", "icebreakingMessage": null }
                """.formatted(elder.getId());

        mockMvc.perform(post("/api/v1/matches")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("C001"));
    }
}
