package com.consult.reservation.service;

import com.consult.reservation.dto.CounselorSummaryResponse;
import com.consult.reservation.dto.LoginRequest;
import com.consult.reservation.dto.UserCreateRequest;
import com.consult.reservation.dto.UserResponse;
import com.consult.reservation.entity.User;
import com.consult.reservation.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final String COUNSELOR_ROLE = "COUNSELOR";

    private final UserRepository userRepository;

    /** 회원가입: 중복 검사 후 사용자를 저장한다. */
    public UserResponse create(UserCreateRequest request) {
        validate(request);

        if (userRepository.existsByLoginId(request.getLoginId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 사용 중인 loginId입니다.");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 사용 중인 email입니다.");
        }

        User user = new User();
        user.setLoginId(request.getLoginId());
        user.setName(request.getName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setRole(request.getRole());

        return new UserResponse(userRepository.save(user));
    }

    private void validate(UserCreateRequest request) {
        if (isBlank(request.getLoginId())
                || isBlank(request.getName())
                || isBlank(request.getPhoneNumber())
                || isBlank(request.getEmail())
                || isBlank(request.getPassword())
                || isBlank(request.getRole())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "모든 필드를 입력해주세요.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /**
     * 로그인: loginId로 사용자를 조회한 뒤 비밀번호를 비교한다.
     * 성공 시 UserResponse를 반환한다.
     */
    public UserResponse login(LoginRequest request) {
        User user = userRepository.findByLoginId(request.getLoginId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 아이디입니다."));

        if (!user.getPassword().equals(request.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다.");
        }

        return new UserResponse(user);
    }

    /** role이 COUNSELOR인 사용자 목록을 반환한다. */
    public List<CounselorSummaryResponse> getCounselors() {
        return userRepository.findByRoleOrderByNameAsc(COUNSELOR_ROLE).stream()
                .map(CounselorSummaryResponse::new)
                .toList();
    }

}
