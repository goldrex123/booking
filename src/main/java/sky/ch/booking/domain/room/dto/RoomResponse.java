package sky.ch.booking.domain.room.dto;

import sky.ch.booking.domain.room.entity.Room;
import sky.ch.booking.domain.room.entity.RoomStatus;

public record RoomResponse(
        Long id,
        String name,
        String location,
        Integer capacity,
        RoomStatus status,
        String description
) {

    public static RoomResponse from(Room room) {
        return new RoomResponse(
                room.getId(),
                room.getName(),
                room.getLocation(),
                room.getCapacity(),
                room.getStatus(),
                room.getDescription()
        );
    }
}
