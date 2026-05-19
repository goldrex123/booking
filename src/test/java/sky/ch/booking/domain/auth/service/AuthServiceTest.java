package sky.ch.booking.domain.auth.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import sky.ch.booking.domain.auth.dto.LoginRequest;
import sky.ch.booking.domain.auth.dto.LoginResult;
import sky.ch.booking.domain.auth.dto.SignupRequest;
import sky.ch.booking.domain.auth.entity.Department;
import sky.ch.booking.domain.auth.entity.Role;
import sky.ch.booking.domain.auth.entity.User;
import sky.ch.booking.domain.auth.exception.AuthErrorCode;
import sky.ch.booking.domain.auth.exception.AuthException;
import sky.ch.booking.domain.auth.repository.UserRepository;
import sky.ch.booking.security.jwt.JwtProvider;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtProvider jwtProvider;

    // ==================== signup ====================

    @Test
    void signup_이메일중복_예외발생() {
        SignupRequest request = new SignupRequest("홍길동", "test@test.com", "password1!", Department.YOUTH);
        given(userRepository.existsByEmail("test@test.com")).willReturn(true);

        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(AuthException.class)
                .extracting(e -> ((AuthException) e).getErrorCode())
                .isEqualTo(AuthErrorCode.DUPLICATE_USER_EMAIL);

        then(userRepository).should(never()).save(any());
    }

    @Test
    void signup_성공_비밀번호인코딩및저장확인() {
        SignupRequest request = new SignupRequest("홍길동", "test@test.com", "password1!", Department.YOUTH);
        given(userRepository.existsByEmail("test@test.com")).willReturn(false);
        given(passwordEncoder.encode("password1!")).willReturn("encodedPassword");
        given(userRepository.save(any(User.class))).willAnswer(inv -> inv.getArgument(0));

        authService.signup(request);

        var captor = forClass(User.class);
        then(userRepository).should().save(captor.capture());
        User saved = captor.getValue();

        assertThat(saved.getEmail()).isEqualTo("test@test.com");
        assertThat(saved.getPassword()).isEqualTo("encodedPassword");
        assertThat(saved.getName()).isEqualTo("홍길동");
        assertThat(saved.getDepartment()).isEqualTo(Department.YOUTH);
        assertThat(saved.getRole()).isEqualTo(Role.USER);
    }

    @Test
    void signup_성공_평문비밀번호저장안됨() {
        String rawPassword = "password1!";
        SignupRequest request = new SignupRequest("홍길동", "test@test.com", rawPassword, Department.FATHER);
        given(userRepository.existsByEmail(any())).willReturn(false);
        given(passwordEncoder.encode(rawPassword)).willReturn("$2a$12$hashedpassword");
        given(userRepository.save(any(User.class))).willAnswer(inv -> inv.getArgument(0));

        authService.signup(request);

        var captor = forClass(User.class);
        then(userRepository).should().save(captor.capture());
        assertThat(captor.getValue().getPassword()).isNotEqualTo(rawPassword);
    }

    // ==================== login ====================

    @Test
    void login_성공_LoginResult반환() {
        // given
        User user = User.create("test@test.com", "encodedPw", "홍길동", Department.YOUTH, Role.USER);
        LoginRequest request = new LoginRequest("test@test.com", "password1!");

        given(userRepository.findByEmail("test@test.com")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("password1!", "encodedPw")).willReturn(true);
        given(jwtProvider.generateAccessToken(any(), any())).willReturn("access-token");
        given(jwtProvider.generateRefreshToken(any())).willReturn("refresh-token");

        // when
        LoginResult result = authService.login(request);

        // then
        assertThat(result.accessToken()).isEqualTo("access-token");
        assertThat(result.refreshToken()).isEqualTo("refresh-token");
        assertThat(result.userInfo().email()).isEqualTo("test@test.com");
        assertThat(result.userInfo().name()).isEqualTo("홍길동");
        assertThat(result.userInfo().role()).isEqualTo("USER");
        assertThat(user.getRefreshToken()).isEqualTo("refresh-token");
    }

    @Test
    void login_이메일없음_예외발생() {
        // given
        LoginRequest request = new LoginRequest("notfound@test.com", "password1!");
        given(userRepository.findByEmail("notfound@test.com")).willReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(AuthException.class)
                .extracting(e -> ((AuthException) e).getErrorCode())
                .isEqualTo(AuthErrorCode.INVALID_CREDENTIALS);
    }

    @Test
    void login_비밀번호불일치_예외발생() {
        // given
        User user = User.create("test@test.com", "encodedPw", "홍길동", Department.YOUTH, Role.USER);
        LoginRequest request = new LoginRequest("test@test.com", "wrongPassword!");

        given(userRepository.findByEmail("test@test.com")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("wrongPassword!", "encodedPw")).willReturn(false);

        // when / then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(AuthException.class)
                .extracting(e -> ((AuthException) e).getErrorCode())
                .isEqualTo(AuthErrorCode.INVALID_CREDENTIALS);
    }
}
