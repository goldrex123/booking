package sky.ch.booking.domain.reservation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import sky.ch.booking.domain.reservation.entity.ResourceType;

import java.time.LocalDateTime;

@Schema(description = "예약 생성 요청")
public record CreateReservationRequest(

        @Schema(description = "자원 유형", example = "VEHICLE", allowableValues = {"VEHICLE", "ROOM"})
        @NotNull
        ResourceType resourceType,

        @Schema(description = "자원 ID (차량 또는 부속실 PK)", example = "1")
        @NotNull
        Long resourceId,

        @Schema(description = "예약 시작일시 (ISO 8601)", example = "2025-06-10T09:00:00")
        @NotNull
        LocalDateTime startAt,

        @Schema(description = "예약 종료일시 (ISO 8601, startAt보다 이후)", example = "2025-06-10T18:00:00")
        @NotNull
        LocalDateTime endAt,

        @Schema(description = "예약 목적", example = "거래처 방문")
        @NotBlank
        String purpose,

        @Schema(description = "목적지 (차량 예약 전용, 부속실 예약 시 null)", example = "서울 강남구", nullable = true)
        String destination
) {
}
