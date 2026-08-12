-- V3: Review & Rating (ROADMAP 6.1)
--
-- Migration đầu tiên cho một FEATURE MỚI (V1 là baseline, V2 là dọn rác).
-- Từ khi bật Flyway + ddl-auto:validate, Hibernate KHÔNG tự tạo bảng nữa —
-- không có file này thì app không boot: SchemaManagementException: missing table [reviews].
--
-- Gồm 2 thay đổi:
--   1. Tạo bảng `reviews`
--   2. Thêm 2 cột denormalized `average_rating` + `review_count` vào `products`
--
-- Vì sao denormalize thay vì tính AVG mỗi lần đọc: tỉ lệ đọc/ghi của rating khoảng
-- 10.000:1 → đặt chi phí lên đường GHI (hiếm) thay vì đường ĐỌC (nóng). Đổi lại phải
-- tự giữ đồng bộ 2 nguồn dữ liệu — xem ProductRepository.recomputeRating().

CREATE TABLE `reviews`(
  `id` bigint NOT NULL AUTO_INCREMENT,
  `rating` INT NOT NULL,
  `comment` TEXT,
  `status` enum('ACTIVE','DELETED') NOT NULL DEFAULT 'ACTIVE',
  `user_id` bigint NOT NULL,
  `product_id` bigint NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  `version` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),

  -- 1 user review 1 sản phẩm đúng 1 lần. Phải chặn Ở DB, không chỉ ở service:
  -- 2 request song song đều qua được check "chưa review" rồi cùng INSERT (TOCTOU).
  -- Vi phạm → DataIntegrityViolationException → 409 (pattern cụm 2.4).
  --
  -- Thứ tự cột cố ý đặt user_id TRƯỚC: index này phục vụ luôn query
  -- "review của tôi" (GET /api/reviews/me) nhờ leftmost-prefix, khỏi tạo index riêng.

  UNIQUE KEY `uk_review_user_product` (`user_id`, `product_id`),

  -- Phục vụ 2 query nóng nhất: list review theo product, và AVG/COUNT lúc recompute.
  KEY `idx_review_product_status` (`product_id`, `status`),

  CONSTRAINT `fk_review_product` FOREIGN KEY (`product_id`) REFERENCES `products`(`id`),
  CONSTRAINT `fk_review_user`    FOREIGN KEY (`user_id`)    REFERENCES `users`(`id`),

  -- Chặn rating rác ở tầng cuối cùng. @Min/@Max ở DTO chỉ chặn đường HTTP;
  -- CHECK chặn cả script chạy tay, migration sau này, và bug ở tầng service.
  CONSTRAINT `ck_review_rating`  CHECK (`rating` BETWEEN 1 AND 5)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- decimal(3,2): tối đa 9.99 — thừa cho thang [1.00, 5.00].
-- DEFAULT 0 vừa là giá trị khởi tạo cho sản phẩm cũ, vừa là sentinel "chưa có review"
-- (an toàn vì rating hợp lệ tối thiểu là 1 → 0 không thể là trung bình thật).
ALTER TABLE `products`
  ADD COLUMN `average_rating` decimal(3,2) NOT NULL DEFAULT 0.00,
  ADD COLUMN `review_count`   int          NOT NULL DEFAULT 0;
