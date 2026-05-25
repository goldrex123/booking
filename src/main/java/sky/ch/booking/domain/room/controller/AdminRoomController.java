package sky.ch.booking.domain.room.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sky.ch.booking.common.ApiResponse;
import sky.ch.booking.common.exception.CommonCode;
import sky.ch.booking.domain.room.dto.CreateRoomRequest;
import sky.ch.booking.domain.room.dto.RoomResponse;
import sky.ch.booking.domain.room.dto.UpdateRoomRequest;
import sky.ch.booking.domain.room.dto.UpdateRoomStatusRequest;
import sky.ch.booking.domain.room.service.RoomService;

import java.util.List;

@Tag(name = "Admin - Room", description = "부속실 관리 API (ADMIN 전용)")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/rooms")
public class AdminRoomController {

    private final RoomService roomService;

    @Operation(
            summary = "전체 부속실 목록 조회",
            description = "등록된 모든 부속실을 조회합니다.",
            security = @SecurityRequirement(name = "JWT"),
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음 (ADMIN 전용)")
            }
    )
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<RoomResponse>>> getAllRooms() {
        return ResponseEntity.ok(ApiResponse.ok(CommonCode.SUCCESS, roomService.getAllRooms()));
    }

    @Operation(
            summary = "부속실 등록",
            description = "새 부속실을 등록합니다.",
            security = @SecurityRequirement(name = "JWT"),
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "등록 성공"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음 (ADMIN 전용)")
            }
    )
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RoomResponse>> postRoom(
            @Valid @RequestBody CreateRoomRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(CommonCode.SUCCESS, roomService.postRoom(request)));
    }

    @Operation(
            summary = "부속실 정보 수정",
            description = "부속실의 이름·위치·수용인원·설명을 수정합니다.",
            security = @SecurityRequirement(name = "JWT"),
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음 (ADMIN 전용)"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "부속실 없음")
            }
    )
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RoomResponse>> putRoom(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRoomRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(CommonCode.SUCCESS, roomService.putRoom(id, request)));
    }

    @Operation(
            summary = "부속실 상태 변경",
            description = "부속실을 활성(ACTIVE) 또는 비활성(INACTIVE) 상태로 변경합니다.",
            security = @SecurityRequirement(name = "JWT"),
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "상태 변경 성공"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음 (ADMIN 전용)"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "부속실 없음")
            }
    )
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RoomResponse>> patchRoomStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRoomStatusRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(CommonCode.SUCCESS, roomService.patchRoomStatus(id, request)));
    }
}
