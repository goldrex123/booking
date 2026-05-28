package sky.ch.booking.domain.vehicle.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import sky.ch.booking.config.SecurityConfig;
import sky.ch.booking.domain.vehicle.dto.VehicleResponse;
import sky.ch.booking.domain.vehicle.entity.VehicleStatus;
import sky.ch.booking.domain.vehicle.exception.VehicleErrorCode;
import sky.ch.booking.domain.vehicle.exception.VehicleException;
import sky.ch.booking.domain.vehicle.service.VehicleService;
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

@WebMvcTest(VehicleController.class)
@Import({SecurityConfig.class, CustomAuthenticationEntryPoint.class, CustomAccessDeniedHandler.class})
@ActiveProfiles("test")
class VehicleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VehicleService vehicleService;

    @MockitoBean
    private JwtProvider jwtProvider;

    private void givenUserAuth() {
        given(jwtProvider.validateToken("user-token")).willReturn(true);
        given(jwtProvider.getUserId("user-token")).willReturn("1");
        given(jwtProvider.getRole("user-token")).willReturn("USER");
    }

    // ==================== GET /api/vehicles/available ====================

    @Test
    void getAvailableVehicles_인증된사용자_200반환() throws Exception {
        // given
        List<VehicleResponse> vehicles = List.of(
                new VehicleResponse(1L, "소나타", "12가3456", 5, VehicleStatus.ACTIVE, null)
        );
        givenUserAuth();
        given(vehicleService.getAvailableVehicles(any(), any(), isNull())).willReturn(vehicles);

        // when / then
        mockMvc.perform(get("/api/vehicles/available")
                        .header("Authorization", "Bearer user-token")
                        .param("startAt", "2025-06-01T09:00:00")
                        .param("endAt", "2025-06-01T18:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].model").value("소나타"))
                .andExpect(jsonPath("$.data[0].status").value("ACTIVE"));
    }

    @Test
    void getAvailableVehicles_excludeId포함_200반환() throws Exception {
        // given
        List<VehicleResponse> vehicles = List.of(
                new VehicleResponse(2L, "그랜저", "34나5678", 5, VehicleStatus.ACTIVE, null)
        );
        givenUserAuth();
        given(vehicleService.getAvailableVehicles(any(), any(), any())).willReturn(vehicles);

        // when / then
        mockMvc.perform(get("/api/vehicles/available")
                        .header("Authorization", "Bearer user-token")
                        .param("startAt", "2025-06-01T09:00:00")
                        .param("endAt", "2025-06-01T18:00:00")
                        .param("excludeId", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].model").value("그랜저"));
    }

    @Test
    void getAvailableVehicles_빈목록_200반환() throws Exception {
        // given
        givenUserAuth();
        given(vehicleService.getAvailableVehicles(any(), any(), isNull())).willReturn(List.of());

        // when / then
        mockMvc.perform(get("/api/vehicles/available")
                        .header("Authorization", "Bearer user-token")
                        .param("startAt", "2025-06-01T09:00:00")
                        .param("endAt", "2025-06-01T18:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    void getAvailableVehicles_잘못된날짜범위_400반환() throws Exception {
        // given
        givenUserAuth();
        willThrow(new VehicleException(VehicleErrorCode.INVALID_DATE_RANGE))
                .given(vehicleService).getAvailableVehicles(any(), any(), isNull());

        // when / then
        mockMvc.perform(get("/api/vehicles/available")
                        .header("Authorization", "Bearer user-token")
                        .param("startAt", "2025-06-01T18:00:00")
                        .param("endAt", "2025-06-01T09:00:00"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void getAvailableVehicles_인증없음_401반환() throws Exception {
        mockMvc.perform(get("/api/vehicles/available")
                        .param("startAt", "2025-06-01T09:00:00")
                        .param("endAt", "2025-06-01T18:00:00"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void getAvailableVehicles_파라미터누락_400반환() throws Exception {
        // given — endAt 누락
        givenUserAuth();

        // when / then
        mockMvc.perform(get("/api/vehicles/available")
                        .header("Authorization", "Bearer user-token")
                        .param("startAt", "2025-06-01T09:00:00"))
                .andExpect(status().isBadRequest());
    }
}
