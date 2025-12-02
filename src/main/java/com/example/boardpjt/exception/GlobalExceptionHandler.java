package com.example.boardpjt.exception;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleException(Exception ex, Model model) {
        model.addAttribute("errorMessage", "요청 처리 중 오류가 발생했습니다.");
        model.addAttribute("exception", ex.getMessage());
        return "error/500";
    }

    // 404 Not Found 예외는 Spring Boot가 기본적으로 처리하지만,
    // 커스텀 페이지를 원하면 ErrorController를 구현하여 처리할 수 있습니다.
}