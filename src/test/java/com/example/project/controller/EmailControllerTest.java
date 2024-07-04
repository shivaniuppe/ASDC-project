package com.example.project.controller;

import com.example.project.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.mockito.Mockito.*;

public class EmailControllerTest {

    @InjectMocks
    private EmailController emailController;

    @Mock
    private EmailService emailService;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(emailController).build();
    }

    @Test
    public void testSendEmail_Success() throws Exception {
        doNothing().when(emailService).sendEmail(anyString(), anyString(), anyString());

        mockMvc.perform(MockMvcRequestBuilders.get("/sendEmail")
                        .param("to", "test@example.com")
                        .param("subject", "Test Subject")
                        .param("body", "Test Body")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("Email sent successfully"));

        verify(emailService, times(1)).sendEmail("test@example.com", "Test Subject", "Test Body");
    }
}
