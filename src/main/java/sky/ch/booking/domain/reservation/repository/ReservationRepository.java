package sky.ch.booking.domain.reservation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sky.ch.booking.domain.reservation.entity.Reservation;
import sky.ch.booking.domain.reservation.entity.ReservationStatus;
import sky.ch.booking.domain.reservation.entity.ResourceType;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    boolean existsByStartAtBeforeAndEndAtAfterAndResourceTypeAndResourceIdAndStatus(
            LocalDateTime endAt, LocalDateTime startAt,
            ResourceType resourceType, Long resourceId, ReservationStatus status
    );

    boolean existsByStartAtBeforeAndEndAtAfterAndResourceTypeAndResourceIdAndStatusAndIdNot(
            LocalDateTime endAt, LocalDateTime startAt,
            ResourceType resourceType, Long resourceId, ReservationStatus status, Long excludeId
    );

    List<Reservation> findByStartAtBeforeAndEndAtAfterOrderByStartAtAsc(
            LocalDateTime endDate, LocalDateTime startDate
    );

    List<Reservation> findByStartAtBeforeAndEndAtAfterAndResourceTypeOrderByStartAtAsc(
            LocalDateTime endDate, LocalDateTime startDate, ResourceType resourceType
    );

    List<Reservation> findByUserIdOrderByCreatedAtDesc(Long userId);

}
