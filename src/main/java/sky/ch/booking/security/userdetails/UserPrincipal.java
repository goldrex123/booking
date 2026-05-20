package sky.ch.booking.security.userdetails;

public record UserPrincipal(
        String userId,
        String role
) {

    public static UserPrincipal from(String userId, String role) {
        return new UserPrincipal(userId, role);
    }
}
