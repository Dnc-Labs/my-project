# Handoff — Trạng thái dự án khi chuyển máy

> File này dành cho **bản thân tương lai** hoặc Claude trên máy mới — đọc file này trước khi bắt đầu phiên làm việc mới.

**Cập nhật lần cuối:** 2026-05-14

---

## 1. Phong cách làm việc với Claude

Phong cách hướng dẫn đầy đủ nằm ở **`CLAUDE.md`** (root của repo, đã commit git). Mở file đó đọc đầu tiên. Tóm tắt:

- Giao tiếp tiếng Việt có dấu, giữ keyword kỹ thuật bằng tiếng Anh
- Claude đóng vai **senior Java developer** đồng hành
- Quy trình: giới thiệu lý thuyết → hỏi cách triển khai → tạo skeleton → tự code → review → docs
- **Deep teaching**: luôn giải thích internal mechanism, so sánh với Express/Node.js, dùng ASCII diagram
- Docs phải **copy nguyên văn Q&A**, KHÔNG tóm tắt
- Clean TODO scaffolding trước khi tạo docs/commit, chỉ giữ comment nói WHY

---

## 2. Tiến độ hiện tại

### Đã hoàn thành (theo thứ tự gần nhất → xa nhất)
- ✅ **3.2.5 Phần 1: Setup Lombok + MapStruct** (vừa xong) — `docs/3.2.5-lombok-mapstruct-setup.md`
- ✅ **Khởi đầu refactor User module** — `User.java` + `CreateUserRequest.java` đã chuyển sang Lombok
- ✅ **3.2 Buổi B Phần 2: Multi-image upload + deep dive File I/O** — `docs/3.2-product-image-upload.md`
- ✅ **3.2 Buổi B Phần 1: ProductVariant + JPA Lifecycle** — `docs/3.2-product-variant.md`
- ✅ **3.2 Buổi A: Product CRUD + ownership authorization** — `docs/3.2-product-basic.md`
- ✅ **3.1: Category** — `docs/3.1-category.md`
- ✅ **2.2: Authentication / Authorization (JWT + Spring Security)** — `docs/2.2-authentication-authorization.md`
- ✅ **2.1: User CRUD** — `docs/2.1-user-crud.md`
- ✅ **0.x → 1.0: Spring Core DI, Spring Boot, JPA, Multi-threading, khởi tạo dự án**

### Đang dang dở — **bắt đầu từ đây khi quay lại**

**Bài 3.2.5 Phần 2: Refactor module-by-module bằng Lombok + MapStruct**

Setup dependency + plugin đã xong (xem `docs/3.2.5-lombok-mapstruct-setup.md`). Build pass.

**Thứ tự đã chốt:**
1. **User module** ← đang làm dở
2. Category module
3. Product + ProductVariant + ProductImage module (phức tạp nhất)

**Trạng thái module User:**
- ✅ `User.java` (entity) — đã chuyển `@Getter @Setter @NoArgsConstructor`
- ✅ `CreateUserRequest.java` (DTO) — đã chuyển `@Data`
- ❌ Các DTO khác của User (`UserResponse`, `UpdateUserRequest`, ...) — chưa refactor
- ❌ `UserService` — chưa thêm `@RequiredArgsConstructor @Slf4j`, chưa replace constructor injection viết tay
- ❌ `UserMapper` interface (MapStruct) — chưa tạo. Sẽ replace các method `toEntity` / `fromEntity` hiện tại trong service/DTO.

---

## 3. Bước tiếp theo cụ thể (action items)

Khi mở Claude trên máy mới, paste prompt sau để có context ngay:

```
Hi Claude, đọc docs/HANDOFF.md để biết trạng thái dự án.
Hôm nay tiếp tục bài 3.2.5 Phần 2 — refactor module User.
Đã xong User.java + CreateUserRequest.java, giờ refactor tiếp
các DTO còn lại + UserService + tạo UserMapper.
```

**Checklist hoàn thành module User:**
- [ ] Quét tất cả file trong `api/src/main/java/com/ecommerce/api/dto/` liên quan User → chuyển sang Lombok
- [ ] `UserService.java`: thêm `@RequiredArgsConstructor @Slf4j`, đảm bảo các dependency là `final`, xoá constructor viết tay
- [ ] Tạo `UserMapper.java` (interface `@Mapper`) replace các method convert Entity ↔ DTO
- [ ] Verify: `./mvnw clean compile` pass + warning `mapstruct.unmappedTargetPolicy` biến mất (vì giờ đã có `@Mapper`)
- [ ] Test runtime: gọi API tạo user / lấy user, verify response đúng
- [ ] Cập nhật docs `3.2.5-lombok-mapstruct-setup.md` thành phần 2, hoặc tạo file mới `3.2.5-refactor-user-module.md`

**Sau đó:** lặp lại checklist trên cho Category, rồi Product+Variant+Image.

**Sau bài 3.2.5:** sang **3.3 — Tìm kiếm & Lọc sản phẩm** (search, filter, `Pageable`, có thể cả Elasticsearch).

---

## 4. Điểm cần lưu ý kỹ thuật

### Warning MapStruct hiện tại
```
The following options were not recognized by any processor:
[mapstruct.unmappedTargetPolicy, mapstruct.defaultComponentModel]
```
→ Không phải bug. Do chưa có file nào dùng `@Mapper` → processor không khởi động. Khi tạo `UserMapper` đầu tiên, warning sẽ biến mất.

### Lý do KHÔNG dùng `@Data` cho JPA entity
- `@EqualsAndHashCode` include tất cả field → vòng lặp vô hạn khi entity có quan hệ 2 chiều
- `@ToString` include tất cả field → trigger lazy-load → N+1 query

→ Dùng `@Getter @Setter @NoArgsConstructor` rời cho **mọi** JPA entity, kể cả entity tưởng đơn giản (vì sau này có thể thêm quan hệ).

### Pattern Service refactor
```java
// Trước
@Service
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    public UserService(UserRepository ur) { this.userRepository = ur; }
}

// Sau
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    // constructor + log được Lombok tự sinh
}
```

### Pattern Mapper (MapStruct)
```java
@Mapper  // componentModel=spring đã set qua compilerArg, không cần ghi ở đây
public interface UserMapper {
    UserResponse toResponse(User user);
    User toEntity(CreateUserRequest request);
}
```
→ MapStruct tự sinh `UserMapperImpl` (Spring bean) → inject vào service như repository thường.

---

## 5. Các file/folder cần biết

- **`CLAUDE.md`** — quy tắc làm việc (bắt buộc đọc đầu phiên)
- **`docs/`** — toàn bộ docs lý thuyết, đặt tên theo ROADMAP (`3.2.5-...`)
- **`api/pom.xml`** — dependencies + plugin Maven
- **`api/src/main/java/com/ecommerce/api/`** — code Java
  - `entity/` — JPA entities
  - `dto/request/`, `dto/response/` — DTO
  - `repository/` — Spring Data JPA
  - `service/` — business logic
  - `controller/` — REST endpoints
  - `security/` — JWT + Spring Security
  - `config/` — Spring config beans
  - `demo/` — code thử nghiệm, **không touch khi clean TODO**

---

## 6. Lệnh hay dùng

```bash
# Build + verify
cd api && ./mvnw clean compile

# Run app
cd api && ./mvnw spring-boot:run

# Run tests
cd api && ./mvnw test

# Xem JAR cuối có những lib gì (verify scope đúng)
cd api && ./mvnw package -DskipTests
unzip -l target/api-0.0.1-SNAPSHOT.jar | grep "BOOT-INF/lib"
```
