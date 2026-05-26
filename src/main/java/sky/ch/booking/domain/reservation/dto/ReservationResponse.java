package sky.ch.booking.domain.reservation.dto;

import sky.ch.booking.domain.auth.entity.User;
import sky.ch.booking.domain.reservation.entity.Reservation;
import sky.ch.booking.domain.reservation.entity.ReservationStatus;
import sky.ch.booking.domain.reservation.entity.ResourceType;

import java.time.Instant;
import java.time.LocalDateTime;

public record ReservationResponse(
    Long id,
    ResourceType resourceType,
    Long resourceId,
    String resourceName,
    Long userId,
    String userName,
    String userDepartment,
    LocalDateTime startAt,
    LocalDateTime endAt,
    String purpose,
    String destination,
    ReservationStatus status,
    Instant createdAt
) {

    public static ReservationResponse from(Reservation reservation, String resourceName, User user) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getResourceType(),
                reservation.getResourceId(),
                resourceName,
                user.getId(),
                user.getName(),
                user.getDepartment().name(),
                reservation.getStartAt(),
                reservation.getEndAt(),
                reservation.getPurpose(),
                reservation.getDestination(),
                reservation.getStatus(),
                reservation.getCreatedAt()
        );
    }

    public static ReservationResponse fromDeleted(Reservation reservation, String resourceName) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getResourceType(),
                reservation.getResourceId(),
                resourceName,
                reservation.getUserId(),
                "알 수 없음",
                "알 수 없음",
                reservation.getStartAt(),
                reservation.getEndAt(),
                reservation.getPurpose(),
                reservation.getDestination(),
                reservation.getStatus(),
                reservation.getCreatedAt()
        );
    }
}
