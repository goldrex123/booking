package sky.ch.booking.domain.vehicle.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sky.ch.booking.common.domain.BaseTimeEntity;

@Entity
@Table(name = "vehicles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Vehicle extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String model;

    @Column(nullable = false, unique = true, length = 20)
    private String licensePlate;

    @Column(nullable = false)
    private Integer seats;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VehicleStatus status;

    @Column(length = 500)
    private String note;

    @Builder(access = AccessLevel.PRIVATE)
    private Vehicle(String model, String licensePlate, Integer seats, VehicleStatus status, String note) {
        this.model = model;
        this.licensePlate = licensePlate;
        this.seats = seats;
        this.status = status;
        this.note = note;
    }

    public static Vehicle create(String model, String licensePlate, Integer seats, String note) {
        return Vehicle.builder()
                .model(model)
                .licensePlate(licensePlate)
                .seats(seats)
                .status(VehicleStatus.ACTIVE)
                .note(note)
                .build();
    }

    public void update(String model, Integer seats, String note) {
        this.model = model;
        this.seats = seats;
        this.note = note;
    }

    public void changeStatus(VehicleStatus status) {
        this.status = status;
    }
}
