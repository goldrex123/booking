package sky.ch.booking.security.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import sky.ch.booking.common.ApiResponse;
import sky.ch.booking.common.exception.BaseCode;
import sky.ch.booking.domain.auth.exception.AuthErrorCode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * 인증되지 않은 요청(401)에 대한 JSON 응답 핸들러
 * - 토큰 없음: A002 (ACCESS_TOKEN_REQUIRED)
 * - 유효하지 않은 토큰: A001 (INVALID_ACCESS_TOKEN)
 */
@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        // JwtAuthenticationFilter에서 설정한 ErrorCode 확인
        BaseCode errorCode = (AuthErrorCode) request.getAttribute("exception");
        if (errorCode == null) {
            errorCode = AuthErrorCode.ACCESS_TOKEN_REQUIRED;
        }

        writeErrorResponse(response, errorCode);
    }

    private void writeErrorResponse(HttpServletResponse response, BaseCode errorCode) throws IOException {
        response.setStatus(errorCode.getHttpStatus().value());
        response.setContentType("application/json;charset=UTF-8");

        ApiResponse<Void> body = ApiResponse.fail(errorCode);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
