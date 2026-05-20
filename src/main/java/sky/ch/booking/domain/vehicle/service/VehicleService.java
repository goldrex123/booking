package sky.ch.booking.domain.vehicle.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sky.ch.booking.domain.vehicle.dto.VehicleResponse;

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
}
