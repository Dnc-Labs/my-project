package com.ecommerce.api.exception;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.ecommerce.api.dto.response.BaseResponse;

/**
 * Xử lý exception tập trung cho toàn bộ API.
 *
 * @RestControllerAdvice = @ControllerAdvice + @ResponseBody
 * Giống error-handling middleware trong Express:
 * app.use((err, req, res, next) => { res.status(500).json({...}) })
 * Nhưng thay vì 1 middleware, Spring cho phép bắt TỪNG loại exception riêng.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<BaseResponse<Void>> handleResourceNotFoundException(ResourceNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(BaseResponse.error(e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<Void>> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(BaseResponse.error(message));
    }

    @ExceptionHandler(InvalidUserOrPassword.class)
    public ResponseEntity<BaseResponse<Void>> handleInvalidUserOrPassword(InvalidUserOrPassword e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(BaseResponse.error(e.getMessage()));
    }

    @ExceptionHandler(InvalidToken.class)
    public ResponseEntity<BaseResponse<Void>> handleInvalidToken(InvalidToken e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(BaseResponse.error(e.getMessage()));
    }

    @ExceptionHandler({AccessDeniedException.class, AuthorizationDeniedException.class})
    public ResponseEntity<BaseResponse<Void>> handleAccessDenied(Exception e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(BaseResponse.error("You do not have permission to perform this action"));
    }

    @ExceptionHandler(DuplicateResource.class)
    public ResponseEntity<BaseResponse<Void>> handleDuplicateResource(DuplicateResource e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(BaseResponse.error(e.getMessage()));
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<BaseResponse<Void>> handleBusinessRuleException(BusinessRuleException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(BaseResponse.error(e.getMessage()));
    }

    @ExceptionHandler(InvalidFileException.class)
    public ResponseEntity<BaseResponse<Void>> handeInvalidFileException(InvalidFileException e) {
        logger.error(e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(BaseResponse.error(e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<Void>> handleException(Exception e) {
        logger.error(e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(BaseResponse.error("Internal Server Error"));
    }
}
