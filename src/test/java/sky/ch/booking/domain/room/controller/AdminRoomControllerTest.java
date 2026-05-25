package sky.ch.booking.domain.room.controller;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import sky.ch.booking.config.SecurityConfig;
import sky.ch.booking.domain.room.dto.CreateRoomRequest;
import sky.ch.booking.domain.room.dto.RoomResponse;
import sky.ch.booking.domain.room.dto.UpdateRoomRequest;
import sky.ch.booking.domain.room.dto.UpdateRoomStatusRequest;
import sky.ch.booking.domain.room.entity.RoomStatus;
import sky.ch.booking.domain.room.exception.RoomErrorCode;
import sky.ch.booking.domain.room.exception.RoomException;
import sky.ch.booking.domain.room.service.RoomService;
import sky.ch.booking.security.handler.CustomAccessDeniedHandler;
import sky.ch.booking.security.handler.CustomAuthenticationEntryPoint;
import sky.ch.booking.security.jwt.JwtProvider;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminRoomController.class)
@Import({SecurityConfig.class, CustomAuthenticationEntryPoint.class, CustomAccessDeniedHandler.class})
@ActiveProfiles("test")
class AdminRoomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RoomService roomService;

    @MockitoBean
    private JwtProvider jwtProvider;

    // ==================== GET /api/admin/rooms ====================

    @Test
    void getAllRooms_ADMIN인증_200반환() throws Exception {
        // given
        List<RoomResponse> rooms = List.of(
                new RoomResponse(1L, "회의실 A", "3층 301호", 8, RoomStatus.ACTIVE, null),
                new RoomResponse(2L, "세미나실", "2층 201호", 20, RoomStatus.INACTIVE, "빔프로젝터")
        );
        given(jwtProvider.validateToken("admin-token")).willReturn(true);
        given(jwtProvider.getUserId("admin-token")).willReturn("1");
        given(jwtProvider.getRole("admin-token")).willReturn("ADMIN");
        given(roomService.getAllRooms()).willReturn(rooms);

        // when / then
        mockMvc.perform(get("/api/admin/rooms")
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].name").value("회의실 A"))
                .andExpect(jsonPath("$.data[0].location").value("3층 301호"))
                .andExpect(jsonPath("$.data[1].name").value("세미나실"));
    }

    @Test
    void getAllRooms_빈목록_200반환() throws Exception {
        // given
        given(jwtProvider.validateToken("admin-token")).willReturn(true);
        given(jwtProvider.getUserId("admin-token")).willReturn("1");
        given(jwtProvider.getRole("admin-token")).willReturn("ADMIN");
        given(roomService.getAllRooms()).willReturn(List.of());

        // when / then
        mockMvc.perform(get("/api/admin/rooms")
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    void getAllRooms_USER권한_403반환() throws Exception {
        // given
        given(jwtProvider.validateToken("user-token")).willReturn(true);
        given(jwtProvider.getUserId("user-token")).willReturn("2");
        given(jwtProvider.getRole("user-token")).willReturn("USER");

        // when / then
        mockMvc.perform(get("/api/admin/rooms")
                        .header("Authorization", "Bearer user-token"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void getAllRooms_인증없음_401반환() throws Exception {
        mockMvc.perform(get("/api/admin/rooms"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ==================== POST /api/admin/rooms ====================

    @Test
    void postRoom_ADMIN인증_201반환() throws Exception {
        // given
        CreateRoomRequest request = new CreateRoomRequest("회의실 A", "3층 301호", 8, "메모");
        RoomResponse response = new RoomResponse(1L, "회의실 A", "3층 301호", 8, RoomStatus.ACTIVE, "메모");
        given(jwtProvider.validateToken("admin-token")).willReturn(true);
        given(jwtProvider.getUserId("admin-token")).willReturn("1");
        given(jwtProvider.getRole("admin-token")).willReturn("ADMIN");
        given(roomService.postRoom(any(CreateRoomRequest.class))).willReturn(response);

        // when / then
        mockMvc.perform(post("/api/admin/rooms")
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("회의실 A"))
                .andExpect(jsonPath("$.data.location").value("3층 301호"))
                .andExpect(jsonPath("$.data.capacity").value(8))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.description").value("메모"));
    }

    @Test
    void postRoom_필수값누락_400반환() throws Exception {
        // given — name 누락
        String body = "{\"location\":\"3층 301호\",\"capacity\":8}";
        given(jwtProvider.validateToken("admin-token")).willReturn(true);
        given(jwtProvider.getUserId("admin-token")).willReturn("1");
        given(jwtProvider.getRole("admin-token")).willReturn("ADMIN");

        // when / then
        mockMvc.perform(post("/api/admin/rooms")
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void postRoom_수용인원0이하_400반환() throws Exception {
        // given — capacity = 0 (Min(1) 위반)
        CreateRoomRequest request = new CreateRoomRequest("회의실 A", "3층 301호", 0, null);
        given(jwtProvider.validateToken("admin-token")).willReturn(true);
        given(jwtProvider.getUserId("admin-token")).willReturn("1");
        given(jwtProvider.getRole("admin-token")).willReturn("ADMIN");

        // when / then
        mockMvc.perform(post("/api/admin/rooms")
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void postRoom_USER권한_403반환() throws Exception {
        // given
        CreateRoomRequest request = new CreateRoomRequest("회의실 A", "3층 301호", 8, null);
        given(jwtProvider.validateToken("user-token")).willReturn(true);
        given(jwtProvider.getUserId("user-token")).willReturn("2");
        given(jwtProvider.getRole("user-token")).willReturn("USER");

        // when / then
        mockMvc.perform(post("/api/admin/rooms")
                        .header("Authorization", "Bearer user-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void postRoom_인증없음_401반환() throws Exception {
        CreateRoomRequest request = new CreateRoomRequest("회의실 A", "3층 301호", 8, null);

        mockMvc.perform(post("/api/admin/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ==================== PUT /api/admin/rooms/{id} ====================

    @Test
    void putRoom_ADMIN인증_200반환() throws Exception {
        // given
        UpdateRoomRequest request = new UpdateRoomRequest("대회의실", "5층 501호", 30, "대형 스크린");
        RoomResponse response = new RoomResponse(1L, "대회의실", "5층 501호", 30, RoomStatus.ACTIVE, "대형 스크린");
        given(jwtProvider.validateToken("admin-token")).willReturn(true);
        given(jwtProvider.getUserId("admin-token")).willReturn("1");
        given(jwtProvider.getRole("admin-token")).willReturn("ADMIN");
        given(roomService.putRoom(eq(1L), any(UpdateRoomRequest.class))).willReturn(response);

        // when / then
        mockMvc.perform(put("/api/admin/rooms/1")
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("대회의실"))
                .andExpect(jsonPath("$.data.capacity").value(30))
                .andExpect(jsonPath("$.data.description").value("대형 스크린"));
    }

    @Test
    void putRoom_존재하지않는부속실_404반환() throws Exception {
        // given
        UpdateRoomRequest request = new UpdateRoomRequest("대회의실", "5층 501호", 30, null);
        given(jwtProvider.validateToken("admin-token")).willReturn(true);
        given(jwtProvider.getUserId("admin-token")).willReturn("1");
        given(jwtProvider.getRole("admin-token")).willReturn("ADMIN");
        willThrow(new RoomException(RoomErrorCode.NOT_FOUND_ROOM))
                .given(roomService).putRoom(eq(999L), any(UpdateRoomRequest.class));

        // when / then
        mockMvc.perform(put("/api/admin/rooms/999")
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void putRoom_필수값누락_400반환() throws Exception {
        // given — name 누락
        String body = "{\"location\":\"5층\",\"capacity\":10}";
        given(jwtProvider.validateToken("admin-token")).willReturn(true);
        given(jwtProvider.getUserId("admin-token")).willReturn("1");
        given(jwtProvider.getRole("admin-token")).willReturn("ADMIN");

        // when / then
        mockMvc.perform(put("/api/admin/rooms/1")
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void putRoom_수용인원0이하_400반환() throws Exception {
        // given — capacity = 0 (Min(1) 위반)
        UpdateRoomRequest request = new UpdateRoomRequest("대회의실", "5층", 0, null);
        given(jwtProvider.validateToken("admin-token")).willReturn(true);
        given(jwtProvider.getUserId("admin-token")).willReturn("1");
        given(jwtProvider.getRole("admin-token")).willReturn("ADMIN");

        // when / then
        mockMvc.perform(put("/api/admin/rooms/1")
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void putRoom_USER권한_403반환() throws Exception {
        // given
        UpdateRoomRequest request = new UpdateRoomRequest("대회의실", "5층", 30, null);
        given(jwtProvider.validateToken("user-token")).willReturn(true);
        given(jwtProvider.getUserId("user-token")).willReturn("2");
        given(jwtProvider.getRole("user-token")).willReturn("USER");

        // when / then
        mockMvc.perform(put("/api/admin/rooms/1")
                        .header("Authorization", "Bearer user-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void putRoom_인증없음_401반환() throws Exception {
        UpdateRoomRequest request = new UpdateRoomRequest("대회의실", "5층", 30, null);

        mockMvc.perform(put("/api/admin/rooms/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ==================== PATCH /api/admin/rooms/{id}/status ====================

    @Test
    void patchRoomStatus_ADMIN인증_200반환() throws Exception {
        // given
        UpdateRoomStatusRequest request = new UpdateRoomStatusRequest(RoomStatus.INACTIVE);
        RoomResponse response = new RoomResponse(1L, "회의실 A", "3층 301호", 8, RoomStatus.INACTIVE, null);
        given(jwtProvider.validateToken("admin-token")).willReturn(true);
        given(jwtProvider.getUserId("admin-token")).willReturn("1");
        given(jwtProvider.getRole("admin-token")).willReturn("ADMIN");
        given(roomService.patchRoomStatus(eq(1L), any(UpdateRoomStatusRequest.class))).willReturn(response);

        // when / then
        mockMvc.perform(patch("/api/admin/rooms/1/status")
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("INACTIVE"));
    }

    @Test
    void patchRoomStatus_존재하지않는부속실_404반환() throws Exception {
        // given
        UpdateRoomStatusRequest request = new UpdateRoomStatusRequest(RoomStatus.INACTIVE);
        given(jwtProvider.validateToken("admin-token")).willReturn(true);
        given(jwtProvider.getUserId("admin-token")).willReturn("1");
        given(jwtProvider.getRole("admin-token")).willReturn("ADMIN");
        willThrow(new RoomException(RoomErrorCode.NOT_FOUND_ROOM))
                .given(roomService).patchRoomStatus(eq(999L), any(UpdateRoomStatusRequest.class));

        // when / then
        mockMvc.perform(patch("/api/admin/rooms/999/status")
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void patchRoomStatus_status누락_400반환() throws Exception {
        // given — status 필드 없음
        given(jwtProvider.validateToken("admin-token")).willReturn(true);
        given(jwtProvider.getUserId("admin-token")).willReturn("1");
        given(jwtProvider.getRole("admin-token")).willReturn("ADMIN");

        // when / then
        mockMvc.perform(patch("/api/admin/rooms/1/status")
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void patchRoomStatus_USER권한_403반환() throws Exception {
        // given
        UpdateRoomStatusRequest request = new UpdateRoomStatusRequest(RoomStatus.INACTIVE);
        given(jwtProvider.validateToken("user-token")).willReturn(true);
        given(jwtProvider.getUserId("user-token")).willReturn("2");
        given(jwtProvider.getRole("user-token")).willReturn("USER");

        // when / then
        mockMvc.perform(patch("/api/admin/rooms/1/status")
                        .header("Authorization", "Bearer user-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void patchRoomStatus_인증없음_401반환() throws Exception {
        UpdateRoomStatusRequest request = new UpdateRoomStatusRequest(RoomStatus.INACTIVE);

        mockMvc.perform(patch("/api/admin/rooms/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }
}
