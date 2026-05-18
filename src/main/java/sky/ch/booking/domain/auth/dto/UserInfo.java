package sky.ch.booking.domain.auth.dto;

public record UserInfo(
        Long id,
        String email,
        String name,
        String role
) {
}
