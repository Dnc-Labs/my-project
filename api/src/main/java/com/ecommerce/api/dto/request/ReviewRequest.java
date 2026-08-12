package com.ecommerce.api.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Dùng chung cho CẢ tạo mới VÀ sửa review — hai thao tác nhận đúng cùng một bộ field.
 * <p>
 * Cố ý lệch khỏi convention "CreateXxxRequest / UpdateXxxRequest" của các module trước:
 * ở đó 2 DTO khác nhau thật (create có field mà update không cho sửa). Ở đây tách ra sẽ
 * là 2 class y hệt nhau — trùng lặp mà không mang thêm thông tin nào. Khi nào 2 luồng
 * thực sự khác field thì mới tách (productId không nằm ở đây vì lấy từ path variable).
 */
@Getter
@Setter
public class ReviewRequest {

    // Integer chứ không phải int: kiểu nguyên thuỷ tự nhận 0 khi client không gửi field,
    // lọt qua @NotNull và chỉ bị @Min(1) chặn với message khó hiểu.
    @NotNull
    @Min(1)
    @Max(5)
    private Integer rating;

    @Size(max=2000)
    private String comment;
}
