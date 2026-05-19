package sky.ch.booking.domain.auth.dto;

public record LoginResult(
        String accessToken,
        String refreshToken,
        UserInfo userInfo
) {
}
