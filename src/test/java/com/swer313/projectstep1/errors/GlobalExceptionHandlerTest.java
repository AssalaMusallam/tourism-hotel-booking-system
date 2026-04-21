package com.swer313.projectstep1.errors;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void handleBaseApiException_returnsBadRequest_and_body() throws Exception {
        mockMvc.perform(get("/test/base"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Bad input"));
    }

    @Test
    void handleValidation_returnsBadRequest_and_errorsArray() throws Exception {
        // send empty body -> name missing -> validation should fail
        mockMvc.perform(post("/test/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.errors[0].field").exists());
    }

    @Test
    void handleBadCredentials_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/test/badcred"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @RestController
    static class TestController {
        @GetMapping("/test/base")
        public void base() {
            throw new BadRequestException("Bad input");
        }

        @PostMapping("/test/validate")
        public void validate(@RequestBody @Valid Dto dto) {
            // noop
        }

        @GetMapping("/test/badcred")
        public void badCred() {
            throw new BadCredentialsException("x");
        }
    }

    static class Dto {
        @NotBlank
        public String name;
    }
}

