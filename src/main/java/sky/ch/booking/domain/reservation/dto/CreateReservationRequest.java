package sky.ch.booking.domain.reservation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import sky.ch.booking.domain.reservation.entity.ResourceType;

import java.time.LocalDateTime;

public record CreateReservationRequest(
        @NotNull
        ResourceType resourceType,
        @NotNull
        Long resourceId,
        @NotNull
        LocalDateTime startAt,
        @NotNull
        LocalDateTime endAt,
        @NotBlank
        String purpose,
        String destination
) {
}
