package sky.ch.booking.domain.auth.dto;

import sky.ch.booking.domain.auth.entity.User;

public record UserInfo(
        Long id,
        String email,
        String name,
        String role
) {
    public static UserInfo from(User user) {
        return new UserInfo(user.getId(), user.getEmail(), user.getName(), user.getRole().name());
    }
}
