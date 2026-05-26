package sky.ch.booking.domain.reservation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sky.ch.booking.common.ApiResponse;
import sky.ch.booking.common.exception.CommonCode;
import sky.ch.booking.domain.reservation.dto.ReservationResponse;
import sky.ch.booking.domain.reservation.entity.ResourceType;
import sky.ch.booking.domain.reservation.service.ReservationService;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Reservation", description = "예약 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    @Operation(
            summary = "전체 예약 조회",
            description = "캘린더용 예약 목록을 조회합니다. startDate~endDate 범위와 겹치는 예약을 반환합니다.",
            security = @SecurityRequirement(name = "JWT"),
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
            }
    )
    @GetMapping
    public ResponseEntity<ApiResponse<List<ReservationResponse>>> getReservations(
            @RequestParam(required = false) ResourceType resourceType,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(CommonCode.SUCCESS, reservationService.getReservations(resourceType, startDate, endDate))
        );
    }
}
