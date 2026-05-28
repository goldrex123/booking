package sky.ch.booking.domain.vehicle.dto;

import sky.ch.booking.domain.vehicle.entity.Vehicle;
import sky.ch.booking.domain.vehicle.entity.VehicleStatus;

public record VehicleResponse(
        Long id,
        String model,
        String licensePlate,
        Integer seats,
        VehicleStatus status,
        String note
) {

    public static VehicleResponse from(Vehicle vehicle) {
        return new VehicleResponse(
                vehicle.getId(),
                vehicle.getModel(),
                vehicle.getLicensePlate(),
                vehicle.getSeats(),
                vehicle.getStatus(),
                vehicle.getNote()
        );
    }
}
