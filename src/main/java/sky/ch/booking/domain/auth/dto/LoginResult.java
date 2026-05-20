package sky.ch.booking.domain.auth.dto;

import java.time.Duration;

public record LoginResult(
        String accessToken,
        String refreshToken,
        Duration refreshTokenExpiry,
        UserInfo userInfo
) {
}
