package com.example.userservice.web;

import static com.example.userservice.web.TestJwtDecoderConfig.ADMIN;
import static com.example.userservice.web.TestJwtDecoderConfig.ADMIN_READ_ONLY;
import static com.example.userservice.web.TestJwtDecoderConfig.ALICE;
import static com.example.userservice.web.TestJwtDecoderConfig.GHOST;
import static com.example.userservice.web.TestJwtDecoderConfig.NO_SUBJECT;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.example.userservice.domain.User;
import com.example.userservice.persistence.UserRepository;

/** The whole API through the real Spring MVC + Spring Security stack, with an in-memory store and fake tokens. */
@SpringBootTest(properties = "app.seed-demo-users=false")
@AutoConfigureMockMvc
@Import(TestJwtDecoderConfig.class)
class UserApiTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    UserRepository repository;

    @BeforeEach
    void emptyTheStore() {
        for (User user : repository.findAll()) {
            repository.deleteById(user.id());
        }
    }

    private static MockHttpServletRequestBuilder as(String token, MockHttpServletRequestBuilder request) {
        return request.header("Authorization", "Bearer " + token);
    }

    private static MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder request, String body) {
        return request.contentType(MediaType.APPLICATION_JSON).content(body);
    }

    /** Creates a user as admin and returns its id (the last segment of the Location header). */
    private String createUser(String username) throws Exception {
        String location = mvc.perform(json(as(ADMIN, post("/api/users")), """
                        {"username": "%s", "email": "%s@example.com", "displayName": "Display %s"}
                        """.formatted(username, username, username)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location");
        return location.substring(location.lastIndexOf('/') + 1);
    }

    // ---- security ----

    @Test
    void noTokenIs401WithAProblemBodyAndAChallenge() throws Exception {
        mvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", "Bearer"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("urn:problem:unauthenticated"))
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void aTokenTheServerDoesNotKnowIs401() throws Exception {
        mvc.perform(as("garbage", get("/api/users")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.type").value("urn:problem:unauthenticated"));
    }

    @Test
    void aUserRoleIsNotEnoughToListUsers() throws Exception {
        mvc.perform(as(ALICE, get("/api/users")))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("urn:problem:forbidden"));
    }

    @Test
    void anAdminWhoseTokenLacksTheWriteScopeCannotCreate() throws Exception {
        mvc.perform(json(as(ADMIN_READ_ONLY, post("/api/users")), """
                        {"username": "carol", "email": "carol@example.com", "displayName": "Carol"}
                        """))
                .andExpect(status().isForbidden());
        mvc.perform(as(ADMIN_READ_ONLY, get("/api/users"))).andExpect(status().isOk());
    }

    @Test
    void healthIsPublicAndEverythingElseIsDenied() throws Exception {
        mvc.perform(get("/actuator/health")).andExpect(status().isOk());
        mvc.perform(as(ADMIN, get("/somewhere-else"))).andExpect(status().isForbidden());
    }

    // ---- create ----

    @Test
    void createReturns201WithLocationAndLeavesNullFieldsOut() throws Exception {
        mvc.perform(json(as(ADMIN, post("/api/users")), """
                        {"username": "carol", "email": "carol@example.com", "displayName": "Carol"}
                        """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.matchesPattern(".*/api/users/[0-9a-f-]{36}")))
                .andExpect(jsonPath("$.username").value("carol"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.phone").doesNotExist())
                .andExpect(jsonPath("$.address").doesNotExist());
    }

    @Test
    void createWithBadFieldsListsEveryBadField() throws Exception {
        mvc.perform(json(as(ADMIN, post("/api/users")), """
                        {"username": "A", "email": "nope", "displayName": "  ", "phone": "12",
                         "address": {"street": "", "city": "", "postalCode": "", "country": "Thailand"}}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("urn:problem:validation-failed"))
                .andExpect(jsonPath("$.errors[*].field", containsInAnyOrder(
                        "username", "email", "displayName", "phone",
                        "address.street", "address.city", "address.postalCode", "address.country")));
    }

    @Test
    void createWithMissingRequiredFieldsIsAValidationError() throws Exception {
        // a missing field reaches the record as null, so bean validation sees all three at once (the Kotlin twin reports the first)
        mvc.perform(json(as(ADMIN, post("/api/users")), "{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("urn:problem:validation-failed"))
                .andExpect(jsonPath("$.errors[*].field", containsInAnyOrder("username", "email", "displayName")))
                .andExpect(jsonPath("$.errors[?(@.field == 'username')].message").value("must not be null"));
    }

    @Test
    void createWithAnAddressThatLacksACityNamesTheNestedField() throws Exception {
        mvc.perform(json(as(ADMIN, post("/api/users")), """
                        {"username": "carol", "email": "carol@example.com", "displayName": "Carol",
                         "address": {"street": "1 Main St", "postalCode": "10110", "country": "TH"}}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("urn:problem:validation-failed"))
                .andExpect(jsonPath("$.errors[0].field").value("address.city"));
    }

    @Test
    void createWithBrokenJsonIsAMalformedBody() throws Exception {
        mvc.perform(json(as(ADMIN, post("/api/users")), "{ not json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("urn:problem:malformed-body"));
    }

    @Test
    void createWithATakenUsernameIs409() throws Exception {
        createUser("carol");

        mvc.perform(json(as(ADMIN, post("/api/users")), """
                        {"username": "carol", "email": "another@example.com", "displayName": "Another Carol"}
                        """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").value("urn:problem:duplicate-user"))
                .andExpect(jsonPath("$.field").value("username"));
    }

    // ---- read ----

    @Test
    void getUnknownIdIs404AndABadIdIs400() throws Exception {
        mvc.perform(as(ADMIN, get("/api/users/00000000-0000-0000-0000-000000000000")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("urn:problem:user-not-found"));
        mvc.perform(as(ADMIN, get("/api/users/not-a-uuid")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listFiltersByStatusAndTextAndRejectsAnUnknownStatus() throws Exception {
        String carol = createUser("carol");
        createUser("dave");
        mvc.perform(json(as(ADMIN, patch("/api/users/" + carol)), """
                {"status": "DISABLED"}"""));

        mvc.perform(as(ADMIN, get("/api/users"))).andExpect(jsonPath("$", hasSize(2)));
        mvc.perform(as(ADMIN, get("/api/users?status=DISABLED")))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].username").value("carol"));
        mvc.perform(as(ADMIN, get("/api/users?q=DAV")))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].username").value("dave"));
        mvc.perform(as(ADMIN, get("/api/users?status=NOPE"))).andExpect(status().isBadRequest());
    }

    // ---- edit ----

    @Test
    void patchChangesOnlyTheFieldsSent() throws Exception {
        String id = createUser("carol");

        mvc.perform(json(as(ADMIN, patch("/api/users/" + id)), """
                        {"displayName": "Carol C.", "phone": "+66812345678",
                         "address": {"street": "1 Main St", "city": "Bangkok", "postalCode": "10110", "country": "TH"}}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("Carol C."))
                .andExpect(jsonPath("$.phone").value("+66812345678"))
                .andExpect(jsonPath("$.address.city").value("Bangkok"))
                .andExpect(jsonPath("$.email").value("carol@example.com"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void patchWithNothingToChangeIs400() throws Exception {
        String id = createUser("carol");

        mvc.perform(json(as(ADMIN, patch("/api/users/" + id)), "{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("urn:problem:empty-update"));
    }

    @Test
    void patchWithBlankOrInvalidValuesIsRejectedButNullIsNot() throws Exception {
        String id = createUser("carol");

        mvc.perform(json(as(ADMIN, patch("/api/users/" + id)), """
                        {"displayName": "  ", "email": "nope"}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field", containsInAnyOrder("displayName", "email")));
        // an explicit null means "unchanged", the same as leaving the field out
        mvc.perform(json(as(ADMIN, patch("/api/users/" + id)), """
                        {"displayName": null, "phone": "+66812345678"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("Display carol"));
    }

    @Test
    void aStatusThatIsNotAnEnumValueIs400() throws Exception {
        String id = createUser("carol");

        mvc.perform(json(as(ADMIN, patch("/api/users/" + id)), """
                        {"status": "NOPE"}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("urn:problem:malformed-body"))
                .andExpect(jsonPath("$.errors[0].field").value("status"))
                .andExpect(jsonPath("$.errors[0].message").value("invalid value"));
    }

    // ---- me ----

    @Test
    void meReturnsTheCallersOwnProfile() throws Exception {
        createUser("alice");

        mvc.perform(as(ALICE, get("/api/users/me")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice"));
    }

    @Test
    void aTokenWithoutASubjectCannotUseMe() throws Exception {
        mvc.perform(as(NO_SUBJECT, get("/api/users/me")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.type").value("urn:problem:unauthenticated"));
    }

    @Test
    void meWithoutAProfileIs404() throws Exception {
        mvc.perform(as(GHOST, get("/api/users/me")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("urn:problem:user-not-found"));
    }

    @Test
    void aUserEditsTheirOwnProfileButCannotTouchStatus() throws Exception {
        createUser("alice");

        mvc.perform(json(as(ALICE, patch("/api/users/me")), """
                        {"displayName": "Alice Again"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("Alice Again"));

        mvc.perform(json(as(ALICE, patch("/api/users/me")), """
                        {"status": "ACTIVE"}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("status"))
                .andExpect(jsonPath("$.errors[0].message").value("unknown field"));
    }

    @Test
    void aUserCannotReachSomeoneElsesProfileById() throws Exception {
        String carol = createUser("carol");

        mvc.perform(as(ALICE, get("/api/users/" + carol))).andExpect(status().isForbidden());
        mvc.perform(json(as(ALICE, patch("/api/users/" + carol)), """
                {"displayName": "hacked"}""")).andExpect(status().isForbidden());
        mvc.perform(as(ALICE, delete("/api/users/" + carol))).andExpect(status().isForbidden());
    }

    // ---- delete ----

    @Test
    void deleteIs204ThenTheUserIsGone() throws Exception {
        String id = createUser("carol");

        mvc.perform(as(ADMIN, delete("/api/users/" + id))).andExpect(status().isNoContent());
        mvc.perform(as(ADMIN, get("/api/users/" + id))).andExpect(status().isNotFound());
        mvc.perform(as(ADMIN, delete("/api/users/" + id))).andExpect(status().isNotFound());
    }

    @Test
    void errorsAreProblemDocumentsEvenForUnsupportedMethods() throws Exception {
        mvc.perform(as(ADMIN, org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/users")))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.instance", endsWith("/api/users")));
    }
}
