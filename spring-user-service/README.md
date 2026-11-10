# spring-user-service

> One user-management API written twice, in Java and in Kotlin, behind one shared OAuth2 login server. Read the two side by side to see how a Spring Boot 4 service is laid out today.

**Spring Boot 4.1.2** (the same as [`../srping-boot-api-calculator`](../srping-boot-api-calculator)), Spring Framework 7, Spring Security 7, Jackson 3, JDK 21. Kotlin 2.4.21 (as in `../kotlin-review`).

## What is in it

```
spring-user-service/
  pom.xml                  parent: Boot 4.1.2, lists the three modules
  auth-server/             SHARED. OAuth2 authorization server, issues the JWTs.  :9000
  user-service-java/       the user service in Java                               :8081
  user-service-kotlin/     the same user service in Kotlin                        :8082
  scripts/token.sh         log in as a demo user with curl, print an access token
  scripts/smoke.sh         the same 34 HTTP checks against either service
```

The two services are two implementations of one requirement: add, remove, edit and read user profiles, and let a user manage their own. They have the same endpoints, the same JSON and the same errors, so `smoke.sh` gives identical output for both.

**Storage is not a SQL database.** `UserRepository` is the seam; `InMemoryUserRepository` (a map behind `synchronized`) stands in for the table, with the unique-username and unique-email rule a SQL index would give. A Spring Data JPA or JDBC repository would replace it, and nothing above that interface would change. That swap is not built here.

## Run it

Windows with Git Bash, or any shell with `bash` and `curl`. JDK 21 and Maven.

```bash
cd spring-user-service
mvn -q -DskipTests package
java -jar auth-server/target/auth-server-0.0.1-SNAPSHOT.jar                  # :9000
java -jar user-service-java/target/user-service-java-0.0.1-SNAPSHOT.jar      # :8081
java -jar user-service-kotlin/target/user-service-kotlin-0.0.1-SNAPSHOT.jar  # :8082
```

(or run each `main` from the IDE). Start order does not matter: a service finds the auth server on its first request, and answers 401 until it can.

```bash
TOKEN=$(bash scripts/token.sh admin)                          # alice and bob are role USER, admin is role ADMIN; password: password
curl -s localhost:8081/api/users -H "Authorization: Bearer $TOKEN"
curl -s localhost:8082/api/users/me -H "Authorization: Bearer $(bash scripts/token.sh alice)"
bash scripts/smoke.sh http://localhost:8081                   # then :8082, expect the same PASS lines
mvn -q test                                                   # 34 tests per service, no servers needed
```

## How a request travels

```
client -- Authorization: Bearer <jwt> -->
  SecurityFilterChain  (SecurityConfig)
     1  JwtDecoder: signature, issuer and expiry, checked against the auth server's public keys
     2  JwtAuthenticationConverter: claim scope -> SCOPE_users.read, claim roles -> ROLE_ADMIN
     3  authorizeHttpRequests: GET /api/** needs SCOPE_users.read, every other method SCOPE_users.write
  UserController
     4  @PreAuthorize: the ROLE (admin-only, except /me)
     5  @Valid on the body: bean validation
     6  UserMapper: request DTO -> domain input
  UserService          the rules: not found, nothing to update
  UserRepository       the store: unique username and email
     7  back up: domain User -> UserMapper -> UserResponse -> JSON

any exception, from any step  -->  ApiExceptionHandler  -->  application/problem+json
```

Two kinds of check on purpose: the token's **scope** says what the client app may do for the user (coarse, in the filter chain), the user's **role** says what the user may do (fine, on the controller).

## The API

| Endpoint | Scope | Role | Answer |
|---|---|---|---|
| `POST /api/users` | users.write | ADMIN | 201 + `Location` |
| `GET /api/users?status=&q=` | users.read | ADMIN | 200, list sorted by username |
| `GET /api/users/{id}` | users.read | ADMIN | 200 |
| `PATCH /api/users/{id}` | users.write | ADMIN | 200 |
| `DELETE /api/users/{id}` | users.write | ADMIN | 204 |
| `GET /api/users/me` | users.read | any signed-in user | 200 |
| `PATCH /api/users/me` | users.write | any signed-in user | 200 |
| `GET /actuator/health` | none | none | 200 |

`/me` takes the username from the token's `sub` claim, so a user can only reach their own profile. Every other path is denied.

## Where each layer lives

Both use the same packages (`domain`, `persistence`, `service`, `web`, `web.dto`, `security`, `config`) under `com.example.userservice`. Dependencies point one way: `web` -> `service` -> `persistence`, all of them -> `domain`.

| Layer | Java | Kotlin |
|---|---|---|
| Domain model | `User` (record), `Address`, `UserStatus`, `NewUser`, `UserChanges`: 5 files | `User.kt` (User, Address, UserStatus) and `UserInput.kt` (NewUser, UserChanges): 2 files |
| Domain exceptions | 3 files | `UserExceptions.kt` |
| Repository | `UserRepository` + `InMemoryUserRepository` | the same two, `.kt` |
| Service | `UserService` | `UserService` |
| Controller | `UserController` | `UserController` |
| DTOs and validation | `web/dto/`: 6 files (5 records + `ValidationRules`) | `web/dto/UserDtos.kt`: one file |
| Mapping | `UserMapper`: a class of static methods | `UserMapper.kt`: extension functions |
| Error handling | `ApiExceptionHandler` + `FieldProblem` | `ApiExceptionHandler.kt` (holds `FieldProblem`) |
| Security | `SecurityConfig` | `SecurityConfig` |
| Config | `AppConfig`, `AppProperties`, `DemoDataSeeder` | the same three, `.kt` |
| Null contract | 7 `package-info.java` with `@NullMarked` | nothing: it is in the types |

Size of `src/main`, blank lines not counted, comments counted: Java 33 files and 714 lines (7 of the files are `package-info.java`), Kotlin 15 files and 567 lines.

**Reading order for a review:** `UserController`, `UserMapper`, `UserService`, `UserRepository`, `ApiExceptionHandler`, `SecurityConfig`, then `UserApiTest`.

## Model mapping

Three shapes of a user, converted only at the edges:

```
JSON  --Jackson-->  CreateUserRequest        (web.dto: validation annotations live here)
                    UpdateUserRequest        PATCH by an admin: all fields optional, includes status
                    UpdateProfileRequest     PATCH of /me: no status, so a user cannot re-enable themselves
         --UserMapper-->  NewUser / UserChanges     (domain input; a null field in UserChanges = leave it)
         --UserService--> User                      (domain; immutable; the id, status and timestamps are decided here)
         --UserMapper-->  UserResponse              (web.dto; null fields are left out of the JSON)
```

An entity for a SQL table would be a fourth shape, between `User` and the database, mapped inside the repository adapter.

`UserMapper` is written by hand, a dozen lines per type, with no mapping library. MapStruct is the usual Java tool that generates this kind of code; it is not used here.

## Exceptions: one place, one format

Every error is an RFC 9457 `application/problem+json` document with a stable `type`. `ApiExceptionHandler` extends Spring's `ResponseEntityExceptionHandler`, which already covers Spring MVC's own errors (405, 415, an id that is not a UUID).

| Raised by | HTTP | `type` |
|---|---|---|
| `UserNotFoundException` | 404 | `urn:problem:user-not-found` |
| `DuplicateUserException` | 409 | `urn:problem:duplicate-user` (+ `field`) |
| `EmptyUpdateException` | 400 | `urn:problem:empty-update` |
| bean validation of a body | 400 | `urn:problem:validation-failed` (+ `errors[]`) |
| body not JSON, unknown field, wrong type (`"status": "NOPE"`) | 400 | `urn:problem:malformed-body` (+ `errors[]`) |
| no token, bad token, token without `sub` | 401 | `urn:problem:unauthenticated`, header `WWW-Authenticate: Bearer` |
| a scope or role is missing | 403 | `urn:problem:forbidden` |
| anything else (a bug) | 500 | `urn:problem:internal-error`, the detail goes to the log only |

401 and 403 are raised inside the security filters, before Spring MVC. `SecurityConfig` hands them to the MVC exception resolver, so they reach the same `ApiExceptionHandler` and look like every other error.

A real answer (Java service, `POST /api/users` with `{}`):

```json
{"detail":"3 field(s) are invalid","instance":"/api/users","status":400,"title":"Validation failed",
 "type":"urn:problem:validation-failed",
 "errors":[{"field":"displayName","message":"must not be blank"},
           {"field":"email","message":"must not be blank"},
           {"field":"username","message":"must not be null"}]}
```

## Null handling

| Situation | Java | Kotlin |
|---|---|---|
| A field a user may not have | `@Nullable String phone` in a `@NullMarked` package | `val phone: String?` |
| A lookup that may find nothing | `Optional<User>`, then `orElseThrow` | `User?`, then `?: throw` |
| An optional query parameter | `@RequestParam @Nullable UserStatus status` | `@RequestParam status: UserStatus?` |
| A PATCH field that was not sent | the record component is `null`, so unchanged | the property is `null` (default `= null`), so unchanged |
| A required JSON field that is missing | arrives as `null`, `@NotNull` / `@NotBlank` reports **every** bad field | Jackson's Kotlin module refuses the body at the **first** one; the handler words it the same way |
| An explicit `"displayName": null` in a PATCH | same as not sent (tested) | same as not sent (tested) |
| Fields that are `null` in a response | left out (`default-property-inclusion: non_null`) | left out |

In Java `@Nullable` and `@NullMarked` (JSpecify, the annotations Spring Framework 7 itself uses) are documentation for the reader and for a separate checker such as NullAway; `javac` does not enforce them (a `null` passed to a `@NullMarked` method compiles with `-Xlint:all` and no warning, and runs). In Kotlin the compiler enforces the same thing.

## What is different between the two (each one was run, not assumed)

1. **The Kotlin compiler found a bug the Java compiler did not.** `Jwt.getSubject()` can be null, because `sub` is optional in a JWT. The Java controller compiled without a word; the Kotlin one failed with "actual type is 'String?', but 'String' was expected". Passed on, a null would have reached the repository and ended as a 500. Both controllers now turn a token without `sub` into a 401 (a test per language).
2. **Kotlin needs the `spring` (all-open) compiler plugin.** Kotlin classes are final, and `@PreAuthorize` needs a CGLIB subclass of the controller. With the plugin removed the application does not start: `Could not generate CGLIB subclass of class UserController ... Cannot subclass final class`.
3. **Annotation placement, and a silent failure on older Kotlin.** `@Pattern` on a constructor property must reach the field for bean validation to see it. On Kotlin 2.3.21 (the version Boot 4.1.2 manages) without `-Xannotation-default-target=param-property` it does not, and an invalid user was **created with a 201** instead of a 400. On Kotlin 2.4.21 it lands on the field without the flag. The flag stays in the pom so the module also works if `kotlin.version` is lowered.
4. **Non-null Kotlin types move a check.** `val username: String` has no `@NotNull`, because a missing field never reaches validation. The cost is that only the first missing field is reported: `{}` gives 3 errors from Java and 1 from Kotlin.
5. **Security DSL.** Java chains lambdas (`http.csrf(...).authorizeHttpRequests(...)`); Kotlin writes `http { csrf { disable() } ... }` with `authorize(...)` rules.
6. **Records and data classes.** Java builds the edited `User` by calling the constructor field by field (`withChanges`); Kotlin writes `copy(email = changes.email ?: email, ...)`. The Java `UserChanges` needs `isEmpty()`; the Kotlin one has default values, so `UserChanges(status = DISABLED)` names just what changes.
7. **Mapping.** `UserMapper.toResponse(user)` against `user.toResponse()`.
8. **Tests.** Java uses MockMvc's fluent builders and text blocks; Kotlin uses backtick test names, raw strings and the MockMvc Kotlin DSL (`mvc.get(...) { }.andExpect { status { isOk() } }`). The scenarios and their order are the same: 9 service tests, 24 API tests, and 1 for the 500 path (a mocked service that throws) in each.

## Not in the MVP

- **No real database.** See above. `@Transactional` would go on `UserService` methods once a database is there.
- **A PATCH cannot clear a field.** `null` and "not sent" both mean unchanged, so a phone number or an address cannot be removed, only replaced. Telling the two apart is JSON Merge Patch (RFC 7396), which needs a tri-state type such as `JsonNullable`.
- **Two stores for one person.** The auth server holds the login and the role, the user service holds the profile; both are in memory here. A real system keeps them in one user database or syncs them. Roles are only in the token; the profile has none.
- **No pagination, no optimistic locking (no `version`), no refresh-token handling in the scripts.**
- **The auth server is demo only:** plain-text passwords (`{noop}`), in-memory users and client, no consent screen, no PKCE. Access tokens last 5 minutes.

## What was checked

- `mvn test` from this folder: 34 tests in `user-service-java`, 34 in `user-service-kotlin`, all passing.
- `scripts/smoke.sh` against both running services with real tokens from the running auth server: 34 of 34, and the PASS lines are identical. Run against a dead port it fails (33 of 34), so it can fail.
- A token whose payload was forged and whose signature was kept: 401 from both services.
- A service started while the auth server was down came up healthy, answered 401 meanwhile, and answered 200 on the same instance once the auth server was up.
- The three experiments in "What is different" (2, 3 and the `sub` compile error) were run by changing the build and reading the result, not from memory.
