package sky.ch.booking.domain.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sky.ch.booking.common.ApiResponse;
import sky.ch.booking.common.exception.CommonCode;
import sky.ch.booking.domain.auth.dto.AuthResponse;
import sky.ch.booking.domain.auth.dto.LoginRequest;
import sky.ch.booking.domain.auth.dto.LoginResult;
import sky.ch.booking.domain.auth.dto.SignupRequest;
import sky.ch.booking.domain.auth.service.AuthService;
import sky.ch.booking.security.jwt.JwtProvider;

import java.time.Duration;

@Tag(name = "Auth", description = "인증 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtProvider jwtProvider;

    @Operation(
            summary = "회원가입",
            description = "이메일·비밀번호로 신규 계정을 생성합니다.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "회원가입 성공"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력값 오류 (이메일 형식, 비밀번호 8자 미만, 유효하지 않은 부서)"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 가입된 이메일")
            }
    )
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signup(
            @Valid @RequestBody SignupRequest signupRequest
    ) {
        authService.signup(signupRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(CommonCode.SUCCESS, null));
    }

    @Operation(
            summary = "로그인",
            description = "이메일·비밀번호로 로그인합니다. AccessToken은 응답 바디에, RefreshToken은 HttpOnly 쿠키로 전달됩니다.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그인 성공"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "이메일 또는 비밀번호 불일치")
            }
    )
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest loginRequest
    ) {
        LoginResult result = authService.login(loginRequest);

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", result.refreshToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/api/auth/refresh")
                .maxAge(Duration.ofMillis(jwtProvider.getRefreshTokenExpiry()))
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(ApiResponse.ok(CommonCode.SUCCESS, new AuthResponse(result.accessToken(), result.userInfo())));
    }
}
