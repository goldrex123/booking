package sky.ch.booking.domain.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import sky.ch.booking.domain.auth.entity.Role;

@Schema(description = "사용자 역할 변경 요청 DTO")
public record UpdateUserRoleRequest(
        @NotNull
        @Schema(description = "변경할 역할", example = "ADMIN", allowableValues = {"ADMIN", "USER"})
        Role role
) {
}
