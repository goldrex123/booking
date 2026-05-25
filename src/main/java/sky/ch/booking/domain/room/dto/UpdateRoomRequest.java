package sky.ch.booking.domain.room.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateRoomRequest(
        @NotBlank
        String name,
        @NotBlank
        String location,
        @NotNull
        @Min(1)
        Integer capacity,
        String description
) {
}
