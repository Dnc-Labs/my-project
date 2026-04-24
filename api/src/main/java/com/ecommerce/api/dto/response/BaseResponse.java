package com.ecommerce.api.dto.response;

import org.springframework.http.ResponseEntity;

/**
 * Response format chuẩn cho toàn bộ API.
 *
 * Mọi API đều trả về cùng 1 format:
 * {
 *   "success": true/false,
 *   "message": "Thành công" / "Lỗi gì đó",
 *   "data": { ... } hoặc null
 * }
 *
 * Dùng Generic <T> để data có thể là bất kỳ kiểu nào (User, Product, List<Order>...)
 */
public class BaseResponse<T> {
    private Boolean success;
    private String message;
    private T data;



    public static <T> BaseResponse<T> success(T data, String message) {
        return new BaseResponse<T>(true, message, data);
    }
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<T>(true, "Success", data);
    }

    public static <T> BaseResponse<T> error(String message) {
      return new BaseResponse<T>(false, message, null);
    };

    public BaseResponse(Boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
