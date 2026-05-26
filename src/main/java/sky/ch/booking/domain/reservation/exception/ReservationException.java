package sky.ch.booking.domain.reservation.exception;

import sky.ch.booking.common.exception.BaseCode;
import sky.ch.booking.common.exception.BusinessException;

public class ReservationException extends BusinessException {

    public ReservationException(BaseCode errorCode) {
        super(errorCode);
    }
}
