package sky.ch.booking.domain.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sky.ch.booking.domain.auth.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);
}
