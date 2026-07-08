package com.consult.reservation.controller;

import com.consult.reservation.dto.ClientSummaryResponse;
import com.consult.reservation.dto.CounselorSummaryResponse;
import com.consult.reservation.dto.LoginRequest;
import com.consult.reservation.dto.UserCreateRequest;
import com.consult.reservation.dto.UserResponse;
import com.consult.reservation.service.UserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /** GET /api/user/counselors — 상담사 목록 조회 */
    @GetMapping("/counselors")
    public List<CounselorSummaryResponse> getCounselors() {
        return userService.getCounselors();
    }

    /** GET /api/user/clients/search?keyword= — 내담자 이름·전화번호 검색 */
    @GetMapping("/clients/search")
    public List<ClientSummaryResponse> searchClients(@RequestParam String keyword) {
        return userService.searchClients(keyword);
    }

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@RequestBody UserCreateRequest request) {
        return userService.create(request);
    }

    /** POST /api/user/login — loginId·password로 로그인 */
    @PostMapping("/login")
    public UserResponse login(@RequestBody LoginRequest request) {
        return userService.login(request);
    }
}
