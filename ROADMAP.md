# Lộ trình học Java Spring Boot - E-commerce API

## Giai đoạn 0: Nền tảng Spring (1-2 tuần)

### 0.1 - Spring Core & Dependency Injection ✅
- [x] Hiểu khái niệm **IoC Container**, **Bean**, **Dependency Injection** (Constructor Injection vs Field Injection)
- [x] So sánh với cách quản lý dependency trong Node.js (require/import) để hiểu sự khác biệt
- [x] Tạo project Spring Boot đầu tiên bằng **Spring Initializr**
- [x] Viết các Bean đơn giản, sử dụng `@Component`, `@Service`, `@Repository`, `@Configuration`

### 0.2 - Spring Boot Basics ✅
- [x] Hiểu cấu trúc project: `controller` → `service` → `repository` → `entity`
- [x] Hiểu `application.properties` / `application.yml`
- [x] Viết REST API đơn giản (CRUD) với `@RestController`, `@GetMapping`, `@PostMapping`...
- [x] Hiểu **Bean lifecycle**, **Bean scope** (Singleton vs Prototype)

### 0.3 - Spring Data JPA & Database
- [ ] Hiểu **ORM**, **JPA**, **Hibernate** là gì (so sánh với Sequelize/Prisma bên Node.js)
- [ ] Kết nối **MySQL/PostgreSQL**
- [ ] Tạo `@Entity`, viết `Repository` interface
- [ ] Hiểu **relationship mapping**: `@OneToMany`, `@ManyToOne`, `@ManyToMany`

### 0.4 - Multi-threading cơ bản
- [ ] Hiểu **Thread**, **Runnable**, **Callable** trong Java
- [ ] Hiểu **Thread Pool**, **ExecutorService**
- [ ] Hiểu tại sao Spring Boot mặc định là **multi-threaded** (khác với Node.js single-threaded)
- [ ] Hiểu `@Async`, cách Spring xử lý request đồng thời
- [ ] Hiểu **thread-safety**, **race condition**, **synchronized**

---

## Giai đoạn 1: Khởi tạo dự án E-commerce (1 tuần)

- [ ] Khởi tạo project Spring Boot (Web, JPA, Security, Validation, MySQL Driver)
- [ ] Thiết kế **database schema** (ERD) cho E-commerce
- [ ] Cấu hình kết nối database, Hibernate DDL auto
- [ ] Tạo cấu trúc package: `controller`, `service`, `repository`, `entity`, `dto`, `config`, `exception`
- [ ] Cấu hình **global exception handling** (`@ControllerAdvice`)
- [ ] Cấu hình **API response format** chuẩn (BaseResponse)

---

## Giai đoạn 2: Quản lý người dùng & Xác thực (2 tuần)

### 2.1 - User Entity & CRUD
- [ ] Tạo `User` entity (id, email, password, fullName, phone, role, status, address...)
- [ ] Tạo `UserRepository`, `UserService`, `UserController`
- [ ] API: đăng ký, xem profile, cập nhật profile

### 2.2 - Authentication & Authorization
- [ ] Hiểu **Spring Security** filter chain
- [ ] Tích hợp **JWT** (access token + refresh token)
- [ ] API: đăng nhập, đăng xuất, refresh token
- [ ] Phân quyền: `ADMIN`, `SELLER`, `CUSTOMER` bằng `@PreAuthorize`
- [ ] Mã hóa password bằng **BCrypt**

---

## Giai đoạn 3: Quản lý sản phẩm (2 tuần)

### 3.1 - Category
- [ ] `Category` entity (hỗ trợ **nested category** - danh mục cha/con)
- [ ] CRUD API cho category (chỉ ADMIN)

### 3.2 - Product
- [ ] `Product` entity (name, description, price, stock, status, category...)
- [ ] CRUD API cho product (SELLER tạo/sửa, tất cả xem)
- [ ] **Product variants** (size, color...) - `ProductVariant` entity
- [ ] Upload **product images** (lưu file hoặc cloud storage)

### 3.3 - Tìm kiếm & Lọc sản phẩm
- [ ] Tìm kiếm theo tên, mô tả
- [ ] Lọc theo category, khoảng giá, rating
- [ ] **Pagination** và **sorting** với `Pageable`
- [ ] (Nâng cao) Tích hợp **Elasticsearch** cho full-text search

---

## Giai đoạn 4: Giỏ hàng & Đặt hàng (2 tuần)

### 4.1 - Shopping Cart
- [ ] `Cart`, `CartItem` entity
- [ ] API: thêm/xóa/cập nhật sản phẩm trong giỏ, xem giỏ hàng
- [ ] Xử lý **concurrency** khi cập nhật giỏ hàng (áp dụng multi-threading)

### 4.2 - Order
- [ ] `Order`, `OrderItem` entity
- [ ] **Order status flow**: PENDING → CONFIRMED → SHIPPING → DELIVERED → CANCELLED
- [ ] API: đặt hàng từ giỏ, xem đơn hàng, hủy đơn
- [ ] Xử lý **trừ tồn kho** an toàn (optimistic locking / pessimistic locking)
- [ ] Gửi **email xác nhận** đơn hàng (dùng `@Async` + JavaMailSender)

---

## Giai đoạn 5: Thanh toán (1-2 tuần)

- [ ] Tích hợp **payment gateway** (VNPay / Stripe)
- [ ] Xử lý **callback/webhook** từ cổng thanh toán
- [ ] Quản lý trạng thái thanh toán
- [ ] Xử lý **idempotency** (tránh thanh toán trùng)

---

## Giai đoạn 6: Tính năng nâng cao (2-3 tuần)

### 6.1 - Review & Rating
- [ ] `Review` entity, API đánh giá sản phẩm
- [ ] Tính **average rating** cho sản phẩm

### 6.2 - Coupon & Discount
- [ ] `Coupon` entity (mã giảm giá, % hoặc cố định, thời hạn, giới hạn sử dụng)
- [ ] Áp dụng coupon khi đặt hàng

### 6.3 - Notification
- [ ] Gửi thông báo qua **email** / **WebSocket** (real-time)
- [ ] Thông báo trạng thái đơn hàng

### 6.4 - Dashboard cho Admin/Seller
- [ ] Thống kê doanh thu, đơn hàng, sản phẩm bán chạy
- [ ] API báo cáo theo ngày/tuần/tháng

---

## Giai đoạn 7: Tối ưu & Production-ready (2 tuần)

- [ ] **Caching** với Redis (cache sản phẩm, session...)
- [ ] **Rate limiting** cho API
- [ ] **Logging** chuẩn với SLF4J + Logback
- [ ] **API documentation** với Swagger/OpenAPI
- [ ] **Docker** hóa ứng dụng
- [ ] **Unit test** + **Integration test** (JUnit + Mockito)
- [ ] **CI/CD** cơ bản

---

## Tổng thời gian ước tính: ~12-14 tuần

> **Ghi chú**: Mỗi checkbox sẽ được đánh dấu `[x]` khi hoàn thành. Lộ trình có thể điều chỉnh trong quá trình học.
