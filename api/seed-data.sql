-- ====================================================================
-- Seed Data cho E-commerce — dùng để test sau khi mất DB
-- ====================================================================
-- Chạy file này SAU KHI Spring Boot đã start lần đầu (Hibernate đã tạo
-- schema). Nếu chưa start app:
--   1. Đảm bảo application.yaml có ddl-auto: update
--   2. Start app → Hibernate tạo tables
--   3. Stop app
--   4. Chạy script này
--   5. Start app lại
--
-- HOẶC chạy lúc app đang chạy cũng được (chỉ cần tables tồn tại).
-- ====================================================================

USE ecommerce;

-- Reset (nếu có data cũ — uncomment nếu cần xoá sạch trước)
-- DELETE FROM product_images;
-- DELETE FROM product_variants;
-- DELETE FROM products;
-- DELETE FROM categories;
-- DELETE FROM users;

-- ====================================================================
-- 1. USERS — password "123456" cho tất cả (BCrypt hash)
-- ====================================================================
-- Hash sinh từ Spring BCryptPasswordEncoder.encode("123456") với strength=10
-- Note: BCrypt mỗi lần gen ra hash khác do salt random — đây là 1 hash hợp lệ

INSERT INTO users (email, password, full_name, phone, role, status, address, created_at)
VALUES
    ('admin@test.com',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'Admin User', '0900000001', 'ADMIN', 'ACTIVE', 'Hà Nội', NOW()),

    ('seller1@test.com',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'Seller One', '0900000002', 'SELLER', 'ACTIVE', 'Hồ Chí Minh', NOW()),

    ('seller2@test.com',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'Seller Two', '0900000003', 'SELLER', 'ACTIVE', 'Đà Nẵng', NOW()),

    ('customer@test.com',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'Customer User', '0900000004', 'CUSTOMER', 'ACTIVE', 'Cần Thơ', NOW());

-- ====================================================================
-- 2. CATEGORIES — nested structure
-- ====================================================================

-- Root categories
INSERT INTO categories (name, slug, description, status) VALUES
    ('Thời trang', 'thoi-trang', 'Danh mục thời trang tổng hợp', 'ACTIVE'),
    ('Điện tử', 'dien-tu', 'Đồ điện tử, công nghệ', 'ACTIVE'),
    ('Sách', 'sach', 'Sách & văn phòng phẩm', 'ACTIVE');

-- Sub categories
SET @thoi_trang_id = (SELECT id FROM categories WHERE slug = 'thoi-trang');
SET @dien_tu_id = (SELECT id FROM categories WHERE slug = 'dien-tu');

INSERT INTO categories (name, slug, description, status, parent_id) VALUES
    ('Thời trang nam', 'thoi-trang-nam', 'Quần áo nam', 'ACTIVE', @thoi_trang_id),
    ('Thời trang nữ', 'thoi-trang-nu', 'Quần áo nữ', 'ACTIVE', @thoi_trang_id),
    ('Điện thoại', 'dien-thoai', 'Smartphone, feature phone', 'ACTIVE', @dien_tu_id),
    ('Laptop', 'laptop', 'Máy tính xách tay', 'ACTIVE', @dien_tu_id);

-- ====================================================================
-- 3. PRODUCTS — vài product mẫu thuộc các seller
-- ====================================================================

SET @seller1_id = (SELECT id FROM users WHERE email = 'seller1@test.com');
SET @seller2_id = (SELECT id FROM users WHERE email = 'seller2@test.com');
SET @cat_thoi_trang_nam = (SELECT id FROM categories WHERE slug = 'thoi-trang-nam');
SET @cat_dien_thoai = (SELECT id FROM categories WHERE slug = 'dien-thoai');
SET @cat_laptop = (SELECT id FROM categories WHERE slug = 'laptop');

-- Seller 1 sở hữu
INSERT INTO products (name, slug, sku, price, description, stock, status, category_id, seller_id, created_at) VALUES
    ('Nike Air Force 1', 'nike-air-force-1', 'NIKE-AF1-001',
     2500000.00, 'Giày Nike Air Force 1 cổ điển', 50, 'ACTIVE',
     @cat_thoi_trang_nam, @seller1_id, NOW()),

    ('iPhone 15 Pro', 'iphone-15-pro', 'IP15PRO-001',
     30000000.00, 'iPhone 15 Pro 256GB', 20, 'ACTIVE',
     @cat_dien_thoai, @seller1_id, NOW());

-- Seller 2 sở hữu
INSERT INTO products (name, slug, sku, price, description, stock, status, category_id, seller_id, created_at) VALUES
    ('MacBook Pro M3', 'macbook-pro-m3', 'MBP-M3-001',
     45000000.00, 'MacBook Pro 14 inch M3 16GB/512GB', 10, 'ACTIVE',
     @cat_laptop, @seller2_id, NOW()),

    ('Adidas Stan Smith', 'adidas-stan-smith', 'ADIDAS-SS-001',
     1800000.00, 'Giày Adidas Stan Smith trắng xanh', 30, 'ACTIVE',
     @cat_thoi_trang_nam, @seller2_id, NOW());

-- ====================================================================
-- 4. PRODUCT VARIANTS — vài variant mẫu
-- ====================================================================

SET @nike_id = (SELECT id FROM products WHERE slug = 'nike-air-force-1');
SET @adidas_id = (SELECT id FROM products WHERE slug = 'adidas-stan-smith');

INSERT INTO product_variants (size, color, sku, price, stock, product_id) VALUES
    -- Nike variants
    ('40', 'Đen', 'NIKE-AF1-40-BLK', 2500000.00, 10, @nike_id),
    ('41', 'Đen', 'NIKE-AF1-41-BLK', 2500000.00, 8, @nike_id),
    ('42', 'Đen', 'NIKE-AF1-42-BLK', 2500000.00, 5, @nike_id),
    ('40', 'Trắng', 'NIKE-AF1-40-WHT', 2500000.00, 12, @nike_id),

    -- Adidas variants
    ('39', 'Trắng', 'ADIDAS-SS-39-WHT', 1800000.00, 15, @adidas_id),
    ('40', 'Trắng', 'ADIDAS-SS-40-WHT', 1800000.00, 10, @adidas_id);

-- ====================================================================
-- DONE — verify
-- ====================================================================

SELECT 'Users created:' AS info, COUNT(*) AS cnt FROM users
UNION ALL
SELECT 'Categories:', COUNT(*) FROM categories
UNION ALL
SELECT 'Products:', COUNT(*) FROM products
UNION ALL
SELECT 'Variants:', COUNT(*) FROM product_variants;
