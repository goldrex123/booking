package sky.ch.booking.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import sky.ch.booking.domain.auth.entity.Department;

@Schema(description = "회원가입 요청")
public record SignupRequest(
        @Schema(description = "이름", example = "홍길동")
        @NotBlank
        String name,

        @Schema(description = "이메일", example = "user@example.com")
        @NotBlank
        @Email
        String email,

        @Schema(description = "비밀번호 (8자 이상)", example = "password1!")
        @NotBlank
        @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다")
        String password,

        @Schema(description = "부서 (YOUTH, FATHER, MOTHER)", example = "YOUTH")
        @NotNull
        Department department
) {
}
