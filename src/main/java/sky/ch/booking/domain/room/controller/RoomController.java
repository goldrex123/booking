package sky.ch.booking.domain.room.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sky.ch.booking.common.ApiResponse;
import sky.ch.booking.common.exception.CommonCode;
import sky.ch.booking.domain.room.dto.RoomResponse;
import sky.ch.booking.domain.room.service.RoomService;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Room", description = "부속실 조회 API (USER+)")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;

    @Operation(
            summary = "가용 부속실 조회",
            description = "ACTIVE 상태이며 주어진 시간대에 CONFIRMED 예약이 없는 부속실 목록을 반환합니다. excludeId를 지정하면 해당 예약의 부속실은 제외 대상에서 빠집니다 (예약 수정 시 활용).",
            security = @SecurityRequirement(name = "JWT")
    )
    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<RoomResponse>>> getAvailableRoom(
            @Parameter(description = "조회 시작 시각 (ISO 8601)", required = true, example = "2025-06-01T09:00:00")
            @RequestParam LocalDateTime startAt,
            @Parameter(description = "조회 종료 시각 (ISO 8601)", required = true, example = "2025-06-01T18:00:00")
            @RequestParam LocalDateTime endAt,
            @Parameter(description = "예약 수정 시 제외할 예약 ID (선택)", example = "3")
            @RequestParam(required = false) Long excludeId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(CommonCode.SUCCESS, roomService.getAvailableRoom(startAt, endAt, excludeId)));
    }
}
