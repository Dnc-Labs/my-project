# Handoff — Trạng thái dự án khi chuyển máy

> File này dành cho **bản thân tương lai** hoặc Claude trên máy mới — đọc file này trước khi bắt đầu phiên làm việc mới.

**Cập nhật lần cuối:** 2026-05-31

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
- ✅ **3.3.1 → 3.3.3: Pagination + Search + Filter động** — `docs/3.3-search-filter-pagination.md`. Pageable/Sort foundation + `PageResponse<T>` (fix warning PageImpl) + `@Query` search keyword + JPA Specification filter động (category/price/status) + `@BatchSize` chống N+1. Build pass, đã test E2E. Còn **3.3.4 Elasticsearch** chưa làm.
- ✅ **3.2.6 Cụm 1 + Cụm 2.1: Production cleanup (1 phần)** — `docs/3.2.6-production-cleanup.md`. Fix bug Category circular nông + apply `@Transactional` 6 service + tắt OSIV + E2E test pass. Còn 4 cụm defer (Cụm 2.3 `@Version` đã giảng lý thuyết).
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

**Bài 3.3 làm xong 3/4 phần** — 3.3.1 (Pageable) + 3.3.2 (search `@Query`) + 3.3.3 (filter Specification) đã xong + test E2E + commit `84c2954`.

**3.3.4 Elasticsearch — DEFER (đang dang dở):** đã giảng xong lý thuyết + chốt kiến trúc + **dựng hạ tầng** (docker ES+Kibana 8.15.3, dependency `spring-boot-starter-data-elasticsearch`, config `application.yaml`). **Phần code Java CHƯA làm** (Document/Repository/event-sync/search endpoint). Toàn bộ lý thuyết + Q&A + 6 bước code còn lại lưu ở **`docs/3.3.4-elasticsearch.md`**. Khi quay lại: `docker compose up -d elasticsearch kibana` rồi đọc file docs đó.

**Tiếp theo (đề xuất):** có thể sang **3.4 / Giai đoạn 4 (Cart/Order)** theo ROADMAP, hoặc quay lại làm nốt 3.3.4 ES, hoặc dọn backlog cleanup 3.2.6 — tuỳ ưu tiên.

**Backlog cleanup 3.2.6** vẫn còn 4 cụm DEFER (xem dưới).

**Backlog cleanup còn lại** (memory `project-production-cleanup-session`):
- ✅ **Cụm 3 — DONE** — `BaseEntity` + JPA Auditing + `@Slf4j` logging (commit 1669062, 0a6de8a)
- ✅ **Cụm 4 — DONE** — JwtAccessDeniedHandler 403 JSON + InvalidFileException + xoá 14 file demo + `@AuthenticationPrincipal`/CustomUserDetails/getReferenceById (commit 9fd5d6b, beac5ae, c0a10cd)
- ⏳ **Cụm 2.3** — Apply `@Version` cho 5 entity (đã giảng lý thuyết đầy đủ, chưa code). Cộng handler `ObjectOptimisticLockingFailureException` → 409.
- ⏳ **Cụm 2.4** — TOCTOU race: catch `DataIntegrityViolationException` → `DuplicateResource` ở `GlobalExceptionHandler`
- ⏳ **Cụm 5** — Compensating transaction cho `ProductImageService.uploadImage` (storage ↔ DB) + tách long-running I/O ra khỏi `@Transactional` (pattern store-then-record)
- ⏳ **Lặt vặt production-grade:** Flyway/Liquibase thay `ddl-auto:update`; password validation regex; tách ProductListItemResponse vs ProductDetailResponse; InvalidFileException log error→warn (400 là client sai); xoá property `app.name/version/description` thừa trong application.yaml

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

*Từ Cụm 1 + 2.1 (cleanup session 2026-05-25):*
- Pattern `@Transactional` chuẩn: class-level `@Transactional(readOnly = true)` default + override `@Transactional` cho write method. Sau đó tắt OSIV `spring.jpa.open-in-view: false` để verify
- Import phải là `org.springframework.transaction.annotation.Transactional` (KHÔNG dùng `jakarta.transaction.Transactional` JTA — hạn chế feature)
- Pattern senior `delete(entity)` thay `deleteById(id)` khi đã có entity managed → 1 SELECT thay 2
- `setPrimary` ở `ProductImageService` là demo đẹp nhất của dirty checking — KHÔNG gọi `save()`, để Hibernate auto flush 2 entity
- Method walk-up O(H) đẹp hơn walk-down O(N) cho cây rộng-nông như category e-commerce — luôn cân search space khi duyệt graph
- Helper method phải **complete trong specification của nó** — không phụ thuộc caller pre-check (vd `isAncestorOf` phải tự cover self-parent case)
- Defense in depth: fix logic chặn bug mới + circuit breaker (depth limit) chặn bug cũ + optimistic locking chặn race
- Optimistic vs Pessimistic Lock là **khái niệm GENERAL CS / concurrency**, có ở mọi ORM/DB/distributed system (Sequelize `version: true` y hệt JPA `@Version`)

---

## 3. Bước tiếp theo cụ thể (action items)

Khi mở Claude trên máy mới, paste prompt sau để có context ngay:

```
Hi Claude, đọc docs/HANDOFF.md để biết trạng thái dự án.
Bài 3.2.5 (refactor Lombok + MapStruct) đã xong cả 4 phần.
Hôm nay bắt đầu BUỔI CLEANUP CHUYÊN SÂU (production-grade hardening)
trước khi sang bài 3.3. Đọc memory project-production-cleanup-session để xem backlog.
```

**Tiếp theo (đã chốt):** sang **3.3 — Tìm kiếm & Lọc sản phẩm** (search, filter, `Pageable`, có thể cả Elasticsearch). 4 cụm cleanup còn lại defer làm sau, không bắt buộc trước 3.3.

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
