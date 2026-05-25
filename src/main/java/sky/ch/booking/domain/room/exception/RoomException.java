package sky.ch.booking.domain.room.exception;

import sky.ch.booking.common.exception.BaseCode;
import sky.ch.booking.common.exception.BusinessException;

public class RoomException extends BusinessException {

    public RoomException(BaseCode errorCode) {
        super(errorCode);
    }
}
