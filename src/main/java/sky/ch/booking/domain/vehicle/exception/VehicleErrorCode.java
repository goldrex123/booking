package sky.ch.booking.domain.vehicle.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import sky.ch.booking.common.exception.BaseCode;

@RequiredArgsConstructor
@Getter
public enum VehicleErrorCode implements BaseCode {

    NOT_FOUND_VEHICLE(HttpStatus.NOT_FOUND, "V000", "등록된 차량 정보가 없습니다"),
    DUPLICATE_LICENSE_PLATE_VEHICLE(HttpStatus.CONFLICT, "V001", "이미 등록된 번호판 차량이 있습니다"),
    NOT_AVAILABLE_VEHICLE(HttpStatus.CONFLICT, "V002", "현재 사용 불가능한 차량입니다"),
    INVALID_DATE_RANGE(HttpStatus.BAD_REQUEST, "V003", "시작일은 종료일보다 이전이어야 합니다")
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
