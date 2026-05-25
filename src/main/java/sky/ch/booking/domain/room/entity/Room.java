package sky.ch.booking.domain.room.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sky.ch.booking.common.domain.BaseTimeEntity;

@Entity
@Table(name = "rooms")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Room extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 200)
    private String location;

    @Column(nullable = false)
    private Integer capacity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoomStatus status;

    @Column(length = 500)
    private String description;

    @Builder(access = AccessLevel.PRIVATE)
    private Room(String name, String location, Integer capacity, RoomStatus status, String description) {
        this.name = name;
        this.location = location;
        this.capacity = capacity;
        this.status = status;
        this.description = description;
    }

    public static Room create(String name, String location, Integer capacity, String description) {
        return Room.builder()
                .name(name)
                .location(location)
                .capacity(capacity)
                .status(RoomStatus.ACTIVE)
                .description(description)
                .build();
    }

    public void update(String name, String location, Integer capacity, String description) {
        this.name = name;
        this.location = location;
        this.capacity = capacity;
        this.description = description;
    }

    public void changeStatus(RoomStatus status) {
        this.status = status;
    }
}
