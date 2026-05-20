package sky.ch.booking.domain.auth.controller;

import jakarta.servlet.http.Cookie;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import sky.ch.booking.config.SecurityConfig;
import sky.ch.booking.domain.auth.dto.LoginRequest;
import sky.ch.booking.domain.auth.dto.LoginResult;
import sky.ch.booking.domain.auth.dto.SignupRequest;
import sky.ch.booking.domain.auth.dto.UserInfo;
import sky.ch.booking.domain.auth.entity.Department;
import sky.ch.booking.domain.auth.exception.AuthErrorCode;
import sky.ch.booking.domain.auth.exception.AuthException;
import sky.ch.booking.domain.auth.service.AuthService;
import sky.ch.booking.security.handler.CustomAccessDeniedHandler;
import sky.ch.booking.security.handler.CustomAuthenticationEntryPoint;
import sky.ch.booking.security.jwt.JwtProvider;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, CustomAuthenticationEntryPoint.class, CustomAccessDeniedHandler.class})
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtProvider jwtProvider;

    // ==================== signup ====================

    @Test
    void signup_성공_201반환() throws Exception {
        SignupRequest request = new SignupRequest("홍길동", "test@test.com", "password1!", Department.YOUTH);
        doNothing().when(authService).signup(any(SignupRequest.class));

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void signup_이메일중복_409반환() throws Exception {
        SignupRequest request = new SignupRequest("홍길동", "test@test.com", "password1!", Department.YOUTH);
        doThrow(new AuthException(AuthErrorCode.DUPLICATE_USER_EMAIL)).when(authService).signup(any());

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("A004"));
    }

    @Test
    void signup_빈이름_400반환() throws Exception {
        SignupRequest request = new SignupRequest("", "test@test.com", "password1!", Department.YOUTH);

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void signup_잘못된이메일형식_400반환() throws Exception {
        SignupRequest request = new SignupRequest("홍길동", "invalid-email", "password1!", Department.YOUTH);

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void signup_짧은비밀번호_400반환() throws Exception {
        SignupRequest request = new SignupRequest("홍길동", "test@test.com", "short", Department.YOUTH);

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void signup_유효하지않은부서_400반환() throws Exception {
        String invalidRequest = """
                {"name": "홍길동", "email": "test@test.com", "password": "password1!", "department": "INVALID_DEPT"}
                """;

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void signup_null부서_400반환() throws Exception {
        String requestWithNullDept = """
                {"name": "홍길동", "email": "test@test.com", "password": "password1!", "department": null}
                """;

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestWithNullDept))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ==================== login ====================

    @Test
    void login_성공_200반환및쿠키설정() throws Exception {
        // given
        LoginRequest request = new LoginRequest("test@test.com", "password1!");
        UserInfo userInfo = new UserInfo(1L, "test@test.com", "홍길동", "USER");
        LoginResult loginResult = new LoginResult("access-token", "refresh-token", Duration.ofMillis(2_592_000_000L), userInfo);
        given(authService.login(any(LoginRequest.class))).willReturn(loginResult);

        // when / then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.userInfo.email").value("test@test.com"))
                .andExpect(cookie().httpOnly("refreshToken", true))
                .andExpect(cookie().path("refreshToken", "/api/auth/refresh"))
                .andExpect(cookie().secure("refreshToken", true));
    }

    @Test
    void login_잘못된자격증명_401반환() throws Exception {
        // given
        LoginRequest request = new LoginRequest("test@test.com", "wrongPassword!");
        given(authService.login(any(LoginRequest.class)))
                .willThrow(new AuthException(AuthErrorCode.INVALID_CREDENTIALS));

        // when / then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("A005"));
    }

    @Test
    void login_빈비밀번호_400반환() throws Exception {
        LoginRequest request = new LoginRequest("test@test.com", "");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void login_빈이메일_400반환() throws Exception {
        LoginRequest request = new LoginRequest("", "password1!");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ==================== logout ====================

    @Test
    void logout_성공_204반환및쿠키만료() throws Exception {
        // given
        given(jwtProvider.validateToken("valid-token")).willReturn(true);
        given(jwtProvider.getUserId("valid-token")).willReturn("1");
        given(jwtProvider.getRole("valid-token")).willReturn("USER");
        doNothing().when(authService).logout(1L);

        // when / then
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isNoContent())
                .andExpect(cookie().httpOnly("refreshToken", true))
                .andExpect(cookie().maxAge("refreshToken", 0));
    }

    @Test
    void logout_인증없음_401반환() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("A002"));
    }

    // ==================== refresh ====================

    @Test
    void refresh_성공_200반환및새쿠키설정() throws Exception {
        // given
        UserInfo userInfo = new UserInfo(1L, "test@test.com", "홍길동", "USER");
        LoginResult result = new LoginResult("new-access-token", "new-refresh-token", Duration.ofMillis(2_592_000_000L), userInfo);
        given(authService.refresh("old-refresh-token")).willReturn(result);

        // when / then
        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new Cookie("refreshToken", "old-refresh-token")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.data.userInfo.email").value("test@test.com"))
                .andExpect(cookie().value("refreshToken", "new-refresh-token"))
                .andExpect(cookie().httpOnly("refreshToken", true));
    }

    @Test
    void refresh_쿠키없음_400반환() throws Exception {
        mockMvc.perform(post("/api/auth/refresh"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void refresh_유효하지않은토큰_401반환() throws Exception {
        // given
        given(authService.refresh("invalid-token"))
                .willThrow(new AuthException(AuthErrorCode.INVALID_REFRESH_TOKEN));

        // when / then
        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new Cookie("refreshToken", "invalid-token")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("A007"));
    }
}
