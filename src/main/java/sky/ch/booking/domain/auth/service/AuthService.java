package sky.ch.booking.domain.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sky.ch.booking.domain.auth.dto.LoginRequest;
import sky.ch.booking.domain.auth.dto.LoginResult;
import sky.ch.booking.domain.auth.dto.SignupRequest;
import sky.ch.booking.domain.auth.dto.UserInfo;
import sky.ch.booking.domain.auth.entity.Role;
import sky.ch.booking.domain.auth.entity.User;
import sky.ch.booking.domain.auth.exception.AuthErrorCode;
import sky.ch.booking.domain.auth.exception.AuthException;
import sky.ch.booking.domain.auth.repository.UserRepository;
import sky.ch.booking.security.jwt.JwtProvider;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;

    @Transactional
    public void signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new AuthException(AuthErrorCode.DUPLICATE_USER_EMAIL);
        }

        String encodedPassword = passwordEncoder.encode(request.password());

        userRepository.save(User.create(
                request.email(),
                encodedPassword,
                request.name(),
                request.department(),
                Role.USER
        ));

        log.info("회원가입 완료 - email: {}", request.email());
    }

    @Transactional
    public LoginResult login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new AuthException(AuthErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new AuthException(AuthErrorCode.INVALID_CREDENTIALS);
        }

        String accessToken = jwtProvider.generateAccessToken(String.valueOf(user.getId()), user.getRole().name());
        String refreshToken = jwtProvider.generateRefreshToken(String.valueOf(user.getId()));

        user.updateRefreshToken(refreshToken);

        log.info("로그인 성공 - userId: {}", user.getId());

        return new LoginResult(accessToken, refreshToken, jwtProvider.getRefreshTokenExpiry(), UserInfo.from(user));
    }

    @Transactional
    public void logout(Long userId) {
        User user = findUserById(userId);
        user.updateRefreshToken(null);

        log.info("로그아웃 - userId: {}", userId);
    }

    @Transactional
    public LoginResult refresh(String refreshToken) {
        if (!jwtProvider.validateToken(refreshToken)) {
            throw new AuthException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        String userId = jwtProvider.getUserId(refreshToken);
        User user = userRepository.findByIdForUpdate(Long.parseLong(userId))
                .orElseThrow(() -> new AuthException(AuthErrorCode.USER_NOT_FOUND));

        if (user.getRefreshToken() == null || !user.getRefreshToken().equals(refreshToken)) {
            throw new AuthException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        String newAccessToken = jwtProvider.generateAccessToken(userId, user.getRole().name());
        String newRefreshToken = jwtProvider.generateRefreshToken(userId);
        user.updateRefreshToken(newRefreshToken);

        log.info("토큰 재발급 - userId: {}", userId);

        return new LoginResult(newAccessToken, newRefreshToken, jwtProvider.getRefreshTokenExpiry(), UserInfo.from(user));
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new AuthException(AuthErrorCode.USER_NOT_FOUND));
    }
}
