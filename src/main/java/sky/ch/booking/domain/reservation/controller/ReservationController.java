package sky.ch.booking.domain.reservation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import sky.ch.booking.common.ApiResponse;
import sky.ch.booking.common.exception.CommonCode;
import sky.ch.booking.domain.reservation.dto.CreateReservationRequest;
import sky.ch.booking.domain.reservation.dto.ReservationResponse;
import sky.ch.booking.domain.reservation.entity.ResourceType;
import sky.ch.booking.domain.reservation.service.ReservationService;
import sky.ch.booking.security.userdetails.CustomUserDetails;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Reservation", description = "예약 API — 차량·부속실 예약 생성 및 조회")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    @Operation(
            summary = "전체 예약 조회 (캘린더용)",
            description = """
                    startDate ~ endDate 범위와 **겹치는** 모든 예약을 조회합니다.

                    - 겹침 조건: `예약.startAt < endDate AND 예약.endAt > startDate`
                    - `resourceType` 미전달 시 차량·부속실 전체 조회
                    - 결과는 `startAt` 오름차순 정렬
                    """,
            security = @SecurityRequirement(name = "JWT"),
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "startDate가 endDate 이후인 경우",
                            content = @Content(schema = @Schema(example = "{\"success\":false,\"data\":null,\"message\":\"시작일은 종료일보다 이전이어야 합니다\"}"))),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요",
                            content = @Content(schema = @Schema(example = "{\"success\":false,\"data\":null,\"message\":\"인증이 필요합니다\"}")))
            }
    )
    @GetMapping
    public ResponseEntity<ApiResponse<List<ReservationResponse>>> getReservations(
            @Parameter(description = "자원 유형 필터 (생략 시 전체 조회)", example = "VEHICLE")
            @RequestParam(required = false) ResourceType resourceType,

            @Parameter(description = "조회 시작일시 (ISO 8601)", required = true, example = "2025-06-01T00:00:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,

            @Parameter(description = "조회 종료일시 (ISO 8601, startDate보다 이후)", required = true, example = "2025-06-30T23:59:59")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(CommonCode.SUCCESS, reservationService.getReservations(resourceType, startDate, endDate))
        );
    }

    @Operation(
            summary = "예약 생성",
            description = """
                    차량 또는 부속실 예약을 생성합니다.

                    - 동일 자원·동일 시간에 `CONFIRMED` 상태 예약이 있으면 `409` 반환
                    - `destination`은 차량 예약 전용 (부속실 예약 시 반드시 null)
                    - INACTIVE 상태 자원은 예약 불가 (`409` 반환)
                    - 동시 예약 처리: 비관적 락(`SELECT FOR UPDATE`)으로 직렬화
                    """,
            security = @SecurityRequirement(name = "JWT"),
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "예약 생성 성공"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력값 오류, 시작일이 종료일 이후, 또는 부속실 예약에 destination 입력",
                            content = @Content(schema = @Schema(example = "{\"success\":false,\"data\":null,\"message\":\"입력값이 올바르지 않습니다\"}"))),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요",
                            content = @Content(schema = @Schema(example = "{\"success\":false,\"data\":null,\"message\":\"인증이 필요합니다\"}"))),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "자원(차량·부속실) 또는 사용자 없음",
                            content = @Content(schema = @Schema(example = "{\"success\":false,\"data\":null,\"message\":\"등록된 차량 정보가 없습니다\"}"))),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "시간 충돌 또는 비활성 자원",
                            content = @Content(schema = @Schema(example = "{\"success\":false,\"data\":null,\"message\":\"해당 시간에 이미 예약이 존재합니다\"}")))
            }
    )
    @PostMapping
    public ResponseEntity<ApiResponse<ReservationResponse>> postReservation(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @Valid @RequestBody CreateReservationRequest request
    ) {
        Long userId = getUserId(customUserDetails);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(CommonCode.SUCCESS, reservationService.postReservation(request, userId)));
    }

    @Operation(
            summary = "내 예약 목록 조회",
            description = "로그인한 사용자의 예약 목록을 최신순(createdAt 내림차순)으로 반환합니다.",
            security = @SecurityRequirement(name = "JWT"),
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요",
                            content = @Content(schema = @Schema(example = "{\"success\":false,\"data\":null,\"message\":\"인증이 필요합니다\"}")))
            }
    )
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<ReservationResponse>>> getMyReservations(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        Long userId = getUserId(customUserDetails);
        return ResponseEntity.ok(ApiResponse.ok(
                CommonCode.SUCCESS, reservationService.getMyReservations(userId)
        ));
    }

    private long getUserId(CustomUserDetails customUserDetails) {
        return Long.parseLong(customUserDetails.getUsername());
    }
}
