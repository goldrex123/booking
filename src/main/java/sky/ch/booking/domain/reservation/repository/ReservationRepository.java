package sky.ch.booking.domain.reservation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sky.ch.booking.domain.reservation.entity.Reservation;
import sky.ch.booking.domain.reservation.entity.ReservationStatus;
import sky.ch.booking.domain.reservation.entity.ResourceType;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("SELECT COUNT(r) > 0 FROM Reservation r " +
            "WHERE r.startAt < :endAt AND r.endAt > :startAt " +
            "AND r.resourceType = :resourceType AND r.resourceId = :resourceId " +
            "AND r.status = :status")
    boolean existsConflict(
            @Param("endAt") LocalDateTime endAt,
            @Param("startAt") LocalDateTime startAt,
            @Param("resourceType") ResourceType resourceType,
            @Param("resourceId") Long resourceId,
            @Param("status") ReservationStatus status
    );

    @Query("SELECT COUNT(r) > 0 FROM Reservation r " +
            "WHERE r.startAt < :endAt AND r.endAt > :startAt " +
            "AND r.resourceType = :resourceType AND r.resourceId = :resourceId " +
            "AND r.status = :status AND r.id <> :excludeId")
    boolean existsConflictExcluding(
            @Param("endAt") LocalDateTime endAt,
            @Param("startAt") LocalDateTime startAt,
            @Param("resourceType") ResourceType resourceType,
            @Param("resourceId") Long resourceId,
            @Param("status") ReservationStatus status,
            @Param("excludeId") Long excludeId
    );

    @Query("SELECT r FROM Reservation r " +
            "WHERE r.startAt < :endDate AND r.endAt > :startDate " +
            "AND r.status = 'CONFIRMED' " +
            "ORDER BY r.startAt ASC")
    List<Reservation> findConfirmedInRange(
            @Param("endDate") LocalDateTime endDate,
            @Param("startDate") LocalDateTime startDate
    );

    @Query("SELECT r FROM Reservation r " +
            "WHERE r.startAt < :endDate AND r.endAt > :startDate " +
            "AND r.resourceType = :resourceType " +
            "AND r.status = 'CONFIRMED' " +
            "ORDER BY r.startAt ASC")
    List<Reservation> findConfirmedInRangeByType(
            @Param("endDate") LocalDateTime endDate,
            @Param("startDate") LocalDateTime startDate,
            @Param("resourceType") ResourceType resourceType
    );

    List<Reservation> findByUserIdOrderByCreatedAtDesc(Long userId);

}
