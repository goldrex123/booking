package sky.ch.booking.domain.vehicle.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import sky.ch.booking.common.exception.BaseCode;

@RequiredArgsConstructor
@Getter
public enum VehicleErrorCode implements BaseCode {

    DUPLICATE_LICENSE_PLATE_VEHICLE(HttpStatus.CONFLICT, "V001", "이미 등록된 번호판 차량이 있습니다")
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
