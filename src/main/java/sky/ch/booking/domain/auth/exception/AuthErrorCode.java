package sky.ch.booking.domain.auth.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import sky.ch.booking.common.exception.BaseCode;

@RequiredArgsConstructor
@Getter
public enum AuthErrorCode implements BaseCode {

    INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "A001", "유효하지 않거나 만료된 Access Token 입니다"),
    ACCESS_TOKEN_REQUIRED(HttpStatus.UNAUTHORIZED, "A002", "인증이 필요합니다"),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "A003", "접근 권한이 없습니다"),
    DUPLICATE_USER_EMAIL(HttpStatus.CONFLICT, "A004", "이미 가입된 이메일입니다"),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "A005", "이메일 또는 비밀번호가 올바르지 않습니다"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "A006", "사용자를 찾을 수 없습니다"),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "A007", "유효하지 않거나 만료된 Refresh Token 입니다."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
