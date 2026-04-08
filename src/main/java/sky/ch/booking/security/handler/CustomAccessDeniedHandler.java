package sky.ch.booking.security.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import sky.ch.booking.common.ApiResponse;
import sky.ch.booking.domain.auth.exception.AuthErrorCode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * 인가되지 않은 요청(403)에 대한 JSON 응답 핸들러
 * 항상 A009 (ACCESS_DENIED) 반환
 */
@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {

        response.setStatus(AuthErrorCode.ACCESS_DENIED.getHttpStatus().value());
        response.setContentType("application/json;charset=UTF-8");

        ApiResponse<Void> body = ApiResponse.fail(AuthErrorCode.ACCESS_DENIED);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
