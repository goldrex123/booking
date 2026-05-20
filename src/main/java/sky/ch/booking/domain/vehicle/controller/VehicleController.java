package sky.ch.booking.domain.vehicle.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sky.ch.booking.domain.vehicle.service.VehicleService;

@Tag(name = "Vehicle", description = "차량 조회 API (USER+)")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;
}
