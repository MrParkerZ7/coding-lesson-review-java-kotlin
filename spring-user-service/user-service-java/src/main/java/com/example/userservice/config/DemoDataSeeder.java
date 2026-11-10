package com.example.userservice.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.example.userservice.domain.Address;
import com.example.userservice.domain.NewUser;
import com.example.userservice.service.UserService;

/**
 * Stands in for rows that already exist in the database: one profile per login of the auth server,
 * so GET /api/users/me works right after start. Off in tests (app.seed-demo-users=false).
 */
@Component
class DemoDataSeeder implements ApplicationRunner {

    private final AppProperties properties;
    private final UserService users;

    DemoDataSeeder(AppProperties properties, UserService users) {
        this.properties = properties;
        this.users = users;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!properties.seedDemoUsers()) {
            return;
        }
        users.create(new NewUser("alice", "alice@example.com", "Alice Anderson", "+66812345678",
                new Address("1 Main Street", "Bangkok", "10110", "TH")));
        users.create(new NewUser("bob", "bob@example.com", "Bob Brown", null, null));
        users.create(new NewUser("admin", "admin@example.com", "Admin Root", null, null));
    }
}
