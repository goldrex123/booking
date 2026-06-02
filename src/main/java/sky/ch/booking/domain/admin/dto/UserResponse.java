package sky.ch.booking.domain.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import sky.ch.booking.domain.auth.entity.User;

@Schema(description = "사용자 응답 DTO")
public record UserResponse(
        @Schema(description = "사용자 ID", example = "1")
        Long id,
        @Schema(description = "이메일", example = "user@example.com")
        String email,
        @Schema(description = "이름", example = "홍길동")
        String name,
        @Schema(description = "부서", example = "YOUTH", allowableValues = {"YOUTH", "FATHER", "MOTHER"})
        String department,
        @Schema(description = "역할", example = "USER", allowableValues = {"ADMIN", "USER"})
        String role
) {

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getDepartment() != null ? user.getDepartment().name() : null,
                user.getRole() != null ? user.getRole().name() : null
        );
    }
}
