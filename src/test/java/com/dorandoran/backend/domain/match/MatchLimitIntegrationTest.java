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

class MatchLimitIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("TC-022: 담당 인원 5명을 채운 청년이 6번째 매칭을 시도하면 409 + M001 으로 차단된다")
    void createMatchOverLimitFails() throws Exception {
        User youth = dataFactory.createApprovedYouth();
        User guardian = dataFactory.createGuardian();

        for (int i = 0; i < 5; i++) {
            Elder existingElder = dataFactory.createElder(guardian);
            dataFactory.createMatchedMatch(youth, existingElder);
        }
        dataFactory.createYouthMatchLimit(youth, 5);

        Elder targetElder = dataFactory.createElder(guardian);
        String token = jwtTokenProvider.createAccessToken(youth.getId(), Role.YOUTH.name());
        MatchCreateRequest body = new MatchCreateRequest(targetElder.getId(), "여섯번째 인사말입니다.");

        mockMvc.perform(post("/api/v1/matches")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("M001"));
    }
}
