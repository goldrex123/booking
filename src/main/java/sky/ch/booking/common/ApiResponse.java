package sky.ch.booking.common;

import sky.ch.booking.common.exception.BaseCode;
import sky.ch.booking.common.exception.CommonCode;

/**
 * 공통 API 응답 래퍼
 */
public record ApiResponse<T>(
        boolean success,
        String code,
        T data,
        String message
) {

    public static <T> ApiResponse<T> ok(CommonCode code, T data) {
        return new ApiResponse<>(true, code.getCode(), data, code.getMessage());
    }

    public static ApiResponse<Void> fail(BaseCode errorCode) {
        return new ApiResponse<>(false, errorCode.getCode(), null, errorCode.getMessage());
    }

    public static ApiResponse<Void> fail(BaseCode errorCode, String message) {
        return new ApiResponse<>(false, errorCode.getCode(), null, message);
    }
}
