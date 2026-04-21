package com.swer313.projectstep1.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Autowired
    private UserRepository repository;

    @Test
    void findByEmail_and_existsByEmail_and_findByEmailIgnoreCase() {
        User u = new User();
        u.setFullName("Test User");
        u.setEmail("User@Test.com");
        u.setPasswordHash("hash");
        u.setPhone("123");
        repository.saveAndFlush(u);

        Optional<User> byEmail = repository.findByEmail("user@test.com");
        assertThat(byEmail).isPresent();

        boolean exists = repository.existsByEmail("user@test.com");
        assertThat(exists).isTrue();

        Optional<User> byIgnore = repository.findByEmailIgnoreCase("USER@Test.com");
        assertThat(byIgnore).isPresent();
    }
}