# Handoff — Trạng thái dự án khi chuyển máy

> File này dành cho **bản thân tương lai** hoặc Claude trên máy mới — đọc file này trước khi bắt đầu phiên làm việc mới.

**Cập nhật lần cuối:** 2026-06-21

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
- ✅ **4.2 Order — CODE XONG** (test E2E + email để sau) — `docs/4.2-order.md`. Entity (snapshot price/total) + DTO (tách Summary/Detail) + Repository (`findByCustomerId`, `findWithItemById` @EntityGraph) + Mapper + Service (checkout atomic, **pessimistic lock qua `em.refresh(PESSIMISTIC_WRITE)`** chống oversell, state machine confirm/ship/deliver/cancel, phân quyền, hoàn stock khi cancel) + Controller (4 endpoint). Postman group "9. Order" đã thêm — **chưa chạy test**. Docs có deep-dive **concurrency lớn nhất dự án**: 3 cách chống oversell, `@Lock` syntax, isolation level vs lock, SELECT FOR UPDATE vs SERIALIZABLE, **stale-read gotcha** (pessimistic lock + entity pre-loaded → phải `em.refresh`), multi-node (DB lock vs JVM lock). Email xác nhận `@Async` CHƯA làm.
- ✅ **4.1 Shopping Cart — CODE XONG** (test E2E để sau) — `docs/4.1-shopping-cart.md`. Entity (owning/inverse side) + DTO + Repository (`findExisting` xử lý variant NULL, `findWithItemsByUserId` `@EntityGraph` chống N+1 — lưu ý: "WithItems" phải đặt TRƯỚC "By" kẻo QueryCreationException) + Mapper (`@AfterMapping` tính live price/subtotal/total) + Service (get-or-create, merge cộng dồn, check stock theo tổng mới, ownership 404, ACTIVE check) + Controller (5 endpoint, user từ `@AuthenticationPrincipal`). Postman group "7. Cart" (15 request) đã thêm — **chưa chạy test**. Docs có 2 deep-dive lớn: **`reduce` Stream API** + **N+1 query** (verbatim Q&A).
- ✅ **3.2.6 Buổi Cleanup Chuyên Sâu — HOÀN THÀNH** (production-grade hardening) — `docs/3.2.6-production-cleanup.md` (11 phần, lý thuyết + Q&A verbatim). Cụm 1 (Category circular) + 2.1 (`@Transactional`+OSIV) + 2.3 (`@Version` optimistic lock) + 2.4 (TOCTOU/`DataIntegrityViolation`→409) + 3 (BaseEntity+Auditing) + 3.6 (`@Slf4j`) + 4 (security exception/#7#8#9) + 5 (compensating tx `uploadImage`). **Buổi cleanup #1 đóng lại**, phần còn lại dời sang **buổi Cleanup #2 ở Giai đoạn 4** (ROADMAP mục 4.0).
- ✅ **3.3.1 → 3.3.3: Pagination + Search + Filter động** — `docs/3.3-search-filter-pagination.md`. Pageable/Sort foundation + `PageResponse<T>` (fix warning PageImpl) + `@Query` search keyword + JPA Specification filter động (category/price/status) + `@BatchSize` chống N+1. Build pass, đã test E2E. Còn **3.3.4 Elasticsearch** chưa làm.
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

**Tiếp theo (đề xuất):** sang **Giai đoạn 4 (Cart/Order)** theo ROADMAP, hoặc quay lại làm nốt 3.3.4 ES — tuỳ ưu tiên.

**Buổi Cleanup #1 (3.2.6) đã ĐÓNG** — 8 cụm done. Phần carry-over gom vào **buổi Cleanup #2 ở Giai đoạn 4** (ROADMAP mục 4.0), gồm:
- ⏳ **Cụm 2.3b** — Full optimistic lock think-time (client gửi `version` qua Request+Response DTO) — đã giảng khái niệm (docs Phần 9.7b), còn code
- ⏳ **Cụm 5b** — `deleteImage` dùng `TransactionSynchronization.afterCommit()` (làm khi migrate S3)
- ⏳ Tách `StorageException` (500) khỏi `InvalidFileException` (400) — lỗi I/O ghi file đang trả nhầm 400
- ⏳ **Lặt vặt:** Flyway/Liquibase thay `ddl-auto:update`; password validation regex; tách ProductListItemResponse vs ProductDetailResponse; dọn property `app.name/version/description` thừa trong application.yaml

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
Buổi Cleanup #1 (3.2.6) đã xong (8 cụm). Bài 3.3 search/filter/pagination xong 3/4.
Hôm nay bắt đầu Giai đoạn 4 (Cart/Order) theo ROADMAP.
(Carry-over cleanup gom ở ROADMAP mục 4.0 — buổi Cleanup #2.)
```

**Tiếp theo (đã chốt):** **4.1 Cart + 4.2 Order đã code xong** (test E2E để sau). Phần kế: **email xác nhận đơn `@Async` + JavaMailSender** (nốt của 4.2), rồi sang **Giai đoạn 5 (Thanh toán)** hoặc **buổi Cleanup #2** (mục 4.0 ROADMAP). 3.3.4 Elasticsearch defer (hạ tầng đã dựng).

**Việc còn nợ ở 4.1 + 4.2 (làm khi quay lại):**
- ⏳ Test E2E Postman group "7. Cart" (15 request) + "9. Order" — bật `show-sql: true` tạm để verify N+1/lock
- ⏳ **Email xác nhận đơn** `@Async` + JavaMailSender (nốt 4.2)
- ⏳ Test oversell bằng script đa luồng thật (Postman thường không mô phỏng được)
- ⏳ Concurrency/optimistic-lock think-time cho cart → gom vào buổi Cleanup #2
- ⏳ Staff (SELLER/ADMIN) xem detail mọi đơn (hiện `getOrderDetail` chỉ chủ đơn)

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
