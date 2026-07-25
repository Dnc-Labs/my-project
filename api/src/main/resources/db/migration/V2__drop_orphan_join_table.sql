-- V2: Dọn bảng join mồ côi `carts_cart_items`.
--
-- Drift do ddl-auto:update để lại: Cart.cartItems từng là @OneToMany unidirectional
-- (không mappedBy) → Hibernate tạo join table. Sau đổi sang @OneToMany(mappedBy="cart")
-- → FK dời về cart_items.cart_id, nhưng ddl-auto KHÔNG xóa join table cũ → nó nằm lại rỗng.
--
-- Đây là migration thật đầu tiên: thay đổi schema có version + review được, thay cho việc
-- gõ DROP bằng tay (không ai nhớ đã chạy ở môi trường nào).

DROP TABLE IF EXISTS carts_cart_items;
