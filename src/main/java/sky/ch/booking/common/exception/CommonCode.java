package sky.ch.booking.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommonCode implements BaseCode {

    // 성공
    SUCCESS(HttpStatus.OK, "S000", "성공"),

    // 공통
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "C001", "입력값이 올바르지 않습니다"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C002", "서버 내부 오류가 발생했습니다"),



    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
