package sky.ch.booking.domain.room.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sky.ch.booking.domain.room.dto.CreateRoomRequest;
import sky.ch.booking.domain.room.dto.RoomResponse;
import sky.ch.booking.domain.room.dto.UpdateRoomRequest;
import sky.ch.booking.domain.room.dto.UpdateRoomStatusRequest;
import sky.ch.booking.domain.room.entity.Room;
import sky.ch.booking.domain.room.entity.RoomStatus;
import sky.ch.booking.domain.room.exception.RoomException;
import sky.ch.booking.domain.room.repository.RoomRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @InjectMocks
    private RoomService roomService;

    @Mock
    private RoomRepository roomRepository;

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
}
