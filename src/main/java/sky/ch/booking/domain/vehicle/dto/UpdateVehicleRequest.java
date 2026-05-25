package sky.ch.booking.domain.vehicle.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateVehicleRequest(
        @NotBlank
        String model,
        @NotNull
        @Min(1)
        Integer seats,
        String note
) {
}
