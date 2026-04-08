package sky.ch.booking.security.userdetails;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {

    UserPrincipal userPrincipal;

    public CustomUserDetails(UserPrincipal userPrincipal) {
        this.userPrincipal = userPrincipal;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_"+userPrincipal.role()));
    }

    @Override
    public @Nullable String getPassword() {
        return userPrincipal.password();
    }

    @Override
    public String getUsername() {
        return userPrincipal.userId();
    }


}
