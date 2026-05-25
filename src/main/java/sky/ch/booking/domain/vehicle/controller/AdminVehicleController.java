package sky.ch.booking.domain.vehicle.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import sky.ch.booking.common.ApiResponse;
import sky.ch.booking.common.exception.CommonCode;
import sky.ch.booking.domain.vehicle.dto.CreateVehicleRequest;
import sky.ch.booking.domain.vehicle.dto.UpdateVehicleRequest;
import sky.ch.booking.domain.vehicle.dto.VehicleResponse;
import sky.ch.booking.domain.vehicle.service.VehicleService;

import java.util.List;

@Tag(name = "Admin - Vehicle", description = "차량 관리 API (ADMIN 전용)")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/vehicles")
public class AdminVehicleController {

    private final VehicleService vehicleService;

    @Operation(
            summary = "전체 차량 목록 조회",
            description = "등록된 모든 차량을 조회합니다.",
            security = @SecurityRequirement(name = "JWT"),
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음 (ADMIN 전용)")
            }
    )
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<VehicleResponse>>> getAllVehicles() {
        return ResponseEntity.ok(ApiResponse.ok(CommonCode.SUCCESS, vehicleService.getAllVehicles()));
    }


    @Operation(
            summary = "차량 등록",
            description = "새 차량을 등록합니다. 번호판 중복 시 409를 반환합니다.",
            security = @SecurityRequirement(name = "JWT"),
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "등록 성공"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음 (ADMIN 전용)"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "번호판 중복")
            }
    )
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<VehicleResponse>> postVehicle(
            @Valid @RequestBody CreateVehicleRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(CommonCode.SUCCESS, vehicleService.postVehicle(request)));
    }

    @Operation(
            summary = "차량 정보 수정",
            description = "차량의 모델명·좌석수·메모를 수정합니다. 번호판은 변경할 수 없습니다.",
            security = @SecurityRequirement(name = "JWT"),
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음 (ADMIN 전용)"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "차량 없음")
            }
    )
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<VehicleResponse>> putVehicle(
            @PathVariable Long id,
            @Valid @RequestBody UpdateVehicleRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(CommonCode.SUCCESS, vehicleService.putVehicle(id, request)));
    }
}
