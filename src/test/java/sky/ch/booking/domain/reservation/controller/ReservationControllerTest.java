package sky.ch.booking.domain.reservation.controller;

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
import sky.ch.booking.domain.reservation.dto.CreateReservationRequest;
import sky.ch.booking.domain.reservation.dto.ReservationResponse;
import sky.ch.booking.domain.reservation.entity.ReservationStatus;
import sky.ch.booking.domain.reservation.entity.ResourceType;
import sky.ch.booking.domain.reservation.exception.ReservationErrorCode;
import sky.ch.booking.domain.reservation.exception.ReservationException;
import sky.ch.booking.domain.reservation.service.ReservationService;
import sky.ch.booking.security.handler.CustomAccessDeniedHandler;
import sky.ch.booking.security.handler.CustomAuthenticationEntryPoint;
import sky.ch.booking.security.jwt.JwtProvider;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import sky.ch.booking.domain.reservation.dto.UpdateReservationRequest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReservationController.class)
@Import({SecurityConfig.class, CustomAuthenticationEntryPoint.class, CustomAccessDeniedHandler.class})
@ActiveProfiles("test")
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReservationService reservationService;

    @MockitoBean
    private JwtProvider jwtProvider;

    private static final LocalDateTime START = LocalDateTime.of(2025, 6, 1, 9, 0);
    private static final LocalDateTime END = LocalDateTime.of(2025, 6, 1, 18, 0);

    private void givenUserAuth() {
        given(jwtProvider.validateToken("user-token")).willReturn(true);
        given(jwtProvider.getUserId("user-token")).willReturn("1");
        given(jwtProvider.getRole("user-token")).willReturn("USER");
    }

    // ==================== GET /api/reservations ====================

    @Test
    void getReservations_인증된사용자_200반환() throws Exception {
        // given
        ReservationResponse response = new ReservationResponse(
                1L, ResourceType.VEHICLE, 1L, "소나타",
                1L, "홍길동", "YOUTH",
                START, END, "출장", "서울",
                ReservationStatus.CONFIRMED, Instant.now()
        );
        givenUserAuth();
        given(reservationService.getReservations(null, START, END)).willReturn(List.of(response));

        // when / then
        mockMvc.perform(get("/api/reservations")
                        .header("Authorization", "Bearer user-token")
                        .param("startDate", "2025-06-01T09:00:00")
                        .param("endDate", "2025-06-01T18:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].resourceName").value("소나타"))
                .andExpect(jsonPath("$.data[0].userName").value("홍길동"))
                .andExpect(jsonPath("$.data[0].status").value("CONFIRMED"));
    }

    @Test
    void getReservations_resourceType필터_200반환() throws Exception {
        // given
        ReservationResponse response = new ReservationResponse(
                1L, ResourceType.VEHICLE, 1L, "그랜저",
                1L, "홍길동", "YOUTH",
                START, END, "외근", "부산",
                ReservationStatus.CONFIRMED, Instant.now()
        );
        givenUserAuth();
        given(reservationService.getReservations(eq(ResourceType.VEHICLE), any(), any())).willReturn(List.of(response));

        // when / then
        mockMvc.perform(get("/api/reservations")
                        .header("Authorization", "Bearer user-token")
                        .param("resourceType", "VEHICLE")
                        .param("startDate", "2025-06-01T09:00:00")
                        .param("endDate", "2025-06-01T18:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].resourceType").value("VEHICLE"));
    }

    @Test
    void getReservations_빈목록_200반환() throws Exception {
        // given
        givenUserAuth();
        given(reservationService.getReservations(any(), any(), any())).willReturn(List.of());

        // when / then
        mockMvc.perform(get("/api/reservations")
                        .header("Authorization", "Bearer user-token")
                        .param("startDate", "2025-06-01T09:00:00")
                        .param("endDate", "2025-06-01T18:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    void getReservations_잘못된날짜범위_400반환() throws Exception {
        // given
        givenUserAuth();
        willThrow(new ReservationException(ReservationErrorCode.INVALID_DATE_RANGE))
                .given(reservationService).getReservations(any(), any(), any());

        // when / then
        mockMvc.perform(get("/api/reservations")
                        .header("Authorization", "Bearer user-token")
                        .param("startDate", "2025-06-01T18:00:00")
                        .param("endDate", "2025-06-01T09:00:00"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void getReservations_인증없음_401반환() throws Exception {
        mockMvc.perform(get("/api/reservations")
                        .param("startDate", "2025-06-01T09:00:00")
                        .param("endDate", "2025-06-01T18:00:00"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ==================== POST /api/reservations ====================

    @Test
    void postReservation_정상요청_201반환() throws Exception {
        // given
        CreateReservationRequest request = new CreateReservationRequest(
                ResourceType.VEHICLE, 1L, START, END, "출장", "서울"
        );
        ReservationResponse response = new ReservationResponse(
                1L, ResourceType.VEHICLE, 1L, "소나타",
                1L, "홍길동", "YOUTH",
                START, END, "출장", "서울",
                ReservationStatus.CONFIRMED, Instant.now()
        );
        givenUserAuth();
        given(reservationService.postReservation(any(CreateReservationRequest.class), eq(1L))).willReturn(response);

        // when / then
        mockMvc.perform(post("/api/reservations")
                        .header("Authorization", "Bearer user-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.resourceName").value("소나타"))
                .andExpect(jsonPath("$.data.status").value("CONFIRMED"));
    }

    @Test
    void postReservation_시간충돌_409반환() throws Exception {
        // given
        CreateReservationRequest request = new CreateReservationRequest(
                ResourceType.VEHICLE, 1L, START, END, "출장", "서울"
        );
        givenUserAuth();
        willThrow(new ReservationException(ReservationErrorCode.CONFLICT))
                .given(reservationService).postReservation(any(CreateReservationRequest.class), eq(1L));

        // when / then
        mockMvc.perform(post("/api/reservations")
                        .header("Authorization", "Bearer user-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void postReservation_필수값누락_400반환() throws Exception {
        // given — resourceType 누락
        String body = "{\"resourceId\":1,\"startAt\":\"2025-06-01T09:00:00\",\"endAt\":\"2025-06-01T18:00:00\",\"purpose\":\"출장\"}";
        givenUserAuth();

        // when / then
        mockMvc.perform(post("/api/reservations")
                        .header("Authorization", "Bearer user-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void postReservation_빈purpose_400반환() throws Exception {
        // given — purpose가 빈 문자열 (@NotBlank 위반)
        String body = "{\"resourceType\":\"VEHICLE\",\"resourceId\":1,\"startAt\":\"2025-06-01T09:00:00\",\"endAt\":\"2025-06-01T18:00:00\",\"purpose\":\"\"}";
        givenUserAuth();

        // when / then
        mockMvc.perform(post("/api/reservations")
                        .header("Authorization", "Bearer user-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void postReservation_인증없음_401반환() throws Exception {
        CreateReservationRequest request = new CreateReservationRequest(
                ResourceType.VEHICLE, 1L, START, END, "출장", "서울"
        );

        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ==================== GET /api/reservations/my ====================

    @Test
    void getMyReservations_인증된사용자_200반환() throws Exception {
        // given
        ReservationResponse response = new ReservationResponse(
                1L, ResourceType.VEHICLE, 1L, "소나타",
                1L, "홍길동", "YOUTH",
                START, END, "출장", "서울",
                ReservationStatus.CONFIRMED, Instant.now()
        );
        givenUserAuth();
        given(reservationService.getMyReservations(1L)).willReturn(List.of(response));

        // when / then
        mockMvc.perform(get("/api/reservations/my")
                        .header("Authorization", "Bearer user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].resourceName").value("소나타"))
                .andExpect(jsonPath("$.data[0].userName").value("홍길동"));
    }

    @Test
    void getMyReservations_예약없음_빈목록반환() throws Exception {
        // given
        givenUserAuth();
        given(reservationService.getMyReservations(1L)).willReturn(List.of());

        // when / then
        mockMvc.perform(get("/api/reservations/my")
                        .header("Authorization", "Bearer user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    void getMyReservations_인증없음_401반환() throws Exception {
        mockMvc.perform(get("/api/reservations/my"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ==================== GET /api/reservations/{id} ====================

    @Test
    void getReservation_정상요청_200반환() throws Exception {
        // given
        ReservationResponse response = new ReservationResponse(
                1L, ResourceType.VEHICLE, 1L, "소나타",
                1L, "홍길동", "YOUTH",
                START, END, "출장", "서울",
                ReservationStatus.CONFIRMED, Instant.now()
        );
        givenUserAuth();
        given(reservationService.getReservation(1L)).willReturn(response);

        // when / then
        mockMvc.perform(get("/api/reservations/1")
                        .header("Authorization", "Bearer user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.resourceName").value("소나타"))
                .andExpect(jsonPath("$.data.status").value("CONFIRMED"));
    }

    @Test
    void getReservation_존재하지않는예약_404반환() throws Exception {
        // given
        givenUserAuth();
        willThrow(new ReservationException(ReservationErrorCode.NOT_FOUND))
                .given(reservationService).getReservation(999L);

        // when / then
        mockMvc.perform(get("/api/reservations/999")
                        .header("Authorization", "Bearer user-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void getReservation_인증없음_401반환() throws Exception {
        mockMvc.perform(get("/api/reservations/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ==================== PUT /api/reservations/{id} ====================

    @Test
    void putReservation_정상요청_200반환() throws Exception {
        // given
        UpdateReservationRequest request = new UpdateReservationRequest(START, END, "수정된 출장", "부산");
        ReservationResponse response = new ReservationResponse(
                1L, ResourceType.VEHICLE, 1L, "소나타",
                1L, "홍길동", "YOUTH",
                START, END, "수정된 출장", "부산",
                ReservationStatus.CONFIRMED, Instant.now()
        );
        givenUserAuth();
        given(reservationService.putReservation(eq(1L), any(UpdateReservationRequest.class), eq(1L))).willReturn(response);

        // when / then
        mockMvc.perform(put("/api/reservations/1")
                        .header("Authorization", "Bearer user-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.purpose").value("수정된 출장"));
    }

    @Test
    void putReservation_수정불가상태_400반환() throws Exception {
        // given
        UpdateReservationRequest request = new UpdateReservationRequest(START, END, "출장", "서울");
        givenUserAuth();
        willThrow(new ReservationException(ReservationErrorCode.NOT_MODIFIABLE))
                .given(reservationService).putReservation(eq(1L), any(UpdateReservationRequest.class), eq(1L));

        // when / then
        mockMvc.perform(put("/api/reservations/1")
                        .header("Authorization", "Bearer user-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void putReservation_권한없음_403반환() throws Exception {
        // given
        UpdateReservationRequest request = new UpdateReservationRequest(START, END, "출장", "서울");
        givenUserAuth();
        willThrow(new ReservationException(ReservationErrorCode.FORBIDDEN))
                .given(reservationService).putReservation(eq(1L), any(UpdateReservationRequest.class), eq(1L));

        // when / then
        mockMvc.perform(put("/api/reservations/1")
                        .header("Authorization", "Bearer user-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void putReservation_시간충돌_409반환() throws Exception {
        // given
        UpdateReservationRequest request = new UpdateReservationRequest(START, END, "출장", "서울");
        givenUserAuth();
        willThrow(new ReservationException(ReservationErrorCode.CONFLICT))
                .given(reservationService).putReservation(eq(1L), any(UpdateReservationRequest.class), eq(1L));

        // when / then
        mockMvc.perform(put("/api/reservations/1")
                        .header("Authorization", "Bearer user-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void putReservation_존재하지않는예약_404반환() throws Exception {
        // given
        UpdateReservationRequest request = new UpdateReservationRequest(START, END, "출장", "서울");
        givenUserAuth();
        willThrow(new ReservationException(ReservationErrorCode.NOT_FOUND))
                .given(reservationService).putReservation(eq(999L), any(UpdateReservationRequest.class), eq(1L));

        // when / then
        mockMvc.perform(put("/api/reservations/999")
                        .header("Authorization", "Bearer user-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void putReservation_인증없음_401반환() throws Exception {
        UpdateReservationRequest request = new UpdateReservationRequest(START, END, "출장", "서울");

        mockMvc.perform(put("/api/reservations/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ==================== DELETE /api/reservations/{id} ====================

    @Test
    void deleteReservation_정상요청_204반환() throws Exception {
        // given
        givenUserAuth();
        willDoNothing().given(reservationService).deleteReservation(eq(1L), anyLong());

        // when / then
        mockMvc.perform(delete("/api/reservations/1")
                        .header("Authorization", "Bearer user-token"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteReservation_권한없음_403반환() throws Exception {
        // given
        givenUserAuth();
        willThrow(new ReservationException(ReservationErrorCode.FORBIDDEN))
                .given(reservationService).deleteReservation(eq(1L), anyLong());

        // when / then
        mockMvc.perform(delete("/api/reservations/1")
                        .header("Authorization", "Bearer user-token"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void deleteReservation_존재하지않는예약_404반환() throws Exception {
        // given
        givenUserAuth();
        willThrow(new ReservationException(ReservationErrorCode.NOT_FOUND))
                .given(reservationService).deleteReservation(eq(999L), anyLong());

        // when / then
        mockMvc.perform(delete("/api/reservations/999")
                        .header("Authorization", "Bearer user-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void deleteReservation_이미취소된예약_400반환() throws Exception {
        // given
        givenUserAuth();
        willThrow(new ReservationException(ReservationErrorCode.ALREADY_CANCELLED))
                .given(reservationService).deleteReservation(eq(1L), anyLong());

        // when / then
        mockMvc.perform(delete("/api/reservations/1")
                        .header("Authorization", "Bearer user-token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void deleteReservation_인증없음_401반환() throws Exception {
        mockMvc.perform(delete("/api/reservations/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }
}
