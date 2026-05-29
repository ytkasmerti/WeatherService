package ru.urfu.webapplication.advice;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {
    @ExceptionHandler(WebClientResponseException.class)
    public Map<String, Object> handleApiError(WebClientResponseException e) {
        log.error("Ошибка при вызове внешнего API: {} {}", e.getStatusCode(), e.getMessage());
        Map<String, Object> error = new HashMap<>();
        error.put("error", "Ошибка при получении данных от погодного сервиса");
        error.put("status", e.getStatusCode().value());
        error.put("message", "Сервис погоды временно недоступен");
        return error;
    }

    @ExceptionHandler(RuntimeException.class)
    public Map<String, String> handleRuntimeException(RuntimeException e) {
        log.error("Ошибка выполнения: {}", e.getMessage());
        Map<String, String> error = new HashMap<>();
        error.put("error", "Ошибка выполнения запроса");
        error.put("message", e.getMessage());
        return error;
    }

    @ExceptionHandler(Exception.class)
    public Map<String, String> handleException(Exception e) {
        log.error("Непредвиденная ошибка: {}", e.getMessage());
        Map<String, String> error = new HashMap<>();
        error.put("error", "Внутренняя ошибка сервера");
        error.put("message", "Попробуйте позже");
        return error;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public Map<String, String> handleValidationError(ConstraintViolationException e) {
        log.error("Ошибка валидации: {}", e.getMessage());
        Map<String, String> error = new HashMap<>();
        error.put("error", "Ошибка валидации параметров");
        error.put("message", e.getConstraintViolations().iterator().next().getMessage());
        return error;
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public Map<String, String> handleHandlerMethodValidationException(HandlerMethodValidationException e) {
        log.error("Ошибка валидации параметров: {}", e.getMessage());
        Map<String, String> error = new HashMap<>();
        error.put("error", "Ошибка валидации параметров");

        String message = "Неверные параметры запроса";
        e.getAllErrors();
        if (!e.getAllErrors().isEmpty()) {
            message = e.getAllErrors().getFirst().getDefaultMessage();
            if (message == null || message.isEmpty()) {
                message = "Пароль должен содержать минимум 6 символов";
            }
        }
        error.put("message", message);
        return error;
    }

    @ExceptionHandler(AccessDeniedException.class)
    public Map<String, Object> handleAccessDenied(AccessDeniedException e) {
        log.error("Доступ запрещён: {}", e.getMessage());
        Map<String, Object> error = new HashMap<>();
        error.put("error", "Недостаточно прав");
        error.put("message", "Ваш тариф не позволяет использовать эту функцию. Повысьте уровень подписки");
        return error;
    }
}
