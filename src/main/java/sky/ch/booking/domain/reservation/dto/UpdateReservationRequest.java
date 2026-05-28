package sky.ch.booking.domain.reservation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record UpdateReservationRequest(
        @Schema(description = "변경할 시작일시", requiredMode = Schema.RequiredMode.REQUIRED, example = "2025-06-10T09:00:00")
        @NotNull LocalDateTime startAt,

        @Schema(description = "변경할 종료일시 (startAt 이후)", requiredMode = Schema.RequiredMode.REQUIRED, example = "2025-06-10T18:00:00")
        @NotNull LocalDateTime endAt,

        @Schema(description = "예약 목적", requiredMode = Schema.RequiredMode.REQUIRED, example = "거래처 방문")
        @NotBlank String purpose,

        @Schema(description = "목적지 (차량 전용, 부속실은 null)", nullable = true, example = "서울 강남구")
        String destination
) {
}
