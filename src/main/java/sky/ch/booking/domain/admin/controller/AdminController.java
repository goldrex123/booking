package sky.ch.booking.domain.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sky.ch.booking.common.ApiResponse;
import sky.ch.booking.common.exception.CommonCode;
import sky.ch.booking.domain.admin.dto.UpdateUserRoleRequest;
import sky.ch.booking.domain.admin.dto.UserResponse;
import sky.ch.booking.domain.admin.service.AdminService;

@Tag(name = "Admin - User", description = "사용자 관리 API (ADMIN 전용)")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    @Operation(
            summary = "전체 사용자 목록 조회",
            description = "등록된 모든 사용자를 페이지 단위로 조회합니다.",
            security = @SecurityRequirement(name = "JWT"),
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음 (ADMIN 전용)")
            }
    )
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getUsers(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(CommonCode.SUCCESS, adminService.getUsers(pageable))
        );
    }

    @Operation(
            summary = "사용자 역할 변경",
            description = "사용자의 역할을 ADMIN 또는 USER로 변경합니다.",
            security = @SecurityRequirement(name = "JWT"),
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "역할 변경 성공"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음 (ADMIN 전용)"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자 없음")
            }
    )
    @PatchMapping("/users/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> patchRole(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRoleRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(CommonCode.SUCCESS, adminService.patchRole(id, request))
        );
    }

}
