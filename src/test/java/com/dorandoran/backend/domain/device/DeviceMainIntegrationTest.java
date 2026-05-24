package com.dorandoran.backend.domain.device;

import com.dorandoran.backend.domain.elder.Elder;
import com.dorandoran.backend.domain.match.Match;
import com.dorandoran.backend.domain.user.User;
import com.dorandoran.backend.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DeviceMainIntegrationTest extends IntegrationTestSupport {

    @Test
    @DisplayName("TC-028: Device token 인증으로 전용 기기 메인을 조회하면 200과 오늘 일정/버튼/기기상태가 응답된다")
    void getDeviceMainSucceeds() throws Exception {
        User guardian = dataFactory.createGuardian();
        Elder elder = dataFactory.createElder(guardian);
        String deviceToken = "test-device-token-main-001";
        dataFactory.createRegisteredDevice(elder, deviceToken);

        User youth = dataFactory.createApprovedYouth();
        Match match = dataFactory.createMatchedMatch(youth, elder);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.plusMinutes(30).withSecond(0).withNano(0);
        LocalDateTime end = start.plusHours(1);
        dataFactory.createConfirmedSchedule(match, start, end);

        mockMvc.perform(get("/api/v1/device/main")
                        .header(HttpHeaders.AUTHORIZATION, "Device " + deviceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.elderId").value(elder.getId().toString()))
                .andExpect(jsonPath("$.elderName").value(elder.getName()))
                .andExpect(jsonPath("$.todaySchedule").exists())
                .andExpect(jsonPath("$.todaySchedule.scheduleId").exists())
                .andExpect(jsonPath("$.buttons").isArray())
                .andExpect(jsonPath("$.buttons", org.hamcrest.Matchers.hasItem("VIDEO_CALL")))
                .andExpect(jsonPath("$.buttons", org.hamcrest.Matchers.hasItem("AUDIO_CALL")))
                .andExpect(jsonPath("$.buttons", org.hamcrest.Matchers.hasItem("HELP_REQUEST")))
                .andExpect(jsonPath("$.deviceStatus").value("REGISTERED"));
    }
}
