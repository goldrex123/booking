package sky.ch.booking.domain.vehicle.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sky.ch.booking.domain.vehicle.entity.Vehicle;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    boolean existsByLicensePlate(String licensePlate);
}
