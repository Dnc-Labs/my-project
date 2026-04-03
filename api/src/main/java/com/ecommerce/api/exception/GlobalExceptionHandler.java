package com.ecommerce.api.exception;

import com.ecommerce.api.dto.response.BaseResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Xử lý exception tập trung cho toàn bộ API.
 *
 * @RestControllerAdvice = @ControllerAdvice + @ResponseBody
 * Giống error-handling middleware trong Express:
 *   app.use((err, req, res, next) => { res.status(500).json({...}) })
 * Nhưng thay vì 1 middleware, Spring cho phép bắt TỪNG loại exception riêng.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // TODO: Xử lý ResourceNotFoundException → trả 404
    // - Bắt ResourceNotFoundException
    // - Trả về BaseResponse với success=false, message từ exception, data=null
    // - HTTP status: 404

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<BaseResponse> handleResourceNotFoundException(ResourceNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(BaseResponse.error(e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(BaseResponse.error(message));
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse> handleException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(BaseResponse.error("Internal Server Error"));

    }



}
