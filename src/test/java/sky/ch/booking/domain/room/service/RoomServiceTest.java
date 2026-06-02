package sky.ch.booking.domain.room.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import sky.ch.booking.domain.reservation.entity.ReservationStatus;
import sky.ch.booking.domain.reservation.entity.ResourceType;
import sky.ch.booking.domain.reservation.repository.ReservationRepository;
import sky.ch.booking.domain.room.dto.CreateRoomRequest;
import sky.ch.booking.domain.room.dto.RoomResponse;
import sky.ch.booking.domain.room.dto.UpdateRoomRequest;
import sky.ch.booking.domain.room.dto.UpdateRoomStatusRequest;
import sky.ch.booking.domain.room.entity.Room;
import sky.ch.booking.domain.room.entity.RoomStatus;
import sky.ch.booking.domain.room.exception.RoomException;
import sky.ch.booking.domain.room.repository.RoomRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @InjectMocks
    private RoomService roomService;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private ReservationRepository reservationRepository;

    private static final LocalDateTime START = LocalDateTime.of(2025, 6, 1, 9, 0);
    private static final LocalDateTime END = LocalDateTime.of(2025, 6, 1, 18, 0);

    // ==================== getAllRooms ====================

    @Test
    void getAllRooms_부속실존재_RoomResponse목록반환() {
        // given
        Room r1 = Room.create("회의실 A", "3층 301호", 8, null);
        Room r2 = Room.create("세미나실", "2층 201호", 20, "빔프로젝터 포함");
        given(roomRepository.findAll()).willReturn(List.of(r1, r2));

        // when
        List<RoomResponse> result = roomService.getAllRooms();

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).name()).isEqualTo("회의실 A");
        assertThat(result.get(0).location()).isEqualTo("3층 301호");
        assertThat(result.get(0).capacity()).isEqualTo(8);
        assertThat(result.get(0).status()).isEqualTo(RoomStatus.ACTIVE);
        assertThat(result.get(0).description()).isNull();
        assertThat(result.get(1).name()).isEqualTo("세미나실");
        assertThat(result.get(1).description()).isEqualTo("빔프로젝터 포함");
    }

    @Test
    void getAllRooms_부속실없음_빈목록반환() {
        // given
        given(roomRepository.findAll()).willReturn(List.of());

        // when
        List<RoomResponse> result = roomService.getAllRooms();

        // then
        assertThat(result).isEmpty();
        then(roomRepository).should().findAll();
    }

    // ==================== postRoom ====================

    @Test
    void postRoom_정상요청_RoomResponse반환() {
        // given
        CreateRoomRequest request = new CreateRoomRequest("회의실 A", "3층 301호", 8, "메모");
        given(roomRepository.save(any(Room.class))).willAnswer(inv -> inv.getArgument(0));

        // when
        RoomResponse result = roomService.postRoom(request);

        // then
        assertThat(result.name()).isEqualTo("회의실 A");
        assertThat(result.location()).isEqualTo("3층 301호");
        assertThat(result.capacity()).isEqualTo(8);
        assertThat(result.status()).isEqualTo(RoomStatus.ACTIVE);
        assertThat(result.description()).isEqualTo("메모");
        then(roomRepository).should().save(any(Room.class));
    }

    // ==================== putRoom ====================

    @Test
    void putRoom_정상요청_RoomResponse반환() {
        // given
        Long id = 1L;
        UpdateRoomRequest request = new UpdateRoomRequest("대회의실", "5층 501호", 30, "대형 스크린");
        Room room = Room.create("회의실 A", "3층 301호", 8, null);
        given(roomRepository.findById(id)).willReturn(Optional.of(room));

        // when
        RoomResponse result = roomService.putRoom(id, request);

        // then
        assertThat(result.name()).isEqualTo("대회의실");
        assertThat(result.location()).isEqualTo("5층 501호");
        assertThat(result.capacity()).isEqualTo(30);
        assertThat(result.description()).isEqualTo("대형 스크린");
        assertThat(result.status()).isEqualTo(RoomStatus.ACTIVE);
    }

    @Test
    void putRoom_존재하지않는부속실_RoomException발생() {
        // given
        Long id = 999L;
        UpdateRoomRequest request = new UpdateRoomRequest("대회의실", "5층 501호", 30, null);
        given(roomRepository.findById(id)).willReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> roomService.putRoom(id, request))
                .isInstanceOf(RoomException.class);
    }

    // ==================== patchRoomStatus ====================

    @Test
    void patchRoomStatus_INACTIVE변경_RoomResponse반환() {
        // given
        Long id = 1L;
        UpdateRoomStatusRequest request = new UpdateRoomStatusRequest(RoomStatus.INACTIVE);
        Room room = Room.create("회의실 A", "3층 301호", 8, null);
        given(roomRepository.findById(id)).willReturn(Optional.of(room));

        // when
        RoomResponse result = roomService.patchRoomStatus(id, request);

        // then
        assertThat(result.status()).isEqualTo(RoomStatus.INACTIVE);
        assertThat(result.name()).isEqualTo("회의실 A");
    }

    @Test
    void patchRoomStatus_ACTIVE복원_RoomResponse반환() {
        // given
        Long id = 1L;
        UpdateRoomStatusRequest request = new UpdateRoomStatusRequest(RoomStatus.ACTIVE);
        Room room = Room.create("회의실 A", "3층 301호", 8, null);
        room.changeStatus(RoomStatus.INACTIVE);
        given(roomRepository.findById(id)).willReturn(Optional.of(room));

        // when
        RoomResponse result = roomService.patchRoomStatus(id, request);

        // then
        assertThat(result.status()).isEqualTo(RoomStatus.ACTIVE);
    }

    @Test
    void patchRoomStatus_존재하지않는부속실_RoomException발생() {
        // given
        Long id = 999L;
        UpdateRoomStatusRequest request = new UpdateRoomStatusRequest(RoomStatus.INACTIVE);
        given(roomRepository.findById(id)).willReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> roomService.patchRoomStatus(id, request))
                .isInstanceOf(RoomException.class);
    }

    // ==================== getAvailableRoom ====================

    @Test
    void getAvailableRoom_예약없음_ACTIVE부속실전체반환() {
        // given
        Room r1 = Room.create("회의실 A", "3층 301호", 8, null);
        Room r2 = Room.create("세미나실", "2층 201호", 20, null);
        ReflectionTestUtils.setField(r1, "id", 1L);
        ReflectionTestUtils.setField(r2, "id", 2L);

        given(roomRepository.findByStatus(RoomStatus.ACTIVE)).willReturn(List.of(r1, r2));
        given(reservationRepository.findConflictingResourceIds(END, START, ResourceType.ROOM, ReservationStatus.CONFIRMED, null))
                .willReturn(Set.of());

        // when
        List<RoomResponse> result = roomService.getAvailableRoom(START, END, null);

        // then
        assertThat(result).hasSize(2);
    }

    @Test
    void getAvailableRoom_예약된부속실제외_가용부속실만반환() {
        // given
        Room r1 = Room.create("회의실 A", "3층 301호", 8, null);
        Room r2 = Room.create("세미나실", "2층 201호", 20, null);
        ReflectionTestUtils.setField(r1, "id", 1L);
        ReflectionTestUtils.setField(r2, "id", 2L);

        given(roomRepository.findByStatus(RoomStatus.ACTIVE)).willReturn(List.of(r1, r2));
        given(reservationRepository.findConflictingResourceIds(END, START, ResourceType.ROOM, ReservationStatus.CONFIRMED, null))
                .willReturn(Set.of(1L));

        // when
        List<RoomResponse> result = roomService.getAvailableRoom(START, END, null);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("세미나실");
    }

    @Test
    void getAvailableRoom_excludeId지정_해당예약부속실포함() {
        // given — 부속실 1이 예약되어 있지만 excludeId=3으로 제외 처리
        Room r1 = Room.create("회의실 A", "3층 301호", 8, null);
        ReflectionTestUtils.setField(r1, "id", 1L);

        given(roomRepository.findByStatus(RoomStatus.ACTIVE)).willReturn(List.of(r1));
        given(reservationRepository.findConflictingResourceIds(END, START, ResourceType.ROOM, ReservationStatus.CONFIRMED, 3L))
                .willReturn(Set.of());

        // when
        List<RoomResponse> result = roomService.getAvailableRoom(START, END, 3L);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("회의실 A");
    }

    @Test
    void getAvailableRoom_종료일이시작일이전_예외발생() {
        // when / then
        assertThatThrownBy(() -> roomService.getAvailableRoom(END, START, null))
                .isInstanceOf(RoomException.class);
        then(roomRepository).should(never()).findByStatus(any());
    }

    @Test
    void getAvailableRoom_ACTIVE부속실없음_빈목록반환() {
        // given
        given(roomRepository.findByStatus(RoomStatus.ACTIVE)).willReturn(List.of());
        given(reservationRepository.findConflictingResourceIds(END, START, ResourceType.ROOM, ReservationStatus.CONFIRMED, null))
                .willReturn(Set.of());

        // when
        List<RoomResponse> result = roomService.getAvailableRoom(START, END, null);

        // then
        assertThat(result).isEmpty();
    }
}
