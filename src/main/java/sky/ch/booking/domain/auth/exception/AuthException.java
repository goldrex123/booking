package sky.ch.booking.domain.auth.exception;

import sky.ch.booking.common.exception.BaseCode;
import sky.ch.booking.common.exception.BusinessException;

public class AuthException extends BusinessException {

    public AuthException(BaseCode errorCode) {
        super(errorCode);
    }
}
