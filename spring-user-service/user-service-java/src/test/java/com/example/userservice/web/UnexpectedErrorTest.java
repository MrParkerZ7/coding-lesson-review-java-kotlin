package com.example.userservice.web;

import static com.example.userservice.web.TestJwtDecoderConfig.ADMIN;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.userservice.service.UserService;

/** The catch-all: a bug inside the service must reach the client as a plain 500, without the exception's message. */
@SpringBootTest(properties = "app.seed-demo-users=false")
@AutoConfigureMockMvc
@Import(TestJwtDecoderConfig.class)
class UnexpectedErrorTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    UserService users;

    @Test
    void anUnexpectedExceptionIsA500ThatTellsTheClientNothing() throws Exception {
        when(users.search(null, null)).thenThrow(new IllegalStateException("secret internal detail: the password is hunter2"));

        mvc.perform(get("/api/users").header("Authorization", "Bearer " + ADMIN))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("urn:problem:internal-error"))
                .andExpect(content().string(not(containsString("hunter2"))));
    }
}
