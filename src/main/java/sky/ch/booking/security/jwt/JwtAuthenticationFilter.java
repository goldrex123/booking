package sky.ch.booking.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import sky.ch.booking.domain.auth.exception.AuthErrorCode;
import sky.ch.booking.security.userdetails.CustomUserDetails;
import sky.ch.booking.security.userdetails.UserPrincipal;

import java.io.IOException;

@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = extractToken(request);

        if (token != null) {
            if (!jwtProvider.validateToken(token)) {
                request.setAttribute("exception", AuthErrorCode.INVALID_ACCESS_TOKEN);
            } else {
                setAuthentication(token);
            }
        }

        filterChain.doFilter(request, response);
    }


    private void setAuthentication(String token) {
        String userId = jwtProvider.getUserId(token);
        String role = jwtProvider.getRole(token);

        CustomUserDetails userDetails = new CustomUserDetails(UserPrincipal.from(userId, role));

        UsernamePasswordAuthenticationToken authenticationToken
                = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        log.info("JWT 인증 완료 userId - {}, role - {}", userId, role);
    }

    /**
     * 헤더에서 토큰 추출
     */
    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }

        return null;
    }
}
