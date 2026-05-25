package sky.ch.booking.domain.vehicle.dto;

import jakarta.validation.constraints.NotNull;
import sky.ch.booking.domain.vehicle.entity.VehicleStatus;

public record UpdateVehicleStatusRequest(
        @NotNull
        VehicleStatus status
) {
}
