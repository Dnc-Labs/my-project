package com.ecommerce.api.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ReviewResponse {

    // KHÔNG trả `status` ra ngoài: review DELETED không bao giờ lọt vào response nên với
    // client field này luôn = ACTIVE → nhiễu, và còn hé lộ cơ chế kiểm duyệt.
    private Long id;
    private Integer rating;
    private String comment;
    private Long userId;
    private String userName;
    private Long productId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
