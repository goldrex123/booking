package sky.ch.booking.domain.reservation.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import sky.ch.booking.common.exception.BaseCode;

@RequiredArgsConstructor
@Getter
public enum ReservationErrorCode implements BaseCode {

    NOT_FOUND(HttpStatus.NOT_FOUND, "RV000", "예약 정보가 없습니다"),
    CONFLICT(HttpStatus.CONFLICT, "RV001", "해당 시간에 이미 예약이 존재합니다"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "RV002", "본인 또는 관리자만 접근할 수 있습니다"),
    NOT_MODIFIABLE(HttpStatus.BAD_REQUEST, "RV003", "확정 상태이며 시작 전인 예약만 수정할 수 있습니다"),
    INVALID_DATE_RANGE(HttpStatus.BAD_REQUEST, "RV004", "시작일은 종료일보다 이전이어야 합니다"),
    DESTINATION_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "RV005", "부속실 예약에는 목적지를 입력할 수 없습니다"),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
