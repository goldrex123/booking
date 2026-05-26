package sky.ch.booking.domain.reservation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import sky.ch.booking.domain.auth.entity.User;
import sky.ch.booking.domain.reservation.entity.Reservation;
import sky.ch.booking.domain.reservation.entity.ReservationStatus;
import sky.ch.booking.domain.reservation.entity.ResourceType;

import java.time.Instant;
import java.time.LocalDateTime;

@Schema(description = "예약 응답")
public record ReservationResponse(

        @Schema(description = "예약 ID", example = "1")
        Long id,

        @Schema(description = "자원 유형", example = "VEHICLE")
        ResourceType resourceType,

        @Schema(description = "자원 ID", example = "3")
        Long resourceId,

        @Schema(description = "자원명 (차량: 차종, 부속실: 이름)", example = "소나타")
        String resourceName,

        @Schema(description = "예약자 ID", example = "7")
        Long userId,

        @Schema(description = "예약자 이름", example = "홍길동")
        String userName,

        @Schema(description = "예약자 소속 부서", example = "YOUTH")
        String userDepartment,

        @Schema(description = "예약 시작일시", example = "2025-06-10T09:00:00")
        LocalDateTime startAt,

        @Schema(description = "예약 종료일시", example = "2025-06-10T18:00:00")
        LocalDateTime endAt,

        @Schema(description = "예약 목적", example = "거래처 방문")
        String purpose,

        @Schema(description = "목적지 (차량 전용, 부속실은 null)", example = "서울 강남구", nullable = true)
        String destination,

        @Schema(description = "예약 상태", example = "CONFIRMED", allowableValues = {"CONFIRMED", "CANCELLED"})
        ReservationStatus status,

        @Schema(description = "생성일시 (UTC Epoch)")
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
