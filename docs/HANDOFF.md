# Handoff — Trạng thái dự án khi chuyển máy

> File này dành cho **bản thân tương lai** hoặc Claude trên máy mới — đọc file này trước khi bắt đầu phiên làm việc mới.

**Cập nhật lần cuối:** 2026-05-23

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
- ✅ **3.2.5 Phần 4: Refactor module Product + Variant + Image** — `docs/3.2.5-refactor-product-module.md`
- ✅ **Test E2E toàn bộ refactor** (vừa xong) — Postman collection `postman/ecommerce-api.postman_collection.json`. Đã pass tất cả 8 module trong collection
- ✅ **3.2.5 Phần 3: Refactor module Category** — `docs/3.2.5-refactor-category-module.md`
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

**Bài 3.2.5 đã KẾT THÚC** — toàn bộ 4 module (setup, User, Category, Product+Variant+Image) đã refactor sang Lombok + MapStruct.

**Tiếp theo: BUỔI CLEANUP CHUYÊN SÂU** (đã chốt từ trước, không phải bài ROADMAP).

Đây là buổi hardening codebase lên production-grade trước khi sang bài 3.3. Backlog đầy đủ ở memory `project-production-cleanup-session`. Top priorities:
- `@Transactional` ở mọi service (User, Category, Product, ProductVariant, ProductImage)
- `@Version` optimistic locking cho entity sửa được
- Auditing (`@CreationTimestamp` / `@UpdateTimestamp` hoặc Spring Data JPA Auditing với `BaseEntity`)
- Fix bug **circular check Category nông** (đang chỉ check 1 cấp con, cần recursive)
- Convert `RuntimeException` → `BadRequestException` / `ConflictException` / `BusinessRuleException`
- `@AuthenticationPrincipal` thay cho `SecurityContextHolder` truy xuất thủ công ở `ProductService.createProduct`
- Compensating transaction cho `ProductImageService.uploadImage` (storage ↔ DB lifecycle)
- TOCTOU race ở các chỗ `existsByX` + `save` ở 2 transaction tách biệt

**Pattern áp dụng (3 module đã làm theo, để tham khảo khi xử lý bài sau):**
- Entity: `@Getter @Setter @NoArgsConstructor` (KHÔNG `@Data` vì có quan hệ)
- DTO request/response: `@Getter @Setter` hoặc `@Data` (đều OK)
- Service + Controller: `@RequiredArgsConstructor`, dependency `final`
- Tạo `*Mapper` interface với 3 method: `fromEntity`, `fromRequestDto`, `updateEntity(req, @MappingTarget entity)`
- Vì `unmappedTargetPolicy=ERROR` global → phải `@Mapping(target=..., ignore=true)` cho MỌI field entity không có trong request

**Lưu ý quan trọng đã rút kinh nghiệm:**

*Từ User module:*
- KHÔNG dùng `@AllArgsConstructor` trên Spring bean (chỉ dùng `@RequiredArgsConstructor`)
- Nếu request có field giống entity nhưng cần xử lý security (password) → BẮT BUỘC `@Mapping(ignore=true)` rồi service tự set
- Javadoc phải đặt TRƯỚC annotation

*Từ Category module:*
- `@BeanMapping(IGNORE)` **CHỈ** dùng cho method update có `@MappingTarget`, KHÔNG đặt ở method create
- MapStruct **TỰ ĐỆ QUY** cho self-reference — chỉ cần có sẵn method map cùng kiểu
- Field khác type giữa request/entity (`parentId: Long` ↔ `parent: Category`) → mapper `ignore`, service tự `findById` + set
- Tên method mapper ngắn theo pattern: `fromEntity` / `fromRequestDto` / `updateEntity`, KHÔNG lặp tên type
- Pragmatic-update (mapper map field an toàn → validate-then-set rời cho field cần validate) là OK miễn có `@Transactional` + `RuntimeException`

*Từ Product module:*
- Nested mapping: `@Mapping(target = "categoryName", source = "category.name")` — MapStruct **tự null-safe** cho nested access
- Compose mapper qua `@Mapper(uses = {OtherMapper.class})` — quên `uses` → compile error với policy `ERROR`
- Custom presentation logic (vd `primaryImageUrl` lọc list) → dùng `default method` + `@Named` + `qualifiedByName` trong mapper, KHÔNG đẩy sang service
- Quy tắc embed vs tách: child resource nhỏ + thường-xuyên-cần → embed; lớn + tuỳ chọn → API riêng
- Defensive NPE check rẻ thì nên làm (`getCategory() == null || !... .equals(...)`)
- Naming convention nhất quán cả mapper + repo: `productVariantMapper` ↔ `productVariantRepository` (không mix rút gọn)
- Build mapper xong PHẢI `./mvnw clean compile`, đừng tin Maven cache

*Từ test E2E (sau khi xong refactor):*
- Field `*Id` ở response (vd `productId`, `sellerId`) mà entity có object → BẮT BUỘC `@Mapping(target = "xxxId", source = "xxx.id")`, MapStruct không tự match
- Bug `children/images/variants = null` ở response sau `create` → fix 2 layer: init entity field `= new ArrayList<>()` + mapper `@BeanMapping(SET_TO_DEFAULT)`. Default `= Collections.emptyList()` ở DTO KHÔNG cứu được vì MapStruct override bằng setter
- Quick fix `JwtAuthenticationEntryPoint` đã apply tạm để test pass (response 401 JSON consistent thay vì 403 plain). Cleanup session sẽ làm bài bản + thêm `AccessDeniedHandler`

---

## 3. Bước tiếp theo cụ thể (action items)

Khi mở Claude trên máy mới, paste prompt sau để có context ngay:

```
Hi Claude, đọc docs/HANDOFF.md để biết trạng thái dự án.
Bài 3.2.5 (refactor Lombok + MapStruct) đã xong cả 4 phần.
Hôm nay bắt đầu BUỔI CLEANUP CHUYÊN SÂU (production-grade hardening)
trước khi sang bài 3.3. Đọc memory project-production-cleanup-session để xem backlog.
```

**Sau cleanup session:** sang **3.3 — Tìm kiếm & Lọc sản phẩm** (search, filter, `Pageable`, có thể cả Elasticsearch).

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
