package sky.ch.booking.domain.vehicle.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import sky.ch.booking.common.exception.BaseCode;

@RequiredArgsConstructor
@Getter
public enum VehicleErrorCode implements BaseCode {


    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
