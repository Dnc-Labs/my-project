package com.ecommerce.api.enums;

/**
 * Trạng thái review.
 * <p>
 * DELETED = xoá mềm bởi ADMIN (kiểm duyệt nội dung). Bản ghi vẫn nằm lại để audit,
 * nên MỌI query hiển thị bắt buộc lọc status = ACTIVE — quên một chỗ là lộ nội dung đã gỡ.
 */
public enum ReviewStatus {
    ACTIVE,
    DELETED
}

