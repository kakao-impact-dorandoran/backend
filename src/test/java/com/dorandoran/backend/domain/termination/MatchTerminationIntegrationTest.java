package com.dorandoran.backend.domain.termination;

import com.dorandoran.backend.domain.elder.Elder;
import com.dorandoran.backend.domain.match.Match;
import com.dorandoran.backend.domain.match.MatchRepository;
import com.dorandoran.backend.domain.match.MatchStatus;
import com.dorandoran.backend.domain.termination.dto.AdminMatchTerminationProcessRequest;
import com.dorandoran.backend.domain.termination.dto.MatchTerminationCreateRequest;
import com.dorandoran.backend.domain.user.Role;
import com.dorandoran.backend.domain.user.User;
import com.dorandoran.backend.global.jwt.JwtTokenProvider;
import com.dorandoran.backend.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MatchTerminationIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private MatchTerminationRequestRepository terminationRequestRepository;

    @Test
    @DisplayName("TC-041: 청년이 매칭 중단을 요청하면 status가 REQUESTED 가 되고 매칭은 ENDED 가 아니다")
    void createTerminationRequestSucceeds() throws Exception {
        User youth = dataFactory.createApprovedYouth();
        User guardian = dataFactory.createGuardian();
        Elder elder = dataFactory.createElder(guardian);
        Match match = dataFactory.createMatchedMatch(youth, elder);

        String token = jwtTokenProvider.createAccessToken(youth.getId(), Role.YOUTH.name());
        MatchTerminationCreateRequest body = new MatchTerminationCreateRequest("일정 충돌이 잦아 중단을 요청합니다.");

        mockMvc.perform(post("/api/v1/matches/{matchId}/termination-requests", match.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.requestId").exists())
                .andExpect(jsonPath("$.matchId").value(match.getId().toString()))
                .andExpect(jsonPath("$.status").value("REQUESTED"));

        Match reloaded = matchRepository.findById(match.getId()).orElseThrow();
        assertThat(reloaded.getStatus()).isNotEqualTo(MatchStatus.ENDED);
    }

    @Test
    @DisplayName("TC-043: 관리자가 매칭 중단 요청을 APPROVED 처리하면 요청은 APPROVED, 매칭은 ENDED 가 된다")
    void adminApprovesTerminationRequest() throws Exception {
        User youth = dataFactory.createApprovedYouth();
        User guardian = dataFactory.createGuardian();
        Elder elder = dataFactory.createElder(guardian);
        Match match = dataFactory.createMatchedMatch(youth, elder);
        MatchTerminationRequest request = dataFactory.createRequestedTermination(youth, match);

        User admin = dataFactory.createAdmin();
        String adminToken = jwtTokenProvider.createAccessToken(admin.getId(), Role.ADMIN.name());
        AdminMatchTerminationProcessRequest body = new AdminMatchTerminationProcessRequest(
                MatchTerminationRequestStatus.APPROVED, "관리자 승인");

        mockMvc.perform(patch("/api/v1/admin/match-termination-requests/{requestId}", request.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").value(request.getId().toString()))
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.processedAt").exists());

        MatchTerminationRequest reloadedReq = terminationRequestRepository.findById(request.getId()).orElseThrow();
        assertThat(reloadedReq.getStatus()).isEqualTo(MatchTerminationRequestStatus.APPROVED);

        Match reloadedMatch = matchRepository.findById(match.getId()).orElseThrow();
        assertThat(reloadedMatch.getStatus()).isEqualTo(MatchStatus.ENDED);
    }
}
