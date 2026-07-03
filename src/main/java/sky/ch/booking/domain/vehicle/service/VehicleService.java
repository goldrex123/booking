package sky.ch.booking.domain.vehicle.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sky.ch.booking.domain.reservation.entity.ReservationStatus;
import sky.ch.booking.domain.reservation.entity.ResourceType;
import sky.ch.booking.domain.reservation.repository.ReservationRepository;
import sky.ch.booking.domain.vehicle.dto.CreateVehicleRequest;
import sky.ch.booking.domain.vehicle.dto.UpdateVehicleRequest;
import sky.ch.booking.domain.vehicle.dto.UpdateVehicleStatusRequest;
import sky.ch.booking.domain.vehicle.dto.VehicleResponse;
import sky.ch.booking.domain.vehicle.entity.Vehicle;
import sky.ch.booking.domain.vehicle.entity.VehicleStatus;
import sky.ch.booking.domain.vehicle.exception.VehicleErrorCode;
import sky.ch.booking.domain.vehicle.exception.VehicleException;
import sky.ch.booking.domain.vehicle.repository.VehicleRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final ReservationRepository reservationRepository;


    public List<VehicleResponse> getAllVehicles() {
        return vehicleRepository.findAll().stream()
                .map(VehicleResponse::from)
                .toList();
    }

    @Transactional
    public VehicleResponse postVehicle(CreateVehicleRequest request) {
        checkLicensePlate(request.licensePlate());

        Vehicle vehicle = Vehicle.create(
                request.model(),
                request.licensePlate(),
                request.seats(),
                request.note()
        );
        vehicleRepository.save(vehicle);

        log.info("차량 생성 - vehicleId: {}, model: {}", vehicle.getId(), vehicle.getModel());

        return VehicleResponse.from(vehicle);
    }

    @Transactional
    public VehicleResponse putVehicle(Long id, UpdateVehicleRequest request) {
        Vehicle vehicle = findVehicle(id);
        vehicle.update(request.model(), request.seats(), request.note());

        log.info("차량 수정 - vehicleId: {}", id);

        return VehicleResponse.from(vehicle);
    }

    @Transactional
    public VehicleResponse patchVehicleStatus(Long id, UpdateVehicleStatusRequest request) {
        Vehicle vehicle = findVehicle(id);
        vehicle.changeStatus(request.status());

        log.info("차량 상태 변경 - vehicleId: {}, status: {}", id, request.status());

        return VehicleResponse.from(vehicle);
    }

    public List<VehicleResponse> getAvailableVehicles(LocalDateTime startAt, LocalDateTime endAt, Long excludeId) {
        if (!startAt.isBefore(endAt)) {
            throw new VehicleException(VehicleErrorCode.INVALID_DATE_RANGE);
        }

        Set<Long> reservedIds = reservationRepository.findConflictingResourceIds(
                endAt, startAt, ResourceType.VEHICLE, ReservationStatus.CONFIRMED, excludeId
        );

        return vehicleRepository.findByStatus(VehicleStatus.ACTIVE).stream()
                .filter(v -> !reservedIds.contains(v.getId()))
                .map(VehicleResponse::from)
                .toList();
    }

    private Vehicle findVehicle(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new VehicleException(VehicleErrorCode.NOT_FOUND_VEHICLE));
    }

    private void checkLicensePlate(String licensePlate) {
        if (vehicleRepository.existsByLicensePlate(licensePlate)) {
            throw new VehicleException(VehicleErrorCode.DUPLICATE_LICENSE_PLATE_VEHICLE);
        }
    }
}
