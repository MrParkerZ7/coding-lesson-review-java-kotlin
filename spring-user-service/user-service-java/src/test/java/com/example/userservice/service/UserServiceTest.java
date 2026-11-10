package com.example.userservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.example.userservice.domain.Address;
import com.example.userservice.domain.DuplicateUserException;
import com.example.userservice.domain.EmptyUpdateException;
import com.example.userservice.domain.NewUser;
import com.example.userservice.domain.User;
import com.example.userservice.domain.UserChanges;
import com.example.userservice.domain.UserNotFoundException;
import com.example.userservice.domain.UserStatus;
import com.example.userservice.persistence.InMemoryUserRepository;

/** No Spring here: the service is plain Java, so it is built by hand and tested in milliseconds. */
class UserServiceTest {

    private static final Instant CREATED = Instant.parse("2026-01-01T10:00:00Z");
    private static final Instant EDITED = Instant.parse("2026-01-02T10:00:00Z");

    private final InMemoryUserRepository repository = new InMemoryUserRepository();
    private final UserService service = new UserService(repository, Clock.fixed(CREATED, ZoneOffset.UTC));
    private final UserService laterService = new UserService(repository, Clock.fixed(EDITED, ZoneOffset.UTC));

    private static NewUser newUser(String username, String email) {
        return new NewUser(username, email, "Display " + username, null, null);
    }

    @Test
    void createAssignsIdStatusAndTimestamps() {
        User user = service.create(newUser("carol", "carol@example.com"));

        assertThat(user.id()).isNotNull();
        assertThat(user.status()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user.createdAt()).isEqualTo(CREATED);
        assertThat(user.updatedAt()).isEqualTo(CREATED);
        assertThat(user.phone()).isNull();
        assertThat(service.get(user.id())).isEqualTo(user);
    }

    @Test
    void duplicateUsernameOrEmailIsRejectedIgnoringCase() {
        service.create(newUser("carol", "carol@example.com"));

        assertThatThrownBy(() -> service.create(newUser("CAROL", "other@example.com")))
                .isInstanceOf(DuplicateUserException.class)
                .extracting("field").isEqualTo("username");
        assertThatThrownBy(() -> service.create(newUser("dave", "Carol@Example.com")))
                .isInstanceOf(DuplicateUserException.class)
                .extracting("field").isEqualTo("email");
    }

    @Test
    void unknownIdAndUnknownUsernameAreNotFound() {
        assertThatThrownBy(() -> service.get(java.util.UUID.randomUUID())).isInstanceOf(UserNotFoundException.class);
        assertThatThrownBy(() -> service.getByUsername("nobody")).isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void updateChangesOnlyTheGivenFieldsAndBumpsUpdatedAt() {
        User before = service.create(new NewUser("carol", "carol@example.com", "Carol", "+66811111111",
                new Address("1 Main St", "Bangkok", "10110", "TH")));

        User after = laterService.update(before.id(), new UserChanges(null, "Carol C.", null, null, UserStatus.DISABLED));

        assertThat(after.displayName()).isEqualTo("Carol C.");
        assertThat(after.status()).isEqualTo(UserStatus.DISABLED);
        assertThat(after.email()).isEqualTo(before.email());
        assertThat(after.phone()).isEqualTo(before.phone());
        assertThat(after.address()).isEqualTo(before.address());
        assertThat(after.createdAt()).isEqualTo(CREATED);
        assertThat(after.updatedAt()).isEqualTo(EDITED);
    }

    @Test
    void updateWithNoChangesIsRejected() {
        User user = service.create(newUser("carol", "carol@example.com"));

        assertThatThrownBy(() -> service.update(user.id(), new UserChanges(null, null, null, null, null)))
                .isInstanceOf(EmptyUpdateException.class);
    }

    @Test
    void updateToAnEmailAnotherUserHasIsRejected() {
        service.create(newUser("carol", "carol@example.com"));
        User dave = service.create(newUser("dave", "dave@example.com"));

        assertThatThrownBy(() -> service.update(dave.id(), new UserChanges("carol@example.com", null, null, null, null)))
                .isInstanceOf(DuplicateUserException.class);
        assertThat(service.get(dave.id()).email()).isEqualTo("dave@example.com");
    }

    @Test
    void updateKeepingYourOwnEmailIsNotADuplicate() {
        User carol = service.create(newUser("carol", "carol@example.com"));

        User same = service.update(carol.id(), new UserChanges("carol@example.com", null, null, null, null));

        assertThat(same.email()).isEqualTo("carol@example.com");
    }

    @Test
    void deleteRemovesTheUserAndASecondDeleteIsNotFound() {
        User carol = service.create(newUser("carol", "carol@example.com"));

        service.delete(carol.id());

        assertThatThrownBy(() -> service.get(carol.id())).isInstanceOf(UserNotFoundException.class);
        assertThatThrownBy(() -> service.delete(carol.id())).isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void searchFiltersByStatusAndTextAndSortsByUsername() {
        service.create(newUser("zed", "zed@example.com"));
        User amy = service.create(new NewUser("amy", "amy@example.com", "Amy Adams", null, null));
        service.create(newUser("bea", "bea@example.com"));
        service.update(amy.id(), new UserChanges(null, null, null, null, UserStatus.DISABLED));

        assertThat(service.search(null, null)).extracting(User::username).containsExactly("amy", "bea", "zed");
        assertThat(service.search(UserStatus.DISABLED, null)).extracting(User::username).containsExactly("amy");
        assertThat(service.search(null, "  ADAMS ")).extracting(User::username).containsExactly("amy");
        assertThat(service.search(UserStatus.ACTIVE, "amy")).isEqualTo(List.of());
    }
}
