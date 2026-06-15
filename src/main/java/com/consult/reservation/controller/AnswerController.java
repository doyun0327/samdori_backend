package com.consult.reservation.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
public class AnswerController {

    @GetMapping("/api/answer")
    public Map<String, String> answer() {
        return Map.of("answer", "스프링부트에서 보낸 데이터ㅋㅋ");
    }
}