package com.nasa.asteral.configuration;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityConfigurationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void anonymousUserCannotAccessFavorites() throws Exception {
        mockMvc.perform(get("/user/favorite/all"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(authorities = "USER")
    void stateChangeWithoutCsrfIsRejected() throws Exception {
        mockMvc.perform(post("/user/favorite/add/123"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "alice", authorities = "USER")
    void authorizedStateChangeWithCsrfIsAccepted() throws Exception {
        mockMvc.perform(post("/user/favorite/add/123").with(csrf()))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void correlationIdIsReturnedWithoutExposingHealthDetails() throws Exception {
        mockMvc.perform(get("/actuator/health").header(CorrelationIdFilter.HEADER_NAME, "test-correlation"))
                .andExpect(status().isOk())
                .andExpect(header().string(CorrelationIdFilter.HEADER_NAME, "test-correlation"));
    }
}
