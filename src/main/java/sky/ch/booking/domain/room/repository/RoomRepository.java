package sky.ch.booking.domain.room.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sky.ch.booking.domain.room.entity.Room;

public interface RoomRepository extends JpaRepository<Room, Long> {
}
