package sky.ch.booking.domain.vehicle.exception;

import sky.ch.booking.common.exception.BaseCode;
import sky.ch.booking.common.exception.BusinessException;

public class VehicleException extends BusinessException {

    public VehicleException(BaseCode errorCode) {
        super(errorCode);
    }
}
