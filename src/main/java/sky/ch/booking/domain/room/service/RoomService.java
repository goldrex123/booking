package sky.ch.booking.domain.room.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sky.ch.booking.domain.room.dto.CreateRoomRequest;
import sky.ch.booking.domain.room.dto.RoomResponse;
import sky.ch.booking.domain.room.dto.UpdateRoomRequest;
import sky.ch.booking.domain.room.dto.UpdateRoomStatusRequest;
import sky.ch.booking.domain.room.entity.Room;
import sky.ch.booking.domain.room.exception.RoomErrorCode;
import sky.ch.booking.domain.room.exception.RoomException;
import sky.ch.booking.domain.room.repository.RoomRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomService {

    private final RoomRepository roomRepository;

    public List<RoomResponse> getAllRooms() {
        return roomRepository.findAll().stream()
                .map(RoomResponse::from)
                .toList();
    }

    @Transactional
    public RoomResponse postRoom(CreateRoomRequest request) {
        Room room = Room.create(
                request.name(),
                request.location(),
                request.capacity(),
                request.description()
        );
        roomRepository.save(room);
        return RoomResponse.from(room);
    }

    @Transactional
    public RoomResponse putRoom(Long id, UpdateRoomRequest request) {
        Room room = findRoom(id);
        room.update(request.name(), request.location(), request.capacity(), request.description());
        return RoomResponse.from(room);
    }

    @Transactional
    public RoomResponse patchRoomStatus(Long id, UpdateRoomStatusRequest request) {
        Room room = findRoom(id);
        room.changeStatus(request.status());
        return RoomResponse.from(room);
    }

    private Room findRoom(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new RoomException(RoomErrorCode.NOT_FOUND_ROOM));
    }
}
