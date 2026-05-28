package sky.ch.booking.domain.vehicle.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sky.ch.booking.domain.reservation.entity.ReservationStatus;
import sky.ch.booking.domain.reservation.entity.ResourceType;
import sky.ch.booking.domain.reservation.repository.ReservationRepository;
import sky.ch.booking.domain.vehicle.dto.CreateVehicleRequest;
import sky.ch.booking.domain.vehicle.dto.UpdateVehicleRequest;
import sky.ch.booking.domain.vehicle.dto.UpdateVehicleStatusRequest;
import sky.ch.booking.domain.vehicle.dto.VehicleResponse;
import sky.ch.booking.domain.vehicle.entity.Vehicle;
import sky.ch.booking.domain.vehicle.entity.VehicleStatus;
import sky.ch.booking.domain.vehicle.exception.VehicleException;
import sky.ch.booking.domain.vehicle.repository.VehicleRepository;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @InjectMocks
    private VehicleService vehicleService;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private ReservationRepository reservationRepository;

    private static final LocalDateTime START = LocalDateTime.of(2025, 6, 1, 9, 0);
    private static final LocalDateTime END = LocalDateTime.of(2025, 6, 1, 18, 0);

    // ==================== getAllVehicles ====================

    @Test
    void getAllVehicles_차량존재_VehicleResponse목록반환() {
        // given
        Vehicle v1 = Vehicle.create("소나타", "12가3456", 5, null);
        Vehicle v2 = Vehicle.create("스타리아", "34나5678", 11, "정비 중");
        given(vehicleRepository.findAll()).willReturn(List.of(v1, v2));

        // when
        List<VehicleResponse> result = vehicleService.getAllVehicles();

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).model()).isEqualTo("소나타");
        assertThat(result.get(0).licensePlate()).isEqualTo("12가3456");
        assertThat(result.get(0).seats()).isEqualTo(5);
        assertThat(result.get(0).status()).isEqualTo(VehicleStatus.ACTIVE);
        assertThat(result.get(0).note()).isNull();
        assertThat(result.get(1).model()).isEqualTo("스타리아");
        assertThat(result.get(1).note()).isEqualTo("정비 중");
    }

    @Test
    void getAllVehicles_차량없음_빈목록반환() {
        // given
        given(vehicleRepository.findAll()).willReturn(List.of());

        // when
        List<VehicleResponse> result = vehicleService.getAllVehicles();

        // then
        assertThat(result).isEmpty();
        then(vehicleRepository).should().findAll();
    }

    // ==================== postVehicle ====================

    @Test
    void postVehicle_정상요청_VehicleResponse반환() {
        // given
        CreateVehicleRequest request = new CreateVehicleRequest("소나타", "12가3456", 5, "메모");
        given(vehicleRepository.existsByLicensePlate("12가3456")).willReturn(false);
        given(vehicleRepository.save(any(Vehicle.class))).willAnswer(inv -> inv.getArgument(0));

        // when
        VehicleResponse result = vehicleService.postVehicle(request);

        // then
        assertThat(result.model()).isEqualTo("소나타");
        assertThat(result.licensePlate()).isEqualTo("12가3456");
        assertThat(result.seats()).isEqualTo(5);
        assertThat(result.status()).isEqualTo(VehicleStatus.ACTIVE);
        assertThat(result.note()).isEqualTo("메모");
        then(vehicleRepository).should().save(any(Vehicle.class));
    }

    @Test
    void postVehicle_번호판중복_VehicleException발생() {
        // given
        CreateVehicleRequest request = new CreateVehicleRequest("소나타", "12가3456", 5, null);
        given(vehicleRepository.existsByLicensePlate("12가3456")).willReturn(true);

        // when / then
        assertThatThrownBy(() -> vehicleService.postVehicle(request))
                .isInstanceOf(VehicleException.class);
        then(vehicleRepository).shouldHaveNoMoreInteractions();
    }

    // ==================== putVehicle ====================

    @Test
    void putVehicle_정상요청_VehicleResponse반환() {
        // given
        Long id = 1L;
        UpdateVehicleRequest request = new UpdateVehicleRequest("그랜저", 7, "VIP용");
        Vehicle vehicle = Vehicle.create("소나타", "12가3456", 5, null);
        given(vehicleRepository.findById(id)).willReturn(Optional.of(vehicle));

        // when
        VehicleResponse result = vehicleService.putVehicle(id, request);

        // then
        assertThat(result.model()).isEqualTo("그랜저");
        assertThat(result.licensePlate()).isEqualTo("12가3456");
        assertThat(result.seats()).isEqualTo(7);
        assertThat(result.note()).isEqualTo("VIP용");
        assertThat(result.status()).isEqualTo(VehicleStatus.ACTIVE);
    }

    @Test
    void putVehicle_존재하지않는차량_VehicleException발생() {
        // given
        Long id = 999L;
        UpdateVehicleRequest request = new UpdateVehicleRequest("그랜저", 7, null);
        given(vehicleRepository.findById(id)).willReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> vehicleService.putVehicle(id, request))
                .isInstanceOf(VehicleException.class);
    }

    // ==================== patchVehicleStatus ====================

    @Test
    void patchVehicleStatus_INACTIVE변경_VehicleResponse반환() {
        // given
        Long id = 1L;
        UpdateVehicleStatusRequest request = new UpdateVehicleStatusRequest(VehicleStatus.INACTIVE);
        Vehicle vehicle = Vehicle.create("소나타", "12가3456", 5, null);
        given(vehicleRepository.findById(id)).willReturn(Optional.of(vehicle));

        // when
        VehicleResponse result = vehicleService.patchVehicleStatus(id, request);

        // then
        assertThat(result.status()).isEqualTo(VehicleStatus.INACTIVE);
        assertThat(result.model()).isEqualTo("소나타");
        assertThat(result.licensePlate()).isEqualTo("12가3456");
    }

    @Test
    void patchVehicleStatus_ACTIVE복원_VehicleResponse반환() {
        // given
        Long id = 1L;
        UpdateVehicleStatusRequest request = new UpdateVehicleStatusRequest(VehicleStatus.ACTIVE);
        Vehicle vehicle = Vehicle.create("소나타", "12가3456", 5, null);
        vehicle.changeStatus(VehicleStatus.INACTIVE);
        given(vehicleRepository.findById(id)).willReturn(Optional.of(vehicle));

        // when
        VehicleResponse result = vehicleService.patchVehicleStatus(id, request);

        // then
        assertThat(result.status()).isEqualTo(VehicleStatus.ACTIVE);
    }

    @Test
    void patchVehicleStatus_존재하지않는차량_VehicleException발생() {
        // given
        Long id = 999L;
        UpdateVehicleStatusRequest request = new UpdateVehicleStatusRequest(VehicleStatus.INACTIVE);
        given(vehicleRepository.findById(id)).willReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> vehicleService.patchVehicleStatus(id, request))
                .isInstanceOf(VehicleException.class);
    }

    // ==================== getAvailableVehicles ====================

    @Test
    void getAvailableVehicles_예약없음_ACTIVE차량전체반환() {
        // given
        Vehicle v1 = Vehicle.create("소나타", "12가3456", 5, null);
        Vehicle v2 = Vehicle.create("그랜저", "34나5678", 5, null);
        ReflectionTestUtils.setField(v1, "id", 1L);
        ReflectionTestUtils.setField(v2, "id", 2L);

        given(vehicleRepository.findByStatus(VehicleStatus.ACTIVE)).willReturn(List.of(v1, v2));
        given(reservationRepository.findConflictingResourceIds(END, START, ResourceType.VEHICLE, ReservationStatus.CONFIRMED, null))
                .willReturn(Set.of());

        // when
        List<VehicleResponse> result = vehicleService.getAvailableVehicles(START, END, null);

        // then
        assertThat(result).hasSize(2);
    }

    @Test
    void getAvailableVehicles_예약된차량제외_가용차량만반환() {
        // given
        Vehicle v1 = Vehicle.create("소나타", "12가3456", 5, null);
        Vehicle v2 = Vehicle.create("그랜저", "34나5678", 5, null);
        ReflectionTestUtils.setField(v1, "id", 1L);
        ReflectionTestUtils.setField(v2, "id", 2L);

        given(vehicleRepository.findByStatus(VehicleStatus.ACTIVE)).willReturn(List.of(v1, v2));
        given(reservationRepository.findConflictingResourceIds(END, START, ResourceType.VEHICLE, ReservationStatus.CONFIRMED, null))
                .willReturn(Set.of(1L));

        // when
        List<VehicleResponse> result = vehicleService.getAvailableVehicles(START, END, null);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).model()).isEqualTo("그랜저");
    }

    @Test
    void getAvailableVehicles_excludeId지정_해당예약차량포함() {
        // given — 차량 1이 예약되어 있지만 excludeId=3으로 제외 처리
        Vehicle v1 = Vehicle.create("소나타", "12가3456", 5, null);
        ReflectionTestUtils.setField(v1, "id", 1L);

        given(vehicleRepository.findByStatus(VehicleStatus.ACTIVE)).willReturn(List.of(v1));
        given(reservationRepository.findConflictingResourceIds(END, START, ResourceType.VEHICLE, ReservationStatus.CONFIRMED, 3L))
                .willReturn(Set.of());

        // when
        List<VehicleResponse> result = vehicleService.getAvailableVehicles(START, END, 3L);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).model()).isEqualTo("소나타");
    }

    @Test
    void getAvailableVehicles_종료일이시작일이전_예외발생() {
        // when / then
        assertThatThrownBy(() -> vehicleService.getAvailableVehicles(END, START, null))
                .isInstanceOf(VehicleException.class);
        then(vehicleRepository).should(never()).findByStatus(any());
    }

    @Test
    void getAvailableVehicles_ACTIVE차량없음_빈목록반환() {
        // given
        given(vehicleRepository.findByStatus(VehicleStatus.ACTIVE)).willReturn(List.of());
        given(reservationRepository.findConflictingResourceIds(END, START, ResourceType.VEHICLE, ReservationStatus.CONFIRMED, null))
                .willReturn(Set.of());

        // when
        List<VehicleResponse> result = vehicleService.getAvailableVehicles(START, END, null);

        // then
        assertThat(result).isEmpty();
    }
}
