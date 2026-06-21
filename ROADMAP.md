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

### 0.3 - Spring Data JPA & Database ✅
- [x] Hiểu **ORM**, **JPA**, **Hibernate** là gì (so sánh với Sequelize/Prisma bên Node.js)
- [x] Kết nối **MySQL/PostgreSQL**
- [x] Tạo `@Entity`, viết `Repository` interface
- [x] Hiểu **relationship mapping**: `@OneToMany`, `@ManyToOne`, `@ManyToMany`

### 0.4 - Multi-threading cơ bản ✅
- [x] Hiểu **Thread**, **Runnable**, **Callable** trong Java
- [x] Hiểu **Thread Pool**, **ExecutorService**
- [x] Hiểu tại sao Spring Boot mặc định là **multi-threaded** (khác với Node.js single-threaded)
- [x] Hiểu `@Async`, cách Spring xử lý request đồng thời
- [x] Hiểu **thread-safety**, **race condition**, **synchronized**

---

## Giai đoạn 1: Khởi tạo dự án E-commerce (1 tuần) ✅

- [x] Khởi tạo project Spring Boot (Web, JPA, Security, Validation, MySQL Driver)
- [x] Thiết kế **database schema** (ERD) cho E-commerce
- [x] Cấu hình kết nối database, Hibernate DDL auto
- [x] Tạo cấu trúc package: `controller`, `service`, `repository`, `entity`, `dto`, `config`, `exception`
- [x] Cấu hình **global exception handling** (`@ControllerAdvice`)
- [x] Cấu hình **API response format** chuẩn (BaseResponse)

---

## Giai đoạn 2: Quản lý người dùng & Xác thực (2 tuần)

### 2.1 - User Entity & CRUD ✅
- [x] Tạo `User` entity (id, email, password, fullName, phone, role, status, address...)
- [x] Tạo `UserRepository`, `UserService`, `UserController`
- [x] API: đăng ký, xem profile, cập nhật profile

### 2.2 - Authentication & Authorization ✅
- [x] Hiểu **Spring Security** filter chain
- [x] Tích hợp **JWT** (access token + refresh token)
- [x] API: đăng nhập, đăng xuất, refresh token
- [x] Phân quyền: `ADMIN`, `SELLER`, `CUSTOMER` bằng `@PreAuthorize`
- [x] Mã hóa password bằng **BCrypt**

---

## Giai đoạn 3: Quản lý sản phẩm (2 tuần)

### 3.1 - Category ✅
- [x] `Category` entity (hỗ trợ **nested category** - danh mục cha/con)
- [x] CRUD API cho category (chỉ ADMIN)

### 3.2 - Product ✅
- [x] `Product` entity (name, description, price, stock, status, category...)
- [x] CRUD API cho product (SELLER tạo/sửa, tất cả xem)
- [x] **Product variants** (size, color...) - `ProductVariant` entity
- [x] Upload **product images** (lưu file hoặc cloud storage)

### 3.2.5 - Refactor: Lombok + MapStruct ✅
- [x] Thay boilerplate getter/setter/constructor bằng Lombok
- [x] Thay mapping thủ công bằng MapStruct (Entity ↔ DTO)

### 3.2.6 - Buổi Cleanup Chuyên Sâu (production-grade hardening) ✅
> Tài liệu đầy đủ: `docs/3.2.6-production-cleanup.md` (lý thuyết + Q&A verbatim)
- [x] **Cụm 1** — Fix bug Category circular (walk-up + depth limit)
- [x] **Cụm 2.1** — `@Transactional` toàn bộ service + tắt OSIV
- [x] **Cụm 2.3** — Optimistic Locking `@Version`
- [x] **Cụm 2.4** — TOCTOU race → `DataIntegrityViolationException` → 409
- [x] **Cụm 3** — BaseEntity + JPA Auditing
- [x] **Cụm 3.6** — `@Slf4j` audit logging
- [x] **Cụm 4** — Security exception flow (#8) + custom exception (#7) + `@AuthenticationPrincipal` (#9)
- [x] **Cụm 5** — Compensating transaction cho `uploadImage` (store-then-record)
- [ ] _(Dời sang buổi Cleanup #2 ở Giai đoạn 4 — xem mục 4.0)_

### 3.3 - Tìm kiếm & Lọc sản phẩm
- [x] Tìm kiếm theo tên, mô tả
- [x] Lọc theo category, khoảng giá (rating defer — chờ Review entity ở 6.1)
- [x] **Pagination** và **sorting** với `Pageable`
- [ ] (Nâng cao) Tích hợp **Elasticsearch** cho full-text search

---

## Giai đoạn 4: Giỏ hàng & Đặt hàng (2 tuần)

### 4.0 - Buổi Cleanup #2 (carry-over từ 3.2.6 + hardening mới)
> Các mục production-grade chưa làm ở 3.2.6, gom làm 1 buổi trong Giai đoạn 4.
- [ ] **Cụm 2.3b** — Full optimistic lock (think-time): client gửi `version` (thêm vào Request + Response DTO) → bảo vệ lost-update kiểu stale-client, không chỉ race chồng lấn
- [ ] **Cụm 5b** — `deleteImage` dùng `TransactionSynchronization.afterCommit()` (làm khi migrate S3)
- [ ] Tách `StorageException` (500) khỏi `InvalidFileException` (400) — `LocalStorageService` lỗi I/O ghi file đang trả nhầm 400
- [ ] **Flyway/Liquibase** thay `ddl-auto: update` (tránh schema drift, backfill có kiểm soát)
- [ ] Password validation regex (hoặc lib passay)
- [ ] Tách `ProductListItemResponse` vs `ProductDetailResponse` (tránh over-fetching list view)
- [ ] Dọn property `app.name/version/description` thừa trong `application.yaml`

### 4.1 - Shopping Cart ✅ (code xong, test E2E để sau)
> Tài liệu: `docs/4.1-shopping-cart.md` (lý thuyết + Q&A verbatim, gồm deep-dive `reduce` và N+1 query)
- [x] `Cart`, `CartItem` entity (owning/inverse side, BaseEntity, unique constraint dòng trùng)
- [x] API: thêm/xóa/cập nhật sản phẩm trong giỏ, xem giỏ hàng (5 endpoint)
- [x] Live price + merge cộng dồn + check stock mềm + ownership 404
- [x] Chống N+1 khi xem giỏ bằng `@EntityGraph` (`findByUserIdWithItems`)
- [ ] Test E2E (Postman group "7. Cart" đã thêm — chạy sau)
- [ ] Xử lý **concurrency/optimistic lock think-time** khi cập nhật giỏ (gom vào buổi Cleanup #2 — mục 4.0)

### 4.2 - Order ✅ (code xong, test E2E + email để sau)
> Tài liệu: `docs/4.2-order.md` (deep-dive: 3 cách chống oversell, `@Lock` syntax, isolation levels, stale-read gotcha, multi-node)
- [x] `Order`, `OrderItem` entity (snapshot price/total, extends BaseEntity)
- [x] **Order status flow**: PENDING → CONFIRMED → SHIPPING → DELIVERED / CANCELLED (state machine + phân quyền)
- [x] API: đặt hàng từ giỏ, xem list/detail đơn, đổi status, hủy đơn (hoàn stock khi cancel)
- [x] Xử lý **trừ tồn kho** an toàn — **pessimistic lock** (`em.refresh(PESSIMISTIC_WRITE)`) chống oversell, an toàn đa node
- [ ] Test E2E (Postman group "9. Order" đã thêm — chạy sau)
- [ ] Gửi **email xác nhận** đơn hàng (dùng `@Async` + JavaMailSender) — chưa làm

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
