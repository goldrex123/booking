package sky.ch.booking.domain.room.dto;

import jakarta.validation.constraints.NotNull;
import sky.ch.booking.domain.room.entity.RoomStatus;

public record UpdateRoomStatusRequest(
        @NotNull
        RoomStatus status
) {
}
