package sky.ch.booking.domain.reservation.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sky.ch.booking.domain.auth.entity.Department;
import sky.ch.booking.domain.auth.entity.Role;
import sky.ch.booking.domain.auth.entity.User;
import sky.ch.booking.domain.auth.exception.AuthException;
import sky.ch.booking.domain.auth.repository.UserRepository;
import sky.ch.booking.domain.reservation.dto.CreateReservationRequest;
import sky.ch.booking.domain.reservation.dto.UpdateReservationRequest;
import sky.ch.booking.domain.reservation.dto.ReservationResponse;
import sky.ch.booking.domain.reservation.entity.Reservation;
import sky.ch.booking.domain.reservation.entity.ReservationStatus;
import sky.ch.booking.domain.reservation.entity.ResourceType;
import sky.ch.booking.domain.reservation.exception.ReservationException;
import sky.ch.booking.domain.reservation.repository.ReservationRepository;
import sky.ch.booking.domain.room.entity.Room;
import sky.ch.booking.domain.room.entity.RoomStatus;
import sky.ch.booking.domain.room.exception.RoomException;
import sky.ch.booking.domain.room.repository.RoomRepository;
import sky.ch.booking.domain.vehicle.entity.Vehicle;
import sky.ch.booking.domain.vehicle.entity.VehicleStatus;
import sky.ch.booking.domain.vehicle.exception.VehicleException;
import sky.ch.booking.domain.vehicle.repository.VehicleRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.springframework.test.util.ReflectionTestUtils;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @InjectMocks
    private ReservationService reservationService;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private RoomRepository roomRepository;

    private static final LocalDateTime START = LocalDateTime.of(2025, 6, 1, 9, 0);
    private static final LocalDateTime END = LocalDateTime.of(2025, 6, 1, 18, 0);

    // ==================== getReservations ====================

    @Test
    void getReservations_resourceType없이_전체예약반환() {
        // given
        Reservation reservation = Reservation.create(ResourceType.VEHICLE, 1L, 1L, START, END, "출장", "서울", ReservationStatus.CONFIRMED);
        User user = User.create("test@test.com", "pass", "홍길동", Department.YOUTH, Role.USER);
        Vehicle vehicle = Vehicle.create("소나타", "123가4567", 5, null);
        ReflectionTestUtils.setField(user, "id", 1L);
        ReflectionTestUtils.setField(vehicle, "id", 1L);

        given(reservationRepository.findByStartAtBeforeAndEndAtAfterOrderByStartAtAsc(END, START))
                .willReturn(List.of(reservation));
        given(userRepository.findAllById(any())).willReturn(List.of(user));
        given(vehicleRepository.findAllById(any())).willReturn(List.of(vehicle));

        // when
        List<ReservationResponse> result = reservationService.getReservations(null, START, END);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).resourceType()).isEqualTo(ResourceType.VEHICLE);
        assertThat(result.get(0).resourceName()).isEqualTo("소나타");
        assertThat(result.get(0).userName()).isEqualTo("홍길동");
        assertThat(result.get(0).startAt()).isEqualTo(START);
        assertThat(result.get(0).endAt()).isEqualTo(END);
        then(reservationRepository).should().findByStartAtBeforeAndEndAtAfterOrderByStartAtAsc(END, START);
    }

    @Test
    void getReservations_resourceType필터_해당타입예약반환() {
        // given
        Reservation reservation = Reservation.create(ResourceType.VEHICLE, 2L, 1L, START, END, "외근", "부산", ReservationStatus.CONFIRMED);
        User user = User.create("test@test.com", "pass", "홍길동", Department.FATHER, Role.USER);
        Vehicle vehicle = Vehicle.create("그랜저", "456나7890", 5, null);
        ReflectionTestUtils.setField(user, "id", 1L);
        ReflectionTestUtils.setField(vehicle, "id", 2L);

        given(reservationRepository.findByStartAtBeforeAndEndAtAfterAndResourceTypeOrderByStartAtAsc(END, START, ResourceType.VEHICLE))
                .willReturn(List.of(reservation));
        given(userRepository.findAllById(any())).willReturn(List.of(user));
        given(vehicleRepository.findAllById(any())).willReturn(List.of(vehicle));

        // when
        List<ReservationResponse> result = reservationService.getReservations(ResourceType.VEHICLE, START, END);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).resourceType()).isEqualTo(ResourceType.VEHICLE);
        then(reservationRepository).should().findByStartAtBeforeAndEndAtAfterAndResourceTypeOrderByStartAtAsc(END, START, ResourceType.VEHICLE);
        then(reservationRepository).should(never()).findByStartAtBeforeAndEndAtAfterOrderByStartAtAsc(any(), any());
    }

    @Test
    void getReservations_결과없음_빈목록반환() {
        // given
        given(reservationRepository.findByStartAtBeforeAndEndAtAfterOrderByStartAtAsc(END, START))
                .willReturn(List.of());

        // when
        List<ReservationResponse> result = reservationService.getReservations(null, START, END);

        // then
        assertThat(result).isEmpty();
        then(userRepository).should(never()).findAllById(any());
    }

    @Test
    void getReservations_종료일이시작일이전_예외발생() {
        // given
        LocalDateTime invalidEnd = START.minusHours(1);

        // when / then
        assertThatThrownBy(() -> reservationService.getReservations(null, START, invalidEnd))
                .isInstanceOf(ReservationException.class);
    }

    @Test
    void getReservations_탈퇴유저예약_알수없음반환() {
        // given
        Reservation reservation = Reservation.create(ResourceType.VEHICLE, 1L, 999L, START, END, "출장", "서울", ReservationStatus.CONFIRMED);
        Vehicle vehicle = Vehicle.create("소나타", "123가4567", 5, null);

        given(reservationRepository.findByStartAtBeforeAndEndAtAfterOrderByStartAtAsc(END, START))
                .willReturn(List.of(reservation));
        given(userRepository.findAllById(any())).willReturn(List.of());
        given(vehicleRepository.findAllById(any())).willReturn(List.of(vehicle));

        // when
        List<ReservationResponse> result = reservationService.getReservations(null, START, END);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).userName()).isNull();
        assertThat(result.get(0).userDepartment()).isNull();
    }

    // ==================== getMyReservations ====================

    @Test
    void getMyReservations_차량예약존재_목록반환() {
        // given
        User user = User.create("test@test.com", "pass", "홍길동", Department.YOUTH, Role.USER);
        ReflectionTestUtils.setField(user, "id", 1L);
        Reservation reservation = Reservation.create(ResourceType.VEHICLE, 1L, 1L, START, END, "출장", "서울", ReservationStatus.CONFIRMED);
        Vehicle vehicle = Vehicle.create("소나타", "123가4567", 5, null);
        ReflectionTestUtils.setField(vehicle, "id", 1L);

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(reservationRepository.findByUserIdOrderByCreatedAtDesc(1L)).willReturn(List.of(reservation));
        given(vehicleRepository.findAllById(any())).willReturn(List.of(vehicle));

        // when
        List<ReservationResponse> result = reservationService.getMyReservations(1L);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).resourceType()).isEqualTo(ResourceType.VEHICLE);
        assertThat(result.get(0).resourceName()).isEqualTo("소나타");
        assertThat(result.get(0).userName()).isEqualTo("홍길동");
    }

    @Test
    void getMyReservations_예약없음_빈목록반환() {
        // given
        User user = User.create("test@test.com", "pass", "홍길동", Department.YOUTH, Role.USER);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(reservationRepository.findByUserIdOrderByCreatedAtDesc(1L)).willReturn(List.of());

        // when
        List<ReservationResponse> result = reservationService.getMyReservations(1L);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void getMyReservations_존재하지않는유저_예외발생() {
        // given
        given(userRepository.findById(99L)).willReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> reservationService.getMyReservations(99L))
                .isInstanceOf(AuthException.class);
        then(reservationRepository).should(never()).findByUserIdOrderByCreatedAtDesc(any());
    }

    // ==================== postReservation ====================

    @Test
    void postReservation_차량예약_생성성공() {
        // given
        CreateReservationRequest request = new CreateReservationRequest(
                ResourceType.VEHICLE, 1L, START, END, "출장", "서울"
        );
        User user = User.create("test@test.com", "pass", "홍길동", Department.YOUTH, Role.USER);
        Vehicle vehicle = Vehicle.create("소나타", "123가4567", 5, null);
        ReflectionTestUtils.setField(user, "id", 1L);

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(vehicleRepository.findByIdForUpdate(1L)).willReturn(Optional.of(vehicle));
        given(reservationRepository.existsByStartAtBeforeAndEndAtAfterAndResourceTypeAndResourceIdAndStatus(
                END, START, ResourceType.VEHICLE, 1L, ReservationStatus.CONFIRMED
        )).willReturn(false);
        given(reservationRepository.save(any(Reservation.class))).willAnswer(inv -> inv.getArgument(0));

        // when
        ReservationResponse result = reservationService.postReservation(request, 1L);

        // then
        assertThat(result.resourceType()).isEqualTo(ResourceType.VEHICLE);
        assertThat(result.resourceName()).isEqualTo("소나타");
        assertThat(result.userName()).isEqualTo("홍길동");
        assertThat(result.status()).isEqualTo(ReservationStatus.CONFIRMED);
        assertThat(result.destination()).isEqualTo("서울");
        then(reservationRepository).should().save(any(Reservation.class));
    }

    @Test
    void postReservation_부속실예약_생성성공() {
        // given
        CreateReservationRequest request = new CreateReservationRequest(
                ResourceType.ROOM, 2L, START, END, "팀 회의", null
        );
        User user = User.create("test@test.com", "pass", "김철수", Department.MOTHER, Role.USER);
        Room room = Room.create("회의실 A", "3층 301호", 8, null);
        ReflectionTestUtils.setField(user, "id", 2L);

        given(userRepository.findById(2L)).willReturn(Optional.of(user));
        given(roomRepository.findByIdForUpdate(2L)).willReturn(Optional.of(room));
        given(reservationRepository.existsByStartAtBeforeAndEndAtAfterAndResourceTypeAndResourceIdAndStatus(
                END, START, ResourceType.ROOM, 2L, ReservationStatus.CONFIRMED
        )).willReturn(false);
        given(reservationRepository.save(any(Reservation.class))).willAnswer(inv -> inv.getArgument(0));

        // when
        ReservationResponse result = reservationService.postReservation(request, 2L);

        // then
        assertThat(result.resourceType()).isEqualTo(ResourceType.ROOM);
        assertThat(result.resourceName()).isEqualTo("회의실 A");
        assertThat(result.destination()).isNull();
    }

    @Test
    void postReservation_시간충돌_예외발생() {
        // given
        CreateReservationRequest request = new CreateReservationRequest(
                ResourceType.VEHICLE, 1L, START, END, "출장", "서울"
        );
        User user = User.create("test@test.com", "pass", "홍길동", Department.YOUTH, Role.USER);
        Vehicle vehicle = Vehicle.create("소나타", "123가4567", 5, null);

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(vehicleRepository.findByIdForUpdate(1L)).willReturn(Optional.of(vehicle));
        given(reservationRepository.existsByStartAtBeforeAndEndAtAfterAndResourceTypeAndResourceIdAndStatus(
                END, START, ResourceType.VEHICLE, 1L, ReservationStatus.CONFIRMED
        )).willReturn(true);

        // when / then
        assertThatThrownBy(() -> reservationService.postReservation(request, 1L))
                .isInstanceOf(ReservationException.class);
        then(reservationRepository).should(never()).save(any());
    }

    @Test
    void postReservation_종료일이시작일이전_예외발생() {
        // given
        CreateReservationRequest request = new CreateReservationRequest(
                ResourceType.VEHICLE, 1L, END, START, "출장", "서울"
        );

        // when / then
        assertThatThrownBy(() -> reservationService.postReservation(request, 1L))
                .isInstanceOf(ReservationException.class);
        then(reservationRepository).should(never()).existsByStartAtBeforeAndEndAtAfterAndResourceTypeAndResourceIdAndStatus(
                any(), any(), any(), any(), any()
        );
    }

    @Test
    void postReservation_존재하지않는차량_예외발생() {
        // given
        CreateReservationRequest request = new CreateReservationRequest(
                ResourceType.VEHICLE, 999L, START, END, "출장", "서울"
        );
        User user = User.create("test@test.com", "pass", "홍길동", Department.YOUTH, Role.USER);

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(vehicleRepository.findByIdForUpdate(999L)).willReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> reservationService.postReservation(request, 1L))
                .isInstanceOf(VehicleException.class);
        then(reservationRepository).should(never()).save(any());
    }

    @Test
    void postReservation_존재하지않는부속실_예외발생() {
        // given
        CreateReservationRequest request = new CreateReservationRequest(
                ResourceType.ROOM, 999L, START, END, "회의", null
        );
        User user = User.create("test@test.com", "pass", "홍길동", Department.YOUTH, Role.USER);

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(roomRepository.findByIdForUpdate(999L)).willReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> reservationService.postReservation(request, 1L))
                .isInstanceOf(RoomException.class);
        then(reservationRepository).should(never()).save(any());
    }

    @Test
    void postReservation_존재하지않는유저_예외발생() {
        // given
        CreateReservationRequest request = new CreateReservationRequest(
                ResourceType.VEHICLE, 1L, START, END, "출장", "서울"
        );
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> reservationService.postReservation(request, 999L))
                .isInstanceOf(AuthException.class);
        then(vehicleRepository).should(never()).findByIdForUpdate(any());
        then(reservationRepository).should(never()).save(any());
    }

    @Test
    void postReservation_비활성차량_예외발생() {
        // given
        CreateReservationRequest request = new CreateReservationRequest(
                ResourceType.VEHICLE, 1L, START, END, "출장", "서울"
        );
        User user = User.create("test@test.com", "pass", "홍길동", Department.YOUTH, Role.USER);
        Vehicle inactiveVehicle = Vehicle.create("소나타", "123가4567", 5, null);
        inactiveVehicle.changeStatus(VehicleStatus.INACTIVE);

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(vehicleRepository.findByIdForUpdate(1L)).willReturn(Optional.of(inactiveVehicle));

        // when / then
        assertThatThrownBy(() -> reservationService.postReservation(request, 1L))
                .isInstanceOf(VehicleException.class);
        then(reservationRepository).should(never()).save(any());
    }

    @Test
    void postReservation_비활성부속실_예외발생() {
        // given
        CreateReservationRequest request = new CreateReservationRequest(
                ResourceType.ROOM, 1L, START, END, "회의", null
        );
        User user = User.create("test@test.com", "pass", "홍길동", Department.YOUTH, Role.USER);
        Room inactiveRoom = Room.create("회의실 A", "3층", 8, null);
        inactiveRoom.changeStatus(RoomStatus.INACTIVE);

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(roomRepository.findByIdForUpdate(1L)).willReturn(Optional.of(inactiveRoom));

        // when / then
        assertThatThrownBy(() -> reservationService.postReservation(request, 1L))
                .isInstanceOf(RoomException.class);
        then(reservationRepository).should(never()).save(any());
    }

    @Test
    void postReservation_부속실예약에destination입력_예외발생() {
        // given
        CreateReservationRequest request = new CreateReservationRequest(
                ResourceType.ROOM, 1L, START, END, "회의", "서울 강남구"
        );

        // when / then
        assertThatThrownBy(() -> reservationService.postReservation(request, 1L))
                .isInstanceOf(ReservationException.class);
        then(userRepository).should(never()).findById(any());
        then(reservationRepository).should(never()).save(any());
    }

    // ==================== getReservation ====================

    @Test
    void getReservation_차량예약_조회성공() {
        // given
        Reservation reservation = Reservation.create(ResourceType.VEHICLE, 1L, 1L, START, END, "출장", "서울", ReservationStatus.CONFIRMED);
        ReflectionTestUtils.setField(reservation, "id", 1L);
        User user = User.create("test@test.com", "pass", "홍길동", Department.YOUTH, Role.USER);
        ReflectionTestUtils.setField(user, "id", 1L);
        Vehicle vehicle = Vehicle.create("소나타", "123가4567", 5, null);

        given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(vehicleRepository.findById(1L)).willReturn(Optional.of(vehicle));

        // when
        ReservationResponse result = reservationService.getReservation(1L);

        // then
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.resourceType()).isEqualTo(ResourceType.VEHICLE);
        assertThat(result.resourceName()).isEqualTo("소나타");
        assertThat(result.userName()).isEqualTo("홍길동");
        assertThat(result.destination()).isEqualTo("서울");
    }

    @Test
    void getReservation_부속실예약_조회성공() {
        // given
        Reservation reservation = Reservation.create(ResourceType.ROOM, 2L, 2L, START, END, "팀 회의", null, ReservationStatus.CONFIRMED);
        ReflectionTestUtils.setField(reservation, "id", 2L);
        User user = User.create("test@test.com", "pass", "김철수", Department.MOTHER, Role.USER);
        ReflectionTestUtils.setField(user, "id", 2L);
        Room room = Room.create("회의실 A", "3층 301호", 8, null);

        given(reservationRepository.findById(2L)).willReturn(Optional.of(reservation));
        given(userRepository.findById(2L)).willReturn(Optional.of(user));
        given(roomRepository.findById(2L)).willReturn(Optional.of(room));

        // when
        ReservationResponse result = reservationService.getReservation(2L);

        // then
        assertThat(result.id()).isEqualTo(2L);
        assertThat(result.resourceType()).isEqualTo(ResourceType.ROOM);
        assertThat(result.resourceName()).isEqualTo("회의실 A");
        assertThat(result.destination()).isNull();
    }

    @Test
    void getReservation_존재하지않는예약_예외발생() {
        // given
        given(reservationRepository.findById(999L)).willReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> reservationService.getReservation(999L))
                .isInstanceOf(ReservationException.class);
        then(userRepository).should(never()).findById(any());
    }

    // ==================== putReservation ====================

    @Test
    void putReservation_정상수정_성공() {
        // given
        LocalDateTime futureStart = LocalDateTime.now().plusDays(1);
        LocalDateTime futureEnd = futureStart.plusHours(8);
        Reservation reservation = Reservation.create(ResourceType.VEHICLE, 1L, 1L, futureStart, futureEnd, "출장", "서울", ReservationStatus.CONFIRMED);
        ReflectionTestUtils.setField(reservation, "id", 1L);
        User user = User.create("test@test.com", "pass", "홍길동", Department.YOUTH, Role.USER);
        ReflectionTestUtils.setField(user, "id", 1L);
        Vehicle vehicle = Vehicle.create("소나타", "123가4567", 5, null);
        UpdateReservationRequest request = new UpdateReservationRequest(futureStart, futureEnd, "수정된 출장", "부산");

        given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(reservationRepository.existsByStartAtBeforeAndEndAtAfterAndResourceTypeAndResourceIdAndStatusAndIdNot(
                futureEnd, futureStart, ResourceType.VEHICLE, 1L, ReservationStatus.CONFIRMED, 1L
        )).willReturn(false);
        given(vehicleRepository.findById(1L)).willReturn(Optional.of(vehicle));

        // when
        ReservationResponse result = reservationService.putReservation(1L, request, 1L);

        // then
        assertThat(result.purpose()).isEqualTo("수정된 출장");
        assertThat(result.destination()).isEqualTo("부산");
    }

    @Test
    void putReservation_이미시작된예약_예외발생() {
        // given — startAt이 과거
        LocalDateTime pastStart = LocalDateTime.now().minusDays(1);
        LocalDateTime pastEnd = LocalDateTime.now().plusHours(1);
        Reservation reservation = Reservation.create(ResourceType.VEHICLE, 1L, 1L, pastStart, pastEnd, "출장", "서울", ReservationStatus.CONFIRMED);
        ReflectionTestUtils.setField(reservation, "id", 1L);
        UpdateReservationRequest request = new UpdateReservationRequest(pastStart, pastEnd, "수정", null);

        given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));

        // when / then
        assertThatThrownBy(() -> reservationService.putReservation(1L, request, 1L))
                .isInstanceOf(ReservationException.class);
        then(reservationRepository).should(never()).existsByStartAtBeforeAndEndAtAfterAndResourceTypeAndResourceIdAndStatusAndIdNot(
                any(), any(), any(), any(), any(), any()
        );
    }

    @Test
    void putReservation_취소된예약_예외발생() {
        // given — CANCELLED 상태
        LocalDateTime futureStart = LocalDateTime.now().plusDays(1);
        LocalDateTime futureEnd = futureStart.plusHours(8);
        Reservation reservation = Reservation.create(ResourceType.VEHICLE, 1L, 1L, futureStart, futureEnd, "출장", "서울", ReservationStatus.CANCELLED);
        ReflectionTestUtils.setField(reservation, "id", 1L);
        UpdateReservationRequest request = new UpdateReservationRequest(futureStart, futureEnd, "수정", null);

        given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));

        // when / then
        assertThatThrownBy(() -> reservationService.putReservation(1L, request, 1L))
                .isInstanceOf(ReservationException.class);
    }

    @Test
    void putReservation_다른사용자_예외발생() {
        // given — userId=2가 userId=1의 예약을 수정 시도
        LocalDateTime futureStart = LocalDateTime.now().plusDays(1);
        LocalDateTime futureEnd = futureStart.plusHours(8);
        Reservation reservation = Reservation.create(ResourceType.VEHICLE, 1L, 1L, futureStart, futureEnd, "출장", "서울", ReservationStatus.CONFIRMED);
        ReflectionTestUtils.setField(reservation, "id", 1L);
        User otherUser = User.create("other@test.com", "pass", "김철수", Department.FATHER, Role.USER);
        ReflectionTestUtils.setField(otherUser, "id", 2L);
        UpdateReservationRequest request = new UpdateReservationRequest(futureStart, futureEnd, "수정", null);

        given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));
        given(userRepository.findById(2L)).willReturn(Optional.of(otherUser));

        // when / then
        assertThatThrownBy(() -> reservationService.putReservation(1L, request, 2L))
                .isInstanceOf(ReservationException.class);
        then(reservationRepository).should(never()).existsByStartAtBeforeAndEndAtAfterAndResourceTypeAndResourceIdAndStatusAndIdNot(
                any(), any(), any(), any(), any(), any()
        );
    }

    @Test
    void putReservation_ADMIN_다른사용자예약_수정성공() {
        // given — ADMIN이 다른 사용자 예약 수정
        LocalDateTime futureStart = LocalDateTime.now().plusDays(1);
        LocalDateTime futureEnd = futureStart.plusHours(8);
        Reservation reservation = Reservation.create(ResourceType.VEHICLE, 1L, 1L, futureStart, futureEnd, "출장", "서울", ReservationStatus.CONFIRMED);
        ReflectionTestUtils.setField(reservation, "id", 1L);
        User admin = User.create("admin@test.com", "pass", "관리자", Department.FATHER, Role.ADMIN);
        ReflectionTestUtils.setField(admin, "id", 99L);
        Vehicle vehicle = Vehicle.create("소나타", "123가4567", 5, null);
        UpdateReservationRequest request = new UpdateReservationRequest(futureStart, futureEnd, "수정된 목적", null);

        given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));
        given(userRepository.findById(99L)).willReturn(Optional.of(admin));
        given(reservationRepository.existsByStartAtBeforeAndEndAtAfterAndResourceTypeAndResourceIdAndStatusAndIdNot(
                futureEnd, futureStart, ResourceType.VEHICLE, 1L, ReservationStatus.CONFIRMED, 1L
        )).willReturn(false);
        given(vehicleRepository.findById(1L)).willReturn(Optional.of(vehicle));

        // when
        ReservationResponse result = reservationService.putReservation(1L, request, 99L);

        // then
        assertThat(result).isNotNull();
    }

    @Test
    void putReservation_시간충돌_예외발생() {
        // given
        LocalDateTime futureStart = LocalDateTime.now().plusDays(1);
        LocalDateTime futureEnd = futureStart.plusHours(8);
        Reservation reservation = Reservation.create(ResourceType.VEHICLE, 1L, 1L, futureStart, futureEnd, "출장", "서울", ReservationStatus.CONFIRMED);
        ReflectionTestUtils.setField(reservation, "id", 1L);
        User user = User.create("test@test.com", "pass", "홍길동", Department.YOUTH, Role.USER);
        ReflectionTestUtils.setField(user, "id", 1L);
        UpdateReservationRequest request = new UpdateReservationRequest(futureStart, futureEnd, "출장", "서울");

        given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(reservationRepository.existsByStartAtBeforeAndEndAtAfterAndResourceTypeAndResourceIdAndStatusAndIdNot(
                futureEnd, futureStart, ResourceType.VEHICLE, 1L, ReservationStatus.CONFIRMED, 1L
        )).willReturn(true);

        // when / then
        assertThatThrownBy(() -> reservationService.putReservation(1L, request, 1L))
                .isInstanceOf(ReservationException.class);
        then(vehicleRepository).should(never()).findById(any());
    }

    @Test
    void putReservation_존재하지않는예약_예외발생() {
        // given
        given(reservationRepository.findById(999L)).willReturn(Optional.empty());
        UpdateReservationRequest request = new UpdateReservationRequest(START, END, "출장", "서울");

        // when / then
        assertThatThrownBy(() -> reservationService.putReservation(999L, request, 1L))
                .isInstanceOf(ReservationException.class);
        then(userRepository).should(never()).findById(any());
    }

    @Test
    void getReservation_탈퇴유저_fromDeleted반환() {
        // given
        Reservation reservation = Reservation.create(ResourceType.VEHICLE, 1L, 999L, START, END, "출장", "서울", ReservationStatus.CONFIRMED);
        ReflectionTestUtils.setField(reservation, "id", 1L);
        Vehicle vehicle = Vehicle.create("소나타", "123가4567", 5, null);

        given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));
        given(userRepository.findById(999L)).willReturn(Optional.empty());
        given(vehicleRepository.findById(1L)).willReturn(Optional.of(vehicle));

        // when
        ReservationResponse result = reservationService.getReservation(1L);

        // then
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.userName()).isNull();
        assertThat(result.userDepartment()).isNull();
        assertThat(result.resourceName()).isEqualTo("소나타");
    }

    // ==================== deleteReservation ====================

    @Test
    void deleteReservation_정상취소_성공() {
        // given
        Reservation reservation = Reservation.create(ResourceType.VEHICLE, 1L, 1L, START, END, "출장", "서울", ReservationStatus.CONFIRMED);
        ReflectionTestUtils.setField(reservation, "id", 1L);
        User user = User.create("test@test.com", "pass", "홍길동", Department.YOUTH, Role.USER);
        ReflectionTestUtils.setField(user, "id", 1L);

        given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        // when
        reservationService.deleteReservation(1L, 1L);

        // then
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
    }

    @Test
    void deleteReservation_다른사용자_예외발생() {
        // given
        Reservation reservation = Reservation.create(ResourceType.VEHICLE, 1L, 1L, START, END, "출장", "서울", ReservationStatus.CONFIRMED);
        ReflectionTestUtils.setField(reservation, "id", 1L);
        User otherUser = User.create("other@test.com", "pass", "김철수", Department.FATHER, Role.USER);
        ReflectionTestUtils.setField(otherUser, "id", 2L);

        given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));
        given(userRepository.findById(2L)).willReturn(Optional.of(otherUser));

        // when / then
        assertThatThrownBy(() -> reservationService.deleteReservation(1L, 2L))
                .isInstanceOf(ReservationException.class);
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
    }

    @Test
    void deleteReservation_ADMIN_다른사용자예약_취소성공() {
        // given
        Reservation reservation = Reservation.create(ResourceType.VEHICLE, 1L, 1L, START, END, "출장", "서울", ReservationStatus.CONFIRMED);
        ReflectionTestUtils.setField(reservation, "id", 1L);
        User admin = User.create("admin@test.com", "pass", "관리자", Department.FATHER, Role.ADMIN);
        ReflectionTestUtils.setField(admin, "id", 99L);

        given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));
        given(userRepository.findById(99L)).willReturn(Optional.of(admin));

        // when
        reservationService.deleteReservation(1L, 99L);

        // then
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
    }

    @Test
    void deleteReservation_존재하지않는예약_예외발생() {
        // given
        given(reservationRepository.findById(999L)).willReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> reservationService.deleteReservation(999L, 1L))
                .isInstanceOf(ReservationException.class);
        then(userRepository).should(never()).findById(any());
    }
}
