package sky.ch.booking.domain.reservation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sky.ch.booking.domain.auth.entity.User;
import sky.ch.booking.domain.auth.exception.AuthErrorCode;
import sky.ch.booking.domain.auth.exception.AuthException;
import sky.ch.booking.domain.auth.repository.UserRepository;
import sky.ch.booking.domain.reservation.dto.CreateReservationRequest;
import sky.ch.booking.domain.reservation.dto.ReservationResponse;
import sky.ch.booking.domain.reservation.entity.Reservation;
import sky.ch.booking.domain.reservation.entity.ReservationStatus;
import sky.ch.booking.domain.reservation.entity.ResourceType;
import sky.ch.booking.domain.reservation.exception.ReservationErrorCode;
import sky.ch.booking.domain.reservation.exception.ReservationException;
import sky.ch.booking.domain.reservation.repository.ReservationRepository;
import sky.ch.booking.domain.room.entity.Room;
import sky.ch.booking.domain.room.entity.RoomStatus;
import sky.ch.booking.domain.room.exception.RoomErrorCode;
import sky.ch.booking.domain.room.exception.RoomException;
import sky.ch.booking.domain.room.repository.RoomRepository;
import sky.ch.booking.domain.vehicle.entity.Vehicle;
import sky.ch.booking.domain.vehicle.entity.VehicleStatus;
import sky.ch.booking.domain.vehicle.exception.VehicleErrorCode;
import sky.ch.booking.domain.vehicle.exception.VehicleException;
import sky.ch.booking.domain.vehicle.repository.VehicleRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final RoomRepository roomRepository;

    public List<ReservationResponse> getReservations(ResourceType resourceType, LocalDateTime startDate, LocalDateTime endDate) {
        if (!startDate.isBefore(endDate)) {
            throw new ReservationException(ReservationErrorCode.INVALID_DATE_RANGE);
        }

        List<Reservation> reservations = resourceType == null
                ? reservationRepository.findByStartAtBeforeAndEndAtAfterOrderByStartAtAsc(endDate, startDate)
                : reservationRepository.findByStartAtBeforeAndEndAtAfterAndResourceTypeOrderByStartAtAsc(endDate, startDate, resourceType);

        if (reservations.isEmpty()) {
            return List.of();
        }

        Map<Long, User> userMap = loadUsers(reservations);
        Map<Long, String> vehicleNames = loadVehicleNames(reservations);
        Map<Long, String> roomNames = loadRoomNames(reservations);

        return reservations.stream()
                .map(r -> {
                    User user = userMap.get(r.getUserId());
                    String resourceName = r.getResourceType() == ResourceType.VEHICLE
                            ? vehicleNames.getOrDefault(r.getResourceId(), "알 수 없음")
                            : roomNames.getOrDefault(r.getResourceId(), "알 수 없음");
                    if (user == null) {
                        return ReservationResponse.fromDeleted(r, resourceName);
                    }
                    return ReservationResponse.from(r, resourceName, user);
                })
                .toList();
    }

    @Transactional
    public ReservationResponse postReservation(CreateReservationRequest request, Long userId) {
        if (!request.startAt().isBefore(request.endAt())) {
            throw new ReservationException(ReservationErrorCode.INVALID_DATE_RANGE);
        }
        if (request.resourceType() == ResourceType.ROOM && request.destination() != null) {
            throw new ReservationException(ReservationErrorCode.DESTINATION_NOT_ALLOWED);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException(AuthErrorCode.USER_NOT_FOUND));

        // 비관적 락으로 자원 행을 잠근 뒤 상태 검사 → 충돌 검사 → 저장을 하나의 트랜잭션 내에서 직렬화
        String resourceName = switch (request.resourceType()) {
            case ROOM -> {
                Room room = roomRepository.findByIdForUpdate(request.resourceId())
                        .orElseThrow(() -> new RoomException(RoomErrorCode.NOT_FOUND_ROOM));
                if (room.getStatus() != RoomStatus.ACTIVE) {
                    throw new RoomException(RoomErrorCode.NOT_AVAILABLE_ROOM);
                }
                yield room.getName();
            }
            case VEHICLE -> {
                Vehicle vehicle = vehicleRepository.findByIdForUpdate(request.resourceId())
                        .orElseThrow(() -> new VehicleException(VehicleErrorCode.NOT_FOUND_VEHICLE));
                if (vehicle.getStatus() != VehicleStatus.ACTIVE) {
                    throw new VehicleException(VehicleErrorCode.NOT_AVAILABLE_VEHICLE);
                }
                yield vehicle.getModel();
            }
        };

        if (reservationRepository.existsByStartAtBeforeAndEndAtAfterAndResourceTypeAndResourceIdAndStatus(
                request.endAt(),
                request.startAt(),
                request.resourceType(),
                request.resourceId(),
                ReservationStatus.CONFIRMED
        )) {
            throw new ReservationException(ReservationErrorCode.CONFLICT);
        }

        Reservation reservation = Reservation.create(
                request.resourceType(),
                request.resourceId(),
                userId,
                request.startAt(),
                request.endAt(),
                request.purpose(),
                request.destination(),
                ReservationStatus.CONFIRMED
        );
        reservationRepository.save(reservation);

        return ReservationResponse.from(reservation, resourceName, user);
    }

    private Map<Long, User> loadUsers(List<Reservation> reservations) {
        Set<Long> userIds = reservations.stream()
                .map(Reservation::getUserId)
                .collect(Collectors.toSet());
        return userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
    }

    private Map<Long, String> loadVehicleNames(List<Reservation> reservations) {
        Set<Long> vehicleIds = reservations.stream()
                .filter(r -> r.getResourceType() == ResourceType.VEHICLE)
                .map(Reservation::getResourceId)
                .collect(Collectors.toSet());
        if (vehicleIds.isEmpty()) {
            return Map.of();
        }
        return vehicleRepository.findAllById(vehicleIds).stream()
                .collect(Collectors.toMap(
                        Vehicle::getId,
                        Vehicle::getModel
                ));
    }

    private Map<Long, String> loadRoomNames(List<Reservation> reservations) {
        Set<Long> roomIds = reservations.stream()
                .filter(r -> r.getResourceType() == ResourceType.ROOM)
                .map(Reservation::getResourceId)
                .collect(Collectors.toSet());
        if (roomIds.isEmpty()) {
            return Map.of();
        }
        return roomRepository.findAllById(roomIds).stream()
                .collect(Collectors.toMap(
                        Room::getId,
                        Room::getName
                ));
    }
}
