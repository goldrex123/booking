package sky.ch.booking.domain.vehicle.controller;

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
import sky.ch.booking.domain.vehicle.dto.CreateVehicleRequest;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminVehicleController.class)
@Import({SecurityConfig.class, CustomAuthenticationEntryPoint.class, CustomAccessDeniedHandler.class})
@ActiveProfiles("test")
class AdminVehicleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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

    // ==================== POST /api/admin/vehicles ====================

    @Test
    void postVehicle_ADMIN인증_201반환() throws Exception {
        // given
        CreateVehicleRequest request = new CreateVehicleRequest("소나타", "12가3456", 5, "메모");
        VehicleResponse response = new VehicleResponse(1L, "소나타", "12가3456", 5, VehicleStatus.ACTIVE, "메모");
        given(jwtProvider.validateToken("admin-token")).willReturn(true);
        given(jwtProvider.getUserId("admin-token")).willReturn("1");
        given(jwtProvider.getRole("admin-token")).willReturn("ADMIN");
        given(vehicleService.postVehicle(any(CreateVehicleRequest.class))).willReturn(response);

        // when / then
        mockMvc.perform(post("/api/admin/vehicles")
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.model").value("소나타"))
                .andExpect(jsonPath("$.data.licensePlate").value("12가3456"))
                .andExpect(jsonPath("$.data.seats").value(5))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.note").value("메모"));
    }

    @Test
    void postVehicle_번호판중복_409반환() throws Exception {
        // given
        CreateVehicleRequest request = new CreateVehicleRequest("소나타", "12가3456", 5, null);
        given(jwtProvider.validateToken("admin-token")).willReturn(true);
        given(jwtProvider.getUserId("admin-token")).willReturn("1");
        given(jwtProvider.getRole("admin-token")).willReturn("ADMIN");
        willThrow(new VehicleException(VehicleErrorCode.DUPLICATE_LICENSE_PLATE_VEHICLE))
                .given(vehicleService).postVehicle(any(CreateVehicleRequest.class));

        // when / then
        mockMvc.perform(post("/api/admin/vehicles")
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void postVehicle_필수값누락_400반환() throws Exception {
        // given — model 누락
        String body = "{\"licensePlate\":\"12가3456\",\"seats\":5}";
        given(jwtProvider.validateToken("admin-token")).willReturn(true);
        given(jwtProvider.getUserId("admin-token")).willReturn("1");
        given(jwtProvider.getRole("admin-token")).willReturn("ADMIN");

        // when / then
        mockMvc.perform(post("/api/admin/vehicles")
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void postVehicle_좌석수0이하_400반환() throws Exception {
        // given — seats = 0 (Min(1) 위반)
        CreateVehicleRequest request = new CreateVehicleRequest("소나타", "12가3456", 0, null);
        given(jwtProvider.validateToken("admin-token")).willReturn(true);
        given(jwtProvider.getUserId("admin-token")).willReturn("1");
        given(jwtProvider.getRole("admin-token")).willReturn("ADMIN");

        // when / then
        mockMvc.perform(post("/api/admin/vehicles")
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void postVehicle_USER권한_403반환() throws Exception {
        // given
        CreateVehicleRequest request = new CreateVehicleRequest("소나타", "12가3456", 5, null);
        given(jwtProvider.validateToken("user-token")).willReturn(true);
        given(jwtProvider.getUserId("user-token")).willReturn("2");
        given(jwtProvider.getRole("user-token")).willReturn("USER");

        // when / then
        mockMvc.perform(post("/api/admin/vehicles")
                        .header("Authorization", "Bearer user-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void postVehicle_인증없음_401반환() throws Exception {
        CreateVehicleRequest request = new CreateVehicleRequest("소나타", "12가3456", 5, null);

        mockMvc.perform(post("/api/admin/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }
}
