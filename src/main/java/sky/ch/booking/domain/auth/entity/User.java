package sky.ch.booking.domain.auth.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sky.ch.booking.common.domain.BaseTimeEntity;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Department department;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(length = 512)
    private String refreshToken;

    @Builder(access = AccessLevel.PRIVATE)
    private User(String email, String password, String name, Department department, Role role) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.department = department;
        this.role = role;
    }

    public static User create(String email, String password, String name, Department department, Role role) {
        return User.builder()
                .email(email)
                .password(password)
                .name(name)
                .department(department)
                .role(role)
                .build();
    }

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
