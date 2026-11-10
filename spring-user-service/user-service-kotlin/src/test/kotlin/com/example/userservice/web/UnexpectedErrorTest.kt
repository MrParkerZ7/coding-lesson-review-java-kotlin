package com.example.userservice.web

import com.example.userservice.service.UserService
import com.example.userservice.web.TestJwtDecoderConfig.Companion.ADMIN
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

/** The catch-all: a bug inside the service must reach the client as a plain 500, without the exception's message. */
@SpringBootTest(properties = ["app.seed-demo-users=false"])
@AutoConfigureMockMvc
@Import(TestJwtDecoderConfig::class)
class UnexpectedErrorTest(@Autowired private val mvc: MockMvc) {

    @MockitoBean
    lateinit var users: UserService

    @Test
    fun `an unexpected exception is a 500 that tells the client nothing`() {
        // `when` is a Kotlin keyword, hence the backticks. Both arguments are nullable, so passing null is allowed.
        Mockito.`when`(users.search(null, null)).thenThrow(IllegalStateException("secret internal detail: the password is hunter2"))

        mvc.get("/api/users") { header("Authorization", "Bearer $ADMIN") }.andExpect {
            status { isInternalServerError() }
            content { contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON) }
            jsonPath("$.type") { value("urn:problem:internal-error") }
            content { string(not(containsString("hunter2"))) }
        }
    }
}
