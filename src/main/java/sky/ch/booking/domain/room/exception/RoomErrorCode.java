package sky.ch.booking.domain.room.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import sky.ch.booking.common.exception.BaseCode;

@RequiredArgsConstructor
@Getter
public enum RoomErrorCode implements BaseCode {

    NOT_FOUND_ROOM(HttpStatus.NOT_FOUND, "RM000", "등록된 부속실 정보가 없습니다"),
    NOT_AVAILABLE_ROOM(HttpStatus.CONFLICT, "RM001", "현재 사용 불가능한 부속실입니다")
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
