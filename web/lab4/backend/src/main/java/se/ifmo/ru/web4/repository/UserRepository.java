package se.ifmo.ru.web4.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.ifmo.ru.web4.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}