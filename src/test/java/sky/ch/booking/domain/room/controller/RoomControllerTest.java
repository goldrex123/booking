package sky.ch.booking.domain.room.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import sky.ch.booking.config.SecurityConfig;
import sky.ch.booking.domain.room.dto.RoomResponse;
import sky.ch.booking.domain.room.entity.RoomStatus;
import sky.ch.booking.domain.room.exception.RoomErrorCode;
import sky.ch.booking.domain.room.exception.RoomException;
import sky.ch.booking.domain.room.service.RoomService;
import sky.ch.booking.security.handler.CustomAccessDeniedHandler;
import sky.ch.booking.security.handler.CustomAuthenticationEntryPoint;
import sky.ch.booking.security.jwt.JwtProvider;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RoomController.class)
@Import({SecurityConfig.class, CustomAuthenticationEntryPoint.class, CustomAccessDeniedHandler.class})
@ActiveProfiles("test")
class RoomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RoomService roomService;

    @MockitoBean
    private JwtProvider jwtProvider;

    private void givenUserAuth() {
        given(jwtProvider.validateToken("user-token")).willReturn(true);
        given(jwtProvider.getUserId("user-token")).willReturn("1");
        given(jwtProvider.getRole("user-token")).willReturn("USER");
    }

    // ==================== GET /api/rooms/available ====================

    @Test
    void getAvailableRoom_인증된사용자_200반환() throws Exception {
        // given
        List<RoomResponse> rooms = List.of(
                new RoomResponse(1L, "회의실 A", "3층 301호", 8, RoomStatus.ACTIVE, null)
        );
        givenUserAuth();
        given(roomService.getAvailableRoom(any(), any(), isNull())).willReturn(rooms);

        // when / then
        mockMvc.perform(get("/api/rooms/available")
                        .header("Authorization", "Bearer user-token")
                        .param("startAt", "2025-06-01T09:00:00")
                        .param("endAt", "2025-06-01T18:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].name").value("회의실 A"))
                .andExpect(jsonPath("$.data[0].status").value("ACTIVE"));
    }

    @Test
    void getAvailableRoom_excludeId포함_200반환() throws Exception {
        // given
        List<RoomResponse> rooms = List.of(
                new RoomResponse(2L, "세미나실", "2층 201호", 20, RoomStatus.ACTIVE, "빔프로젝터")
        );
        givenUserAuth();
        given(roomService.getAvailableRoom(any(), any(), any())).willReturn(rooms);

        // when / then
        mockMvc.perform(get("/api/rooms/available")
                        .header("Authorization", "Bearer user-token")
                        .param("startAt", "2025-06-01T09:00:00")
                        .param("endAt", "2025-06-01T18:00:00")
                        .param("excludeId", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("세미나실"));
    }

    @Test
    void getAvailableRoom_빈목록_200반환() throws Exception {
        // given
        givenUserAuth();
        given(roomService.getAvailableRoom(any(), any(), isNull())).willReturn(List.of());

        // when / then
        mockMvc.perform(get("/api/rooms/available")
                        .header("Authorization", "Bearer user-token")
                        .param("startAt", "2025-06-01T09:00:00")
                        .param("endAt", "2025-06-01T18:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    void getAvailableRoom_잘못된날짜범위_400반환() throws Exception {
        // given
        givenUserAuth();
        willThrow(new RoomException(RoomErrorCode.INVALID_DATE_RANGE))
                .given(roomService).getAvailableRoom(any(), any(), isNull());

        // when / then
        mockMvc.perform(get("/api/rooms/available")
                        .header("Authorization", "Bearer user-token")
                        .param("startAt", "2025-06-01T18:00:00")
                        .param("endAt", "2025-06-01T09:00:00"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void getAvailableRoom_인증없음_401반환() throws Exception {
        mockMvc.perform(get("/api/rooms/available")
                        .param("startAt", "2025-06-01T09:00:00")
                        .param("endAt", "2025-06-01T18:00:00"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void getAvailableRoom_파라미터누락_400반환() throws Exception {
        // given — endAt 누락
        givenUserAuth();

        // when / then
        mockMvc.perform(get("/api/rooms/available")
                        .header("Authorization", "Bearer user-token")
                        .param("startAt", "2025-06-01T09:00:00"))
                .andExpect(status().isBadRequest());
    }
}
