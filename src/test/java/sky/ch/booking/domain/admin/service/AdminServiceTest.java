package sky.ch.booking.domain.admin.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;
import sky.ch.booking.domain.admin.dto.UpdateUserRoleRequest;
import sky.ch.booking.domain.admin.dto.UserResponse;
import sky.ch.booking.domain.auth.entity.Department;
import sky.ch.booking.domain.auth.entity.Role;
import sky.ch.booking.domain.auth.entity.User;
import sky.ch.booking.domain.auth.exception.AuthException;
import sky.ch.booking.domain.auth.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @InjectMocks
    private AdminService adminService;

    @Mock
    private UserRepository userRepository;

    // ==================== getUsers ====================

    @Test
    void getUsers_사용자존재_UserResponse페이지반환() {
        // given
        User user1 = User.create("admin@example.com", "encoded", "관리자", Department.YOUTH, Role.ADMIN);
        User user2 = User.create("user@example.com", "encoded", "일반유저", Department.FATHER, Role.USER);
        ReflectionTestUtils.setField(user1, "id", 1L);
        ReflectionTestUtils.setField(user2, "id", 2L);

        Pageable pageable = PageRequest.of(0, 20);
        Page<User> userPage = new PageImpl<>(List.of(user1, user2), pageable, 2);
        given(userRepository.findAll(pageable)).willReturn(userPage);

        // when
        Page<UserResponse> result = adminService.getUsers(pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).email()).isEqualTo("admin@example.com");
        assertThat(result.getContent().get(0).role()).isEqualTo("ADMIN");
        assertThat(result.getContent().get(0).department()).isEqualTo("YOUTH");
        assertThat(result.getContent().get(1).email()).isEqualTo("user@example.com");
        assertThat(result.getContent().get(1).role()).isEqualTo("USER");
    }

    @Test
    void getUsers_사용자없음_빈페이지반환() {
        // given
        Pageable pageable = PageRequest.of(0, 20);
        given(userRepository.findAll(pageable)).willReturn(new PageImpl<>(List.of(), pageable, 0));

        // when
        Page<UserResponse> result = adminService.getUsers(pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(0);
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void getUsers_department_role_null인경우_null문자열반환() {
        // given — DB constraint가 깨진 레코드 방어
        User user = User.create("broken@example.com", "encoded", "테스트", Department.MOTHER, Role.USER);
        ReflectionTestUtils.setField(user, "id", 3L);
        ReflectionTestUtils.setField(user, "department", null);
        ReflectionTestUtils.setField(user, "role", null);

        Pageable pageable = PageRequest.of(0, 20);
        given(userRepository.findAll(pageable)).willReturn(new PageImpl<>(List.of(user), pageable, 1));

        // when
        Page<UserResponse> result = adminService.getUsers(pageable);

        // then
        assertThat(result.getContent().get(0).department()).isNull();
        assertThat(result.getContent().get(0).role()).isNull();
    }

    // ==================== patchRole ====================

    @Test
    void patchRole_유효한요청_역할변경후UserResponse반환() {
        // given
        User user = User.create("user@example.com", "encoded", "일반유저", Department.FATHER, Role.USER);
        ReflectionTestUtils.setField(user, "id", 2L);
        given(userRepository.findById(2L)).willReturn(Optional.of(user));

        // when
        UserResponse result = adminService.patchRole(2L, new UpdateUserRoleRequest(Role.ADMIN));

        // then
        assertThat(result.id()).isEqualTo(2L);
        assertThat(result.role()).isEqualTo("ADMIN");
        assertThat(result.email()).isEqualTo("user@example.com");
    }

    @Test
    void patchRole_존재하지않는사용자_예외발생() {
        // given
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> adminService.patchRole(999L, new UpdateUserRoleRequest(Role.ADMIN)))
                .isInstanceOf(AuthException.class);
    }
}
