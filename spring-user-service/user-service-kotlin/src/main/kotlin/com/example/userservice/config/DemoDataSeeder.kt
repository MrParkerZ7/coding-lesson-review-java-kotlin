package com.example.userservice.config

import com.example.userservice.domain.Address
import com.example.userservice.domain.NewUser
import com.example.userservice.service.UserService
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

/**
 * Stands in for rows that already exist in the database: one profile per login of the auth server,
 * so GET /api/users/me works right after start. Off in tests (app.seed-demo-users=false).
 */
@Component
class DemoDataSeeder(
    private val properties: AppProperties,
    private val users: UserService,
) : ApplicationRunner {

    override fun run(args: ApplicationArguments) {
        if (!properties.seedDemoUsers) return
        users.create(NewUser("alice", "alice@example.com", "Alice Anderson", "+66812345678", Address("1 Main Street", "Bangkok", "10110", "TH")))
        users.create(NewUser("bob", "bob@example.com", "Bob Brown"))
        users.create(NewUser("admin", "admin@example.com", "Admin Root"))
    }
}
