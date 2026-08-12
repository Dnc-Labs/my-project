package com.ecommerce.api.enums;

/**
 * Kết quả kiểm tra "user này có được review sản phẩm này không".
 * <p>
 * Tồn tại để một bộ luật phục vụ được HAI nhu cầu khác nhau mà không phải chép lại:
 * createReview cần biết LÝ DO từ chối (để ném đúng exception + thông điệp),
 * canReview chỉ cần biết CÓ/KHÔNG. Trả về kết quả thay vì ném exception giúp
 * canReview không phải bắt exception làm luồng điều khiển.
 */
public enum ReviewEligibility {
    OK,
    ALREADY_REVIEWED,
    NOT_PURCHASED
}
