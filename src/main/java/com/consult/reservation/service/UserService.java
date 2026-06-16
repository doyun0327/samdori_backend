package com.consult.reservation.service;

import com.consult.reservation.dto.UserCreateRequest;
import com.consult.reservation.dto.UserResponse;
import com.consult.reservation.entity.User;
import com.consult.reservation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

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

        return new UserResponse(userRepository.save(user));
    }

    private void validate(UserCreateRequest request) {
        if (isBlank(request.getLoginId())
                || isBlank(request.getName())
                || isBlank(request.getPhoneNumber())
                || isBlank(request.getEmail())
                || isBlank(request.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "모든 필드를 입력해주세요.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
