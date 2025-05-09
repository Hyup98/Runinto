package com.runinto.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 유효성 검사 실패 처리
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        // 유효성 검사 실패 상세 정보를 추출하여 응답에 포함시키는 것이 일반적입니다.
        // 여기서는 단순화하여 메시지만 사용합니다.
        String errorMessage = ex.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .findFirst() // 첫 번째 위반만 가져오거나, 모두 가져와 리스트로 반환 가능
                .orElse("Validation failed");

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST) // 400 Bad Request
                .body(ErrorResponse.of(HttpStatus.BAD_REQUEST, errorMessage));
    }

    // 새로 추가할 UserNameAlreadyExistsException 핸들러 (이름 중복 처리)
    @ExceptionHandler(UserNameAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserNameAlreadyExists(UserNameAlreadyExistsException ex) {
        log.warn("Attempted registration with existing username: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT) // 409 Conflict (리소스 충돌)
                .body(ErrorResponse.of(HttpStatus.CONFLICT, ex.getMessage()));
    }

    // 새로 추가할 UserEmailAlreadyExistsException 핸들러 (이메일 중복 처리)
    @ExceptionHandler(UserEmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserEmailAlreadyExists(UserEmailAlreadyExistsException ex) {
        log.warn("Attempted registration with existing email: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT) // 409 Conflict
                .body(ErrorResponse.of(HttpStatus.CONFLICT, ex.getMessage()));
    }

}