package com.nasa.asteral.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RegistrationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shortPasswordReturnsActionableValidationMessage() throws Exception {
        mockMvc.perform(post("/register").with(csrf())
                        .param("username", "new-user")
                        .param("password", "short"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attribute("error", "Password must be between 12 and 72 characters."));
    }

    @Test
    void whitespaceUsernameReturnsActionableValidationMessage() throws Exception {
        mockMvc.perform(post("/register").with(csrf())
                        .param("username", "   ")
                        .param("password", "valid-password"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attribute("error", "Username is required."));
    }

    @Test
    void missingResourceReturnsNotFoundInsteadOfSuccessfulErrorPage() throws Exception {
        mockMvc.perform(get("/favicon.ico"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error"))
                .andExpect(model().attribute("errorMessage", "The requested page or resource was not found."));
    }
}
