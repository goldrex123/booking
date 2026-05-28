package sky.ch.booking.domain.reservation.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sky.ch.booking.common.domain.BaseTimeEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResourceType resourceType;

    @Column(nullable = false)
    private Long resourceId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private LocalDateTime startAt;

    @Column(nullable = false)
    private LocalDateTime endAt;

    @Column(nullable = false)
    private String purpose;

    private String destination;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    @Version
    private Long version;

    @Builder(access = AccessLevel.PRIVATE)
    private Reservation(ResourceType resourceType, Long resourceId, Long userId, LocalDateTime startAt, LocalDateTime endAt, String purpose, String destination, ReservationStatus status) {
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.userId = userId;
        this.startAt = startAt;
        this.endAt = endAt;
        this.purpose = purpose;
        this.destination = destination;
        this.status = status;
    }

    public static Reservation create(ResourceType resourceType, Long resourceId, Long userId, LocalDateTime startAt, LocalDateTime endAt, String purpose, String destination, ReservationStatus status) {
        return Reservation.builder()
                .resourceType(resourceType)
                .resourceId(resourceId)
                .userId(userId)
                .startAt(startAt)
                .endAt(endAt)
                .purpose(purpose)
                .destination(destination)
                .status(status)
                .build();
    }

    public void update(LocalDateTime startAt, LocalDateTime endAt, String purpose, String destination) {
        this.startAt = startAt;
        this.endAt = endAt;
        this.purpose = purpose;
        this.destination = destination;
    }

    public void changeStatus(ReservationStatus status) {
        this.status = status;
    }
}
