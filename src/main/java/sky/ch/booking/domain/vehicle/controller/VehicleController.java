package sky.ch.booking.domain.vehicle.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sky.ch.booking.common.ApiResponse;
import sky.ch.booking.common.exception.CommonCode;
import sky.ch.booking.domain.vehicle.dto.VehicleResponse;
import sky.ch.booking.domain.vehicle.service.VehicleService;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Vehicle", description = "차량 조회 API (USER+)")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;

    @Operation(
            summary = "가용 차량 조회",
            description = "ACTIVE 상태이며 주어진 시간대에 CONFIRMED 예약이 없는 차량 목록을 반환합니다. excludeId를 지정하면 해당 예약의 차량은 제외 대상에서 빠집니다 (예약 수정 시 활용).",
            security = @SecurityRequirement(name = "JWT")
    )
    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<VehicleResponse>>> getAvailableVehicles(
            @Parameter(description = "조회 시작 시각 (ISO 8601)", required = true, example = "2025-06-01T09:00:00")
            @RequestParam LocalDateTime startAt,
            @Parameter(description = "조회 종료 시각 (ISO 8601)", required = true, example = "2025-06-01T18:00:00")
            @RequestParam LocalDateTime endAt,
            @Parameter(description = "예약 수정 시 제외할 예약 ID (선택)", example = "3")
            @RequestParam(required = false) Long excludeId
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(CommonCode.SUCCESS, vehicleService.getAvailableVehicles(startAt, endAt, excludeId))
        );
    }
}
