package sky.ch.booking.domain.room.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sky.ch.booking.domain.reservation.entity.ReservationStatus;
import sky.ch.booking.domain.reservation.entity.ResourceType;
import sky.ch.booking.domain.reservation.repository.ReservationRepository;
import sky.ch.booking.domain.room.dto.CreateRoomRequest;
import sky.ch.booking.domain.room.dto.RoomResponse;
import sky.ch.booking.domain.room.dto.UpdateRoomRequest;
import sky.ch.booking.domain.room.dto.UpdateRoomStatusRequest;
import sky.ch.booking.domain.room.entity.Room;
import sky.ch.booking.domain.room.entity.RoomStatus;
import sky.ch.booking.domain.room.exception.RoomErrorCode;
import sky.ch.booking.domain.room.exception.RoomException;
import sky.ch.booking.domain.room.repository.RoomRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomService {

    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;

    public List<RoomResponse> getAllRooms() {
        log.debug("회의실 목록 조회");

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

        log.info("회의실 생성 - roomId: {}, name: {}", room.getId(), room.getName());

        return RoomResponse.from(room);
    }

    @Transactional
    public RoomResponse putRoom(Long id, UpdateRoomRequest request) {
        Room room = findRoom(id);
        room.update(request.name(), request.location(), request.capacity(), request.description());

        log.info("회의실 수정 - roomId: {}", id);

        return RoomResponse.from(room);
    }

    @Transactional
    public RoomResponse patchRoomStatus(Long id, UpdateRoomStatusRequest request) {
        Room room = findRoom(id);
        room.changeStatus(request.status());

        log.info("회의실 상태 변경 - roomId: {}, status: {}", id, request.status());

        return RoomResponse.from(room);
    }

    public List<RoomResponse> getAvailableRoom(LocalDateTime startAt, LocalDateTime endAt, Long excludeId) {
        log.debug("예약 가능 회의실 조회 - startAt: {}, endAt: {}, excludeId: {}", startAt, endAt, excludeId);

        if (!startAt.isBefore(endAt)) {
            throw new RoomException(RoomErrorCode.INVALID_DATE_RANGE);
        }

        Set<Long> resourceIds = reservationRepository.findConflictingResourceIds(
                endAt, startAt, ResourceType.ROOM, ReservationStatus.CONFIRMED, excludeId
        );

        return roomRepository.findByStatus(RoomStatus.ACTIVE).stream()
                .filter(room -> !resourceIds.contains(room.getId()))
                .map(RoomResponse::from)
                .toList();
    }

    private Room findRoom(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new RoomException(RoomErrorCode.NOT_FOUND_ROOM));
    }
}
