package sky.ch.booking.common.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final BaseCode errorCode;

    public BusinessException(BaseCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
