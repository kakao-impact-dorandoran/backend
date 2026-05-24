package com.dorandoran.backend.domain.call;

import com.dorandoran.backend.domain.call.dto.CallStartRequest;
import com.dorandoran.backend.domain.elder.Elder;
import com.dorandoran.backend.domain.match.Match;
import com.dorandoran.backend.domain.user.User;
import com.dorandoran.backend.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CallIntegrationTest extends IntegrationTestSupport {

    @Test
    @DisplayName("TC-030: Device token 인증으로 화상 통화를 시작하면 201과 VIDEO/PENDING CallLog가 생성된다")
    void startVideoCallSucceeds() throws Exception {
        CallFixture fixture = setupCallFixture("test-device-token-video-001");

        CallStartRequest body = new CallStartRequest(fixture.match().getId(), null);

        mockMvc.perform(post("/api/v1/calls/video")
                        .header(HttpHeaders.AUTHORIZATION, "Device " + fixture.deviceToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.callLogId").exists())
                .andExpect(jsonPath("$.matchId").value(fixture.match().getId().toString()))
                .andExpect(jsonPath("$.callType").value("VIDEO"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("TC-031: Device token 인증으로 음성 통화를 시작하면 201과 AUDIO/PENDING CallLog가 생성된다")
    void startAudioCallSucceeds() throws Exception {
        CallFixture fixture = setupCallFixture("test-device-token-audio-001");

        CallStartRequest body = new CallStartRequest(fixture.match().getId(), null);

        mockMvc.perform(post("/api/v1/calls/audio")
                        .header(HttpHeaders.AUTHORIZATION, "Device " + fixture.deviceToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.callType").value("AUDIO"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    private CallFixture setupCallFixture(String deviceToken) {
        User guardian = dataFactory.createGuardian();
        Elder elder = dataFactory.createElder(guardian);
        dataFactory.createRegisteredDevice(elder, deviceToken);

        User youth = dataFactory.createApprovedYouth();
        Match match = dataFactory.createMatchedMatch(youth, elder);
        return new CallFixture(match, deviceToken);
    }

    private record CallFixture(Match match, String deviceToken) {
    }
}
