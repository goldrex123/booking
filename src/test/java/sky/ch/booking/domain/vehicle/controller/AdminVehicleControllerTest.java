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
import sky.ch.booking.domain.vehicle.service.VehicleService;
import sky.ch.booking.security.handler.CustomAccessDeniedHandler;
import sky.ch.booking.security.handler.CustomAuthenticationEntryPoint;
import sky.ch.booking.security.jwt.JwtProvider;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminVehicleController.class)
@Import({SecurityConfig.class, CustomAuthenticationEntryPoint.class, CustomAccessDeniedHandler.class})
@ActiveProfiles("test")
class AdminVehicleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VehicleService vehicleService;

    @MockitoBean
    private JwtProvider jwtProvider;

    // ==================== GET /api/admin/vehicles ====================

    @Test
    void getAllVehicles_ADMIN인증_200반환() throws Exception {
        // given
        List<VehicleResponse> vehicles = List.of(
                new VehicleResponse(1L, "소나타", "12가3456", 5, VehicleStatus.ACTIVE, null),
                new VehicleResponse(2L, "스타리아", "34나5678", 11, VehicleStatus.INACTIVE, "정비 중")
        );
        given(jwtProvider.validateToken("admin-token")).willReturn(true);
        given(jwtProvider.getUserId("admin-token")).willReturn("1");
        given(jwtProvider.getRole("admin-token")).willReturn("ADMIN");
        given(vehicleService.getAllVehicles()).willReturn(vehicles);

        // when / then
        mockMvc.perform(get("/api/admin/vehicles")
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].model").value("소나타"))
                .andExpect(jsonPath("$.data[0].licensePlate").value("12가3456"))
                .andExpect(jsonPath("$.data[1].model").value("스타리아"));
    }

    @Test
    void getAllVehicles_빈목록_200반환() throws Exception {
        // given
        given(jwtProvider.validateToken("admin-token")).willReturn(true);
        given(jwtProvider.getUserId("admin-token")).willReturn("1");
        given(jwtProvider.getRole("admin-token")).willReturn("ADMIN");
        given(vehicleService.getAllVehicles()).willReturn(List.of());

        // when / then
        mockMvc.perform(get("/api/admin/vehicles")
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    void getAllVehicles_USER권한_403반환() throws Exception {
        // given
        given(jwtProvider.validateToken("user-token")).willReturn(true);
        given(jwtProvider.getUserId("user-token")).willReturn("2");
        given(jwtProvider.getRole("user-token")).willReturn("USER");

        // when / then
        mockMvc.perform(get("/api/admin/vehicles")
                        .header("Authorization", "Bearer user-token"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void getAllVehicles_인증없음_401반환() throws Exception {
        mockMvc.perform(get("/api/admin/vehicles"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }
}
