package sky.ch.booking.domain.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sky.ch.booking.domain.auth.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);
}
