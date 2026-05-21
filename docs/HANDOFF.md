# Handoff — Trạng thái dự án khi chuyển máy

> File này dành cho **bản thân tương lai** hoặc Claude trên máy mới — đọc file này trước khi bắt đầu phiên làm việc mới.

**Cập nhật lần cuối:** 2026-05-21

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
- ✅ **3.2.5 Phần 3: Refactor module Category** (vừa xong) — `docs/3.2.5-refactor-category-module.md`
- ✅ **3.2.5 Phần 2: Refactor module User** — `docs/3.2.5-refactor-user-module.md`
- ✅ **3.2.5 Phần 1: Setup Lombok + MapStruct** — `docs/3.2.5-lombok-mapstruct-setup.md`
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
1. ✅ **User module** — đã xong, xem `docs/3.2.5-refactor-user-module.md`
2. ✅ **Category module** — đã xong, xem `docs/3.2.5-refactor-category-module.md`
3. **Product + ProductVariant + ProductImage module** ← TIẾP THEO (phức tạp nhất, có quan hệ 2 chiều)

**Pattern áp dụng (lặp lại y hệt cho Category & Product):**
- Entity: `@Getter @Setter @NoArgsConstructor` (KHÔNG `@Data` vì có thể có quan hệ 2 chiều)
- DTO request/response: `@Data`
- Service + Controller: `@RequiredArgsConstructor`, dependency `final`
- Tạo `*Mapper` interface với 3 method: `fromEntity`, `fromRequestDto`, `updateEntity(req, @MappingTarget entity)`
- Vì `unmappedTargetPolicy=ERROR` global → phải `@Mapping(target=..., ignore=true)` cho MỌI field entity không có trong request

**Lưu ý quan trọng từ User module (đã rút kinh nghiệm):**
- KHÔNG dùng `@AllArgsConstructor` trên Spring bean (chỉ dùng `@RequiredArgsConstructor`)
- Nếu request có field giống entity nhưng cần xử lý security (password) → BẮT BUỘC `@Mapping(ignore=true)` rồi service tự set
- Javadoc phải đặt TRƯỚC annotation
- Sau khi build pass, dọn TODO scaffolding, chỉ giữ comment WHY

**Lưu ý quan trọng từ Category module (mới rút kinh nghiệm):**
- `@BeanMapping(IGNORE)` **CHỈ** dùng cho method update có `@MappingTarget`, KHÔNG đặt ở method create (vô nghĩa, gây nhầm lẫn)
- MapStruct **TỰ ĐỆ QUY** cho self-reference (`List<Category> children` → `List<CategoryResponse> children`) — chỉ cần có sẵn method map cùng kiểu
- Field khác type giữa request/entity (`parentId: Long` ↔ `parent: Category`) → mapper `ignore`, service tự `findById` + set (Option A)
- Đặt tên method mapper ngắn theo pattern: `fromEntity` / `fromRequestDto` / `updateEntity`, KHÔNG lặp tên type trong method name
- Validate-then-mutate là **purist**, mutate-then-validate-then-set rời cho field cần validate là **pragmatic** — cả 2 đều OK miễn có `@Transactional` + `RuntimeException`

---

## 3. Bước tiếp theo cụ thể (action items)

Khi mở Claude trên máy mới, paste prompt sau để có context ngay:

```
Hi Claude, đọc docs/HANDOFF.md để biết trạng thái dự án.
Hôm nay tiếp tục bài 3.2.5 Phần 2 — sang module Product + ProductVariant + ProductImage.
User và Category module đã xong (xem docs/3.2.5-refactor-user-module.md và
docs/3.2.5-refactor-category-module.md), áp dụng cùng pattern cho Product.
```

**Checklist module Product (sắp làm):**
- [ ] `entity/Product.java`, `ProductVariant.java`, `ProductImage.java` — `@Getter @Setter @NoArgsConstructor`
- [ ] `dto/request/Create*Request.java` + `Update*Request.java` — `@Getter @Setter` hoặc `@Data`
- [ ] `dto/response/ProductResponse.java`, etc. — `@Getter @Setter`, xoá `fromEntity` viết tay
- [ ] `services/ProductService.java` — `@RequiredArgsConstructor`, inject `ProductMapper`
- [ ] `controllers/ProductController.java` — `@RequiredArgsConstructor`
- [ ] Tạo `mapper/ProductMapper.java` — 3 method + xử lý **nested mapping** (`@Mapping(source = "seller.id", target = "sellerId")`, etc.)
- [ ] Verify `./mvnw clean compile` pass + test runtime API product

**Sau Product:** **buổi cleanup chuyên sâu** (xem memory `project-production-cleanup-session`).

**Sau bài 3.2.5:** trước khi sang 3.3, có **buổi cleanup chuyên sâu** đã chốt (xem memory `project-production-cleanup-session`) để hardening codebase lên production-grade: `@Transactional`, `@Version` optimistic locking, `@CreationTimestamp`/Auditing, `@Slf4j` log audit, validation chặt hơn, xử lý TOCTOU race, cân nhắc `record` cho Response DTO.

**Sau cleanup:** sang **3.3 — Tìm kiếm & Lọc sản phẩm** (search, filter, `Pageable`, có thể cả Elasticsearch).

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
