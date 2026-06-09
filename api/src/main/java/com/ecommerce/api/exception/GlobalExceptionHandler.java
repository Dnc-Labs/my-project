package com.ecommerce.api.exception;

import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
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
@Slf4j
public class GlobalExceptionHandler {
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
    public ResponseEntity<BaseResponse<Void>> handleInvalidFileException(InvalidFileException e) {
        // 400 client error (file sai) → warn, không error. Message do mình kiểm soát nên an toàn trả client.
        log.warn("Invalid file request: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(BaseResponse.error(e.getMessage()));
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<BaseResponse<Void>> handleObjectOptimisticLockingFailureException(ObjectOptimisticLockingFailureException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(BaseResponse.error("This record was modified by someone else. Please reload and try again."));
    }

    // Lưới an toàn cho TOCTOU race: existsBy có thể lọt khe (2 request đồng thời cùng vượt check)
    // → DB unique constraint chặn lại → ném DataIntegrityViolationException. Đây là trường hợp dự
    // kiến được, không phải bug server → warn (không error). Message KHÔNG dùng e.getMessage() vì
    // lộ chi tiết SQL/tên constraint cho client.
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<BaseResponse<Void>> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        log.warn("Data integrity violation (possible duplicate/constraint): {}", e.getMostSpecificCause().getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(BaseResponse.error("A record with the same unique value already exists."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<Void>> handleException(Exception e) {
        log.error(e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(BaseResponse.error("Internal Server Error"));
    }
}
