package com.dorandoran.backend.domain.help;

import com.dorandoran.backend.domain.elder.Elder;
import com.dorandoran.backend.domain.user.Role;
import com.dorandoran.backend.domain.user.User;
import com.dorandoran.backend.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class HelpRequestIntegrationTest extends IntegrationTestSupport {

    @Test
    @DisplayName("TC-033: 전용 기기 인증으로 도움 요청을 생성하면 201 + PENDING 상태로 응답한다")
    void createHelpRequestSucceeds() throws Exception {
        User guardian = dataFactory.createActiveUser(Role.GUARDIAN);
        Elder elder = dataFactory.createElder(guardian);
        String deviceToken = "test-device-token-help-001";
        dataFactory.createRegisteredDevice(elder, deviceToken);

        String body = """
                {
                  "requestType": "DEVICE_HELP",
                  "deviceStatus": { "battery": 80, "wifi": "OK" }
                }
                """;

        mockMvc.perform(post("/api/v1/help-requests")
                        .header(HttpHeaders.AUTHORIZATION, "Device " + deviceToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.elderId").value(elder.getId().toString()))
                .andExpect(jsonPath("$.requestType").value("DEVICE_HELP"))
                .andExpect(jsonPath("$.handledStatus").value("PENDING"));
    }
}
