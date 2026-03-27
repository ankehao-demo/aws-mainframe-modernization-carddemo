package com.carddemo.repository;

import com.carddemo.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldFindUserById() {
        User user = new User("TESTUSER", "John", "Doe", "PASS1234", "U");
        entityManager.persistAndFlush(user);

        Optional<User> found = userRepository.findById("TESTUSER");

        assertThat(found).isPresent();
        assertThat(found.get().getFirstName()).isEqualTo("John");
        assertThat(found.get().getUserType()).isEqualTo("U");
    }

    @Test
    void shouldFindUserByUsrId() {
        User user = new User("TADMIN01", "Admin", "User", "PASSWORD", "A");
        entityManager.persistAndFlush(user);

        Optional<User> found = userRepository.findByUsrId("TADMIN01");

        assertThat(found).isPresent();
        assertThat(found.get().getUserType()).isEqualTo("A");
    }

    @Test
    void shouldReturnEmptyForNonExistentUser() {
        Optional<User> found = userRepository.findById("NOUSER01");
        assertThat(found).isEmpty();
    }

    @Test
    void shouldSaveAndRetrieveUser() {
        User user = new User("TUSER001", "Jane", "Smith", "PASS5678", "U");
        userRepository.save(user);

        Optional<User> found = userRepository.findById("TUSER001");
        assertThat(found).isPresent();
        assertThat(found.get().getLastName()).isEqualTo("Smith");
    }

    @Test
    void shouldUpdateUser() {
        User user = new User("TUSER002", "Bob", "Jones", "OLDPASS1", "U");
        entityManager.persistAndFlush(user);

        user.setPassword("NEWPASS1");
        userRepository.save(user);

        User updated = entityManager.find(User.class, "TUSER002");
        assertThat(updated.getPassword()).isEqualTo("NEWPASS1");
    }

    @Test
    void shouldDeleteUser() {
        User user = new User("DELUSER1", "Del", "User", "PASS1234", "U");
        entityManager.persistAndFlush(user);

        userRepository.deleteById("DELUSER1");

        assertThat(userRepository.findById("DELUSER1")).isEmpty();
    }

    @Test
    void shouldEnforceNotNullConstraints() {
        User user = new User();
        user.setUsrId("BADUSER1");
        // password and user_type are NOT NULL

        assertThatThrownBy(() -> {
            entityManager.persistAndFlush(user);
        }).isInstanceOf(Exception.class);
    }

    @Test
    void shouldFindAllUsers() {
        User user1 = new User("USR00001", "A", "B", "PASS1234", "U");
        User user2 = new User("USR00002", "C", "D", "PASS5678", "A");
        entityManager.persistAndFlush(user1);
        entityManager.persistAndFlush(user2);

        assertThat(userRepository.findAll()).hasSizeGreaterThanOrEqualTo(2);
    }
}
