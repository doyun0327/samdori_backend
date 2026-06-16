package com.consult.reservation.exception;

import com.consult.reservation.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /** ResponseStatusException의 reason을 message로 프론트에 전달 */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(ResponseStatusException ex) {
        String message = ex.getReason();
        if (message == null || message.isBlank()) {
            message = ex.getStatusCode().toString();
        }

        ErrorResponse body = new ErrorResponse(ex.getStatusCode().value(), message);
        return ResponseEntity.status(ex.getStatusCode()).body(body);
    }

    /** 예상하지 못한 서버 오류 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        ErrorResponse body = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "서버 오류가 발생했습니다."
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
