package sky.ch.booking.domain.admin.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sky.ch.booking.domain.admin.dto.UpdateUserRoleRequest;
import sky.ch.booking.domain.admin.dto.UserResponse;
import sky.ch.booking.domain.auth.entity.Role;
import sky.ch.booking.domain.auth.entity.User;
import sky.ch.booking.domain.auth.exception.AuthErrorCode;
import sky.ch.booking.domain.auth.exception.AuthException;
import sky.ch.booking.domain.auth.repository.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

    private final UserRepository userRepository;

    public Page<UserResponse> getUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(UserResponse::from);
    }

    @Transactional
    public UserResponse patchRole(Long id, UpdateUserRoleRequest request) {
        //사용자 검증
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AuthException(AuthErrorCode.USER_NOT_FOUND));
        //역할 변경
        Role previousRole = user.getRole();
        user.changeRole(request.role());

        log.info("사용자 역할 변경 - userId: {}, {} -> {}", id, previousRole, request.role());

        return UserResponse.from(user);
    }
}
