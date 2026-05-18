package sky.ch.booking.domain.auth.dto;

public record AuthResponse(
        String accessToken,
        UserInfo userInfo
) {
}
