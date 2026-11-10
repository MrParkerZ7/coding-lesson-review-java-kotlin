package com.example.userservice.web

import com.example.userservice.persistence.UserRepository
import com.example.userservice.web.TestJwtDecoderConfig.Companion.ADMIN
import com.example.userservice.web.TestJwtDecoderConfig.Companion.ADMIN_READ_ONLY
import com.example.userservice.web.TestJwtDecoderConfig.Companion.ALICE
import com.example.userservice.web.TestJwtDecoderConfig.Companion.GHOST
import com.example.userservice.web.TestJwtDecoderConfig.Companion.NO_SUBJECT
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.endsWith
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.matchesPattern
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockHttpServletRequestDsl
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put

/** The whole API through the real Spring MVC + Spring Security stack, with an in-memory store and fake tokens. */
@SpringBootTest(properties = ["app.seed-demo-users=false"])
@AutoConfigureMockMvc
@Import(TestJwtDecoderConfig::class)
class UserApiTest(
    @Autowired private val mvc: MockMvc,
    @Autowired private val repository: UserRepository,
) {

    @BeforeEach
    fun `empty the store`() {
        repository.findAll().forEach { repository.deleteById(it.id) }
    }

    private fun MockHttpServletRequestDsl.bearer(token: String) = header("Authorization", "Bearer $token")

    private fun MockHttpServletRequestDsl.json(body: String) {
        contentType = MediaType.APPLICATION_JSON
        content = body
    }

    /** Creates a user as admin and returns its id (the last segment of the Location header). */
    private fun createUser(username: String): String {
        val location = mvc.post("/api/users") {
            bearer(ADMIN)
            json("""{"username": "$username", "email": "$username@example.com", "displayName": "Display $username"}""")
        }.andExpect { status { isCreated() } }.andReturn().response.getHeader("Location")!!
        return location.substringAfterLast('/')
    }

    // ---- security ----

    @Test
    fun `no token is 401 with a problem body and a challenge`() {
        mvc.get("/api/users").andExpect {
            status { isUnauthorized() }
            header { string("WWW-Authenticate", "Bearer") }
            content { contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON) }
            jsonPath("$.type") { value("urn:problem:unauthenticated") }
            jsonPath("$.status") { value(401) }
        }
    }

    @Test
    fun `a token the server does not know is 401`() {
        mvc.get("/api/users") { bearer("garbage") }.andExpect {
            status { isUnauthorized() }
            jsonPath("$.type") { value("urn:problem:unauthenticated") }
        }
    }

    @Test
    fun `a user role is not enough to list users`() {
        mvc.get("/api/users") { bearer(ALICE) }.andExpect {
            status { isForbidden() }
            content { contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON) }
            jsonPath("$.type") { value("urn:problem:forbidden") }
        }
    }

    @Test
    fun `an admin whose token lacks the write scope cannot create`() {
        mvc.post("/api/users") {
            bearer(ADMIN_READ_ONLY)
            json("""{"username": "carol", "email": "carol@example.com", "displayName": "Carol"}""")
        }.andExpect { status { isForbidden() } }
        mvc.get("/api/users") { bearer(ADMIN_READ_ONLY) }.andExpect { status { isOk() } }
    }

    @Test
    fun `health is public and everything else is denied`() {
        mvc.get("/actuator/health").andExpect { status { isOk() } }
        mvc.get("/somewhere-else") { bearer(ADMIN) }.andExpect { status { isForbidden() } }
    }

    // ---- create ----

    @Test
    fun `create returns 201 with Location and leaves null fields out`() {
        mvc.post("/api/users") {
            bearer(ADMIN)
            json("""{"username": "carol", "email": "carol@example.com", "displayName": "Carol"}""")
        }.andExpect {
            status { isCreated() }
            header { string("Location", matchesPattern(".*/api/users/[0-9a-f-]{36}")) }
            jsonPath("$.username") { value("carol") }
            jsonPath("$.status") { value("ACTIVE") }
            jsonPath("$.createdAt") { exists() }
            jsonPath("$.phone") { doesNotExist() }
            jsonPath("$.address") { doesNotExist() }
        }
    }

    @Test
    fun `create with bad fields lists every bad field`() {
        mvc.post("/api/users") {
            bearer(ADMIN)
            json(
                """{"username": "A", "email": "nope", "displayName": "  ", "phone": "12",
                    "address": {"street": "", "city": "", "postalCode": "", "country": "Thailand"}}""",
            )
        }.andExpect {
            status { isBadRequest() }
            content { contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON) }
            jsonPath("$.type") { value("urn:problem:validation-failed") }
            jsonPath("$.errors[*].field") {
                value(
                    containsInAnyOrder(
                        "username", "email", "displayName", "phone",
                        "address.street", "address.city", "address.postalCode", "address.country",
                    ),
                )
            }
        }
    }

    @Test
    fun `create with missing required fields is a validation error`() {
        // Kotlin: username is a non-null String, so Jackson's Kotlin module rejects the body before bean validation runs,
        // and stops at the first missing field. The handler words it like bean validation. The Java twin lists all three.
        mvc.post("/api/users") {
            bearer(ADMIN)
            json("{}")
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.type") { value("urn:problem:validation-failed") }
            jsonPath("$.errors") { value(hasSize<Any>(1)) }
            jsonPath("$.errors[0].field") { value("username") }
            jsonPath("$.errors[0].message") { value("must not be null") }
        }
    }

    @Test
    fun `create with an address that lacks a city names the nested field`() {
        mvc.post("/api/users") {
            bearer(ADMIN)
            json(
                """{"username": "carol", "email": "carol@example.com", "displayName": "Carol",
                    "address": {"street": "1 Main St", "postalCode": "10110", "country": "TH"}}""",
            )
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.type") { value("urn:problem:validation-failed") }
            jsonPath("$.errors[0].field") { value("address.city") }
        }
    }

    @Test
    fun `create with broken json is a malformed body`() {
        mvc.post("/api/users") {
            bearer(ADMIN)
            json("{ not json")
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.type") { value("urn:problem:malformed-body") }
        }
    }

    @Test
    fun `create with a taken username is 409`() {
        createUser("carol")

        mvc.post("/api/users") {
            bearer(ADMIN)
            json("""{"username": "carol", "email": "another@example.com", "displayName": "Another Carol"}""")
        }.andExpect {
            status { isConflict() }
            jsonPath("$.type") { value("urn:problem:duplicate-user") }
            jsonPath("$.field") { value("username") }
        }
    }

    // ---- read ----

    @Test
    fun `get unknown id is 404 and a bad id is 400`() {
        mvc.get("/api/users/00000000-0000-0000-0000-000000000000") { bearer(ADMIN) }.andExpect {
            status { isNotFound() }
            jsonPath("$.type") { value("urn:problem:user-not-found") }
        }
        mvc.get("/api/users/not-a-uuid") { bearer(ADMIN) }.andExpect { status { isBadRequest() } }
    }

    @Test
    fun `list filters by status and text and rejects an unknown status`() {
        val carol = createUser("carol")
        createUser("dave")
        mvc.patch("/api/users/$carol") {
            bearer(ADMIN)
            json("""{"status": "DISABLED"}""")
        }

        mvc.get("/api/users") { bearer(ADMIN) }.andExpect { jsonPath("$") { value(hasSize<Any>(2)) } }
        mvc.get("/api/users?status=DISABLED") { bearer(ADMIN) }.andExpect {
            jsonPath("$") { value(hasSize<Any>(1)) }
            jsonPath("$[0].username") { value("carol") }
        }
        mvc.get("/api/users?q=DAV") { bearer(ADMIN) }.andExpect {
            jsonPath("$") { value(hasSize<Any>(1)) }
            jsonPath("$[0].username") { value("dave") }
        }
        mvc.get("/api/users?status=NOPE") { bearer(ADMIN) }.andExpect { status { isBadRequest() } }
    }

    // ---- edit ----

    @Test
    fun `patch changes only the fields sent`() {
        val id = createUser("carol")

        mvc.patch("/api/users/$id") {
            bearer(ADMIN)
            json(
                """{"displayName": "Carol C.", "phone": "+66812345678",
                    "address": {"street": "1 Main St", "city": "Bangkok", "postalCode": "10110", "country": "TH"}}""",
            )
        }.andExpect {
            status { isOk() }
            jsonPath("$.displayName") { value("Carol C.") }
            jsonPath("$.phone") { value("+66812345678") }
            jsonPath("$.address.city") { value("Bangkok") }
            jsonPath("$.email") { value("carol@example.com") }
            jsonPath("$.status") { value("ACTIVE") }
        }
    }

    @Test
    fun `patch with nothing to change is 400`() {
        val id = createUser("carol")

        mvc.patch("/api/users/$id") {
            bearer(ADMIN)
            json("{}")
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.type") { value("urn:problem:empty-update") }
        }
    }

    @Test
    fun `patch with blank or invalid values is rejected but null is not`() {
        val id = createUser("carol")

        mvc.patch("/api/users/$id") {
            bearer(ADMIN)
            json("""{"displayName": "  ", "email": "nope"}""")
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.errors[*].field") { value(containsInAnyOrder("displayName", "email")) }
        }
        // an explicit null means "unchanged", the same as leaving the field out
        mvc.patch("/api/users/$id") {
            bearer(ADMIN)
            json("""{"displayName": null, "phone": "+66812345678"}""")
        }.andExpect {
            status { isOk() }
            jsonPath("$.displayName") { value("Display carol") }
        }
    }

    @Test
    fun `a status that is not an enum value is 400`() {
        val id = createUser("carol")

        mvc.patch("/api/users/$id") {
            bearer(ADMIN)
            json("""{"status": "NOPE"}""")
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.type") { value("urn:problem:malformed-body") }
            jsonPath("$.errors[0].field") { value("status") }
            jsonPath("$.errors[0].message") { value("invalid value") }
        }
    }

    // ---- me ----

    @Test
    fun `me returns the callers own profile`() {
        createUser("alice")

        mvc.get("/api/users/me") { bearer(ALICE) }.andExpect {
            status { isOk() }
            jsonPath("$.username") { value("alice") }
        }
    }

    @Test
    fun `a token without a subject cannot use me`() {
        mvc.get("/api/users/me") { bearer(NO_SUBJECT) }.andExpect {
            status { isUnauthorized() }
            jsonPath("$.type") { value("urn:problem:unauthenticated") }
        }
    }

    @Test
    fun `me without a profile is 404`() {
        mvc.get("/api/users/me") { bearer(GHOST) }.andExpect {
            status { isNotFound() }
            jsonPath("$.type") { value("urn:problem:user-not-found") }
        }
    }

    @Test
    fun `a user edits their own profile but cannot touch status`() {
        createUser("alice")

        mvc.patch("/api/users/me") {
            bearer(ALICE)
            json("""{"displayName": "Alice Again"}""")
        }.andExpect {
            status { isOk() }
            jsonPath("$.displayName") { value("Alice Again") }
        }

        mvc.patch("/api/users/me") {
            bearer(ALICE)
            json("""{"status": "ACTIVE"}""")
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.errors[0].field") { value("status") }
            jsonPath("$.errors[0].message") { value("unknown field") }
        }
    }

    @Test
    fun `a user cannot reach someone elses profile by id`() {
        val carol = createUser("carol")

        mvc.get("/api/users/$carol") { bearer(ALICE) }.andExpect { status { isForbidden() } }
        mvc.patch("/api/users/$carol") {
            bearer(ALICE)
            json("""{"displayName": "hacked"}""")
        }.andExpect { status { isForbidden() } }
        mvc.delete("/api/users/$carol") { bearer(ALICE) }.andExpect { status { isForbidden() } }
    }

    // ---- delete ----

    @Test
    fun `delete is 204 then the user is gone`() {
        val id = createUser("carol")

        mvc.delete("/api/users/$id") { bearer(ADMIN) }.andExpect { status { isNoContent() } }
        mvc.get("/api/users/$id") { bearer(ADMIN) }.andExpect { status { isNotFound() } }
        mvc.delete("/api/users/$id") { bearer(ADMIN) }.andExpect { status { isNotFound() } }
    }

    @Test
    fun `errors are problem documents even for unsupported methods`() {
        mvc.put("/api/users") { bearer(ADMIN) }.andExpect {
            status { isMethodNotAllowed() }
            content { contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON) }
            jsonPath("$.instance") { value(endsWith("/api/users")) }
        }
    }
}
