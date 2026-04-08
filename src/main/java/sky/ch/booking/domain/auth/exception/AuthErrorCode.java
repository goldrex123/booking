package sky.ch.booking.domain.auth.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import sky.ch.booking.common.exception.BaseCode;

@RequiredArgsConstructor
@Getter
public enum AuthErrorCode implements BaseCode {

    // 인증/인가 (A)
    INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "A001", "유효하지 않거나 만료된 Access Token 입니다"),
    ACCESS_TOKEN_REQUIRED(HttpStatus.UNAUTHORIZED, "A002", "인증이 필요합니다"),

    ACCESS_DENIED(HttpStatus.FORBIDDEN, "A003", "접근 권한이 없습니다")
    ;
    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
