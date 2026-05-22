package sky.ch.booking.domain.vehicle.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sky.ch.booking.domain.vehicle.dto.CreateVehicleRequest;
import sky.ch.booking.domain.vehicle.dto.VehicleResponse;

import sky.ch.booking.domain.vehicle.entity.Vehicle;
import sky.ch.booking.domain.vehicle.exception.VehicleErrorCode;
import sky.ch.booking.domain.vehicle.exception.VehicleException;
import sky.ch.booking.domain.vehicle.repository.VehicleRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VehicleService {

    private final VehicleRepository vehicleRepository;


    public List<VehicleResponse> getAllVehicles() {
        return vehicleRepository.findAll().stream()
                .map(VehicleResponse::from)
                .toList();
    }

    @Transactional
    public VehicleResponse postVehicle(CreateVehicleRequest request) {
        if (vehicleRepository.existsByLicensePlate(request.licensePlate())) {
            throw new VehicleException(VehicleErrorCode.DUPLICATE_LICENSE_PLATE_VEHICLE);
        }

        Vehicle vehicle = Vehicle.create(
                request.model(),
                request.licensePlate(),
                request.seats(),
                request.note()
        );
        vehicleRepository.save(vehicle);

        return VehicleResponse.from(vehicle);
    }
}
