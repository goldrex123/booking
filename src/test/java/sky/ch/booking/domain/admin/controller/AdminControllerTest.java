package sky.ch.booking.domain.admin.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import sky.ch.booking.config.SecurityConfig;
import sky.ch.booking.domain.admin.dto.UserResponse;
import sky.ch.booking.domain.admin.service.AdminService;
import sky.ch.booking.security.handler.CustomAccessDeniedHandler;
import sky.ch.booking.security.handler.CustomAuthenticationEntryPoint;
import sky.ch.booking.security.jwt.JwtProvider;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
@Import({SecurityConfig.class, CustomAuthenticationEntryPoint.class, CustomAccessDeniedHandler.class})
@ActiveProfiles("test")
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminService adminService;

    @MockitoBean
    private JwtProvider jwtProvider;

    // ==================== GET /api/admin/users ====================

    @Test
    void getUsers_ADMIN인증_200반환() throws Exception {
        // given
        List<UserResponse> users = List.of(
                new UserResponse(1L, "admin@example.com", "관리자", "YOUTH", "ADMIN"),
                new UserResponse(2L, "user@example.com", "일반유저", "FATHER", "USER")
        );
        Page<UserResponse> page = new PageImpl<>(users, PageRequest.of(0, 20), users.size());
        given(jwtProvider.validateToken("admin-token")).willReturn(true);
        given(jwtProvider.getUserId("admin-token")).willReturn("1");
        given(jwtProvider.getRole("admin-token")).willReturn("ADMIN");
        given(adminService.getUsers(any())).willReturn(page);

        // when / then
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.content[0].email").value("admin@example.com"))
                .andExpect(jsonPath("$.data.content[0].role").value("ADMIN"))
                .andExpect(jsonPath("$.data.content[1].email").value("user@example.com"));
    }

    @Test
    void getUsers_빈목록_200반환() throws Exception {
        // given
        Page<UserResponse> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        given(jwtProvider.validateToken("admin-token")).willReturn(true);
        given(jwtProvider.getUserId("admin-token")).willReturn("1");
        given(jwtProvider.getRole("admin-token")).willReturn("ADMIN");
        given(adminService.getUsers(any())).willReturn(emptyPage);

        // when / then
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content.length()").value(0))
                .andExpect(jsonPath("$.data.totalElements").value(0));
    }

    @Test
    void getUsers_USER권한_403반환() throws Exception {
        // given
        given(jwtProvider.validateToken("user-token")).willReturn(true);
        given(jwtProvider.getUserId("user-token")).willReturn("2");
        given(jwtProvider.getRole("user-token")).willReturn("USER");

        // when / then
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer user-token"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void getUsers_인증없음_401반환() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }
}
