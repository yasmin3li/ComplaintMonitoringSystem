package com.myapp.complaints.exceptionHandller;

import com.myapp.complaints.dto.ApiResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Optional;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // 1. معالجة الاستثناء المخصص (ApiException)
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponseDto<?>> handleApiException(ApiException ex) {
        log.warn("API Error [{}]: {}", ex.getStatus(), ex.getMessage());
        ApiResponseDto<?> response = new ApiResponseDto<>(
                false,
                ex.getMessage(),
                Optional.empty()
        );
        return new ResponseEntity<>(response, ex.getStatus());
    }

    // 2. معالجة أخطاء التحقق (Validation Errors) مثل @NotEmpty و @Valid المستخدمة في الـ DTOs لديك
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseDto<?>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        log.info("Validation Failed: {}", errorMessage);

        ApiResponseDto<?> response = new ApiResponseDto<>(
                false,
                "خطأ في البيانات المدخلة: " + errorMessage,
                Optional.empty()
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // 3. معالجة أي استثناءات غير متوقعة (Runtime Exceptions)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponseDto<?>> handleRuntimeException(RuntimeException ex) {

        log.error("Unexpected Runtime Error: ", ex);

        ApiResponseDto<?> response = new ApiResponseDto<>(
                false,
                "  حدث خطأ غير متوقع:  " + ex.getMessage(),
                Optional.empty()
        );
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

//    @ExceptionHandler(DisabledException.class)
//    public ResponseEntity<ApiResponseDto<?>> handleDisabledAccount(DisabledException ex) {
//        return new ResponseEntity<>(
//                new ApiResponseDto<>(false, "حسابك  غير مفعل (PENDING)", Optional.empty()),
//                HttpStatus.FORBIDDEN
//        );
//    }

}