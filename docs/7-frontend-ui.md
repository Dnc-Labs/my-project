# Buổi 7 — Dựng Frontend UI cho toàn bộ module 1→6

> Bản ghi nguyên văn buổi làm việc. Ghi đủ câu của người học, phần assistant giảng/quyết định,
> output của tool quan trọng (design system, kết quả verify, stacktrace) theo đúng thứ tự thời gian.
> Code đầy đủ nằm trong repo ở `UI/` — trong file này chỉ dán các đoạn cốt lõi kèm lý do.

**Ngày:** 2026-08-17 → 2026-08-18
**Phạm vi:** UI React cho tất cả endpoint đã code từ module 1 (User/Auth) đến module 6.1 (Review & Rating)

---

## 1. Yêu cầu ban đầu

**Người học:**

> Tôi đã làm đến module 6 rồi, hãy triển khai ui theo các tính năng đã triển khai nhé, dùng ui-ux-pro để triển khai thật hiện đại, bắt mắt và hợp lý

**Assistant** khảo sát codebase trước: đọc 10 controller, toàn bộ `dto/request` + `dto/response`, enum, `SecurityConfig`, `WebMvcConfig`, `WebConfig`, `application.yaml`, `JwtTokenProvider`, `OrderService`, `ReviewService`, `docs/HANDOFF.md`, `ROADMAP.md`, `seed-data.sql`.

Sau đó assistant định hỏi 3 câu chốt phạm vi (stack / phạm vi storefront-dashboard / hướng thiết kế). Người học ngắt:

> CHúng ta đã có một thư mục UI và chọn cấu trúc rồi mà, bạn chỉ cần kiểm tra và phát triển thêm thôi

Assistant tìm lại và thấy thư mục `UI/` đã tồn tại (trước đó bị cắt khỏi output `find` do `head -60`).

---

## 2. Trạng thái `UI/` khi tiếp nhận

Stack đã chốt sẵn trong `package.json`:

| Thành phần | Lựa chọn |
|---|---|
| Build | Vite 8 + React 19 + TypeScript |
| Style | Tailwind CSS v4 + shadcn/ui (style `new-york`), token qua CSS variable |
| Data fetching | TanStack Query v5 |
| Auth state | Zustand + `persist` |
| Form | React Hook Form + Zod |
| Router | React Router v7 (`createBrowserRouter`) |

Đã có: `apiClient` (axios + interceptor refresh token), `authStore`, `ProtectedRoute`, 3 layout, 13 component `ui/`, trang Login/Register, `index.css` với design token (Nunito Sans + Rubik, accent xanh lá oklch).

**Còn là placeholder:** `ProductListPage`, `ProductDetailPage`, `SellerProductListPage`, `SellerProductFormPage` — nội dung chỉ ghi "sẽ được dựng ở Phase 3/4".

### Những chỗ code cũ lệch backend (phát hiện khi đối chiếu DTO)

Đây là phần quan trọng: **type FE cũ không khớp DTO thật**, nếu cứ thế build tiếp thì lỗi runtime rải rác.

| Chỗ lệch | Thực tế backend |
|---|---|
| `ProductResponse` thiếu `slug`, `sku`, `status`, `averageRating`, `reviewCount`, `primaryImageUrl`, `images`; lại có `imageUrl` không tồn tại | `ProductResponse.java` có đủ các field trên |
| `listProducts()` khai trả `ProductResponse[]` | Controller trả `PageResponse<ProductResponse>` (bọc `content` + `page`) |
| `CreateProductRequest` thiếu `slug`, `sku` | Cả hai đều `@NotBlank` → tạo sản phẩm chắc chắn nhận 400 |
| `CategoryResponse` có `parentId` | Backend trả cây (`children`), **không** có `parentId`; có `description`, `status` |
| `JwtPayload` thiếu `userId` | `JwtTokenProvider` đặt claim `userId` |

---

## 3. Nạp skill ui-ux-pro-max

Chạy:

```bash
python scripts/search.py "ecommerce marketplace storefront seller dashboard modern trustworthy" --design-system -f markdown
```

Output (nguyên văn phần chính):

```
### Pattern
- Name: Marketplace / Directory
- Conversion Focus: Search bar is the CTA. Reduce friction to search. Popular searches suggestions.
- CTA Placement: Hero Search Bar + Navbar 'List your item'
- Color Strategy: Search: High contrast. Categories: Visual icons. Trust: Blue/Green.
- Sections: 1. Hero (Search focused), 2. Categories, 3. Featured Listings, 4. Trust/Safety, 5. CTA

### Style
- Name: Vibrant & Block-based
- Colors: Primary #0F172A · Accent #22C55E · Background #020617 (dark)
- Typography: Heading Fira Code / Body Fira Sans

### Avoid (Anti-patterns)
- Low trust signals
- Confusing layout
```

**Quyết định của assistant:** không áp bộ này. Lý do đã nói với người học:

> Design-system mặc định gợi ý dark + Fira Code (hợp dashboard analytics, không hợp storefront). Dự án đã có token riêng (Nunito Sans + Rubik, accent xanh lá) — tôi giữ hệ đó cho nhất quán

Đây đúng nguyên tắc `consistency` của chính skill: **một style xuyên suốt** quan trọng hơn việc đổi sang bảng màu "đẹp hơn" giữa dự án. Cái lấy từ skill là **pattern Marketplace** (hero search làm CTA, khối danh mục, listing nổi bật, khối trust) và bộ checklist a11y/performance.

Tra thêm 2 domain:

```bash
python scripts/search.py "ecommerce product listing cart checkout trust" --domain product
python scripts/search.py "empty-state loading skeleton form validation pagination table" --domain ux
```

Các rule rút ra và áp dụng thật:
- `empty-states`: mọi danh sách rỗng phải có thông điệp + hành động tiếp theo
- `loading-states` / `progressive-loading`: skeleton giữ đúng kích thước nội dung thật (chống CLS)
- `loading-buttons`: disable + spinner khi async
- `input-labels`, `error-placement`, `inline-validation`
- `deep-linking`: trạng thái lọc phải nằm trên URL
- `table-handling`: bảng cuộn ngang trong container riêng
- `color-not-only`: trạng thái đơn phải có icon + chữ, không chỉ màu

---

## 4. Kế hoạch 8 hạng mục

1. Đồng bộ types + API layer với backend thật
2. Bổ sung design token + UI primitive còn thiếu
3. Component domain dùng chung
4. Storefront: Home + Product List + Product Detail
5. Storefront: Cart + Checkout + VNPay
6. Đơn hàng + Hồ sơ + Đánh giá của tôi
7. Seller/Admin dashboard
8. Router, Header/nav, typecheck + build

---

## 5. Hạng mục 1 — Types + API layer

Viết lại `src/types/api.ts` mirror đúng DTO. Hai chỗ đáng ghi chú:

```ts
/** Wrapper phân trang tự định nghĩa ở backend (PageResponse<T>) — KHÔNG phải Page của Spring Data. */
export interface PageResponse<T> {
  content: T[]
  page: {
    number: number; size: number; totalElements: number
    totalPages: number; first: boolean; last: boolean
  }
}
```

```ts
/**
 * Update là PUT toàn phần: backend khai `stock = 0` làm default nên field nào không gửi
 * sẽ bị ghi đè bằng default → form luôn gửi đủ field.
 */
export interface UpdateProductRequest {
  name?: string; slug?: string; sku?: string; price?: number
  description?: string; stock: number
  status?: ProductStatus; categoryId?: number
}
```

Cái thứ hai là **bẫy thật** của `UpdateProductRequest.java`: field `private Integer stock = 0;`. Nếu FE gửi PATCH-style (chỉ field đổi) thì tồn kho về 0 âm thầm. Vì vậy form sửa sản phẩm luôn gửi đủ mọi field.

**CORS:** không sửa gì ở backend. Dùng dev proxy của Vite — request đi cùng origin với dev server:

```ts
// vite.config.ts
server: {
  port: 5173,
  proxy: {
    // Proxy thay cho CORS: request đi cùng origin với dev server nên backend
    // không cần khai CorsConfigurationSource.
    "/api": { target, changeOrigin: true },
    // Ảnh sản phẩm được Spring serve tĩnh qua /uploads/** (WebMvcConfig).
    "/uploads": { target, changeOrigin: true },
  },
}
```

Đồng thời sửa một lỗi có sẵn: proxy đang trỏ `http://localhost:8081` trong khi `application.yaml` khai `server.port: 8080`. Nay đọc từ env, mặc định 8080.

Tạo 7 file API (`cart`, `orders`, `payments`, `reviews`, `users`, `variants`, `images`) và 8 hook. Ghi chú đáng chú ý trong `images.ts`:

```ts
/**
 * Upload multipart. KHÔNG set Content-Type thủ công — để axios tự sinh boundary,
 * nếu ghi đè "multipart/form-data" trần thì Spring không parse được part.
 */
```

`lib/orderStatus.ts` mirror đúng `OrderService.validateTransition` để FE chỉ hiện nút hợp lệ:

```ts
export function nextStatuses(current: OrderStatus): OrderStatus[] {
  switch (current) {
    case "PENDING":   return ["CONFIRMED", "CANCELLED"]
    case "CONFIRMED": return ["SHIPPING", "CANCELLED"]
    case "SHIPPING":  return ["DELIVERED"]
    default:          return []
  }
}
```

---

## 6. Hạng mục 2 — Token + primitive

Thêm token trạng thái vào `index.css` (light + dark định nghĩa riêng, **không đảo giá trị**):

```css
--success: oklch(0.62 0.18 145);
--success-foreground: oklch(0.42 0.13 145);
--warning: oklch(0.75 0.16 75);
--warning-foreground: oklch(0.48 0.12 62);
--info: oklch(0.6 0.15 250);
--info-foreground: oklch(0.44 0.14 254);
```

Mỗi màu có **2 tông**: một cho nền (`bg-warning/15`), một cho chữ (`text-warning-foreground`) để cặp nền-nhạt/chữ-đậm luôn đạt contrast ≥ 4.5:1.

Thêm 2 utility:

```css
.tabular { font-variant-numeric: tabular-nums; }  /* giá tiền không nhảy layout khi số đổi */
.container-page { margin-inline: auto; max-width: 80rem; padding-inline: 1rem; }
```

Và `prefers-reduced-motion` tắt animation toàn cục.

Viết thêm 6 primitive theo pattern shadcn của dự án (`radix-ui` bundled): `separator`, `textarea`, `tabs`, `alert-dialog`, `sheet`, `avatar`. Thêm `ThemeToggle` — `next-themes` đã có trong deps và `sonner.tsx` đã `useTheme()` nhưng **chưa ai bọc `ThemeProvider`**, nên bổ sung vào `main.tsx`.

---

## 7. Hạng mục 3 — Component domain

`RatingStars`: nửa sao làm bằng overlay width `%` thay vì icon half-star riêng, nhờ đó 4.3★ hiển thị đúng tỉ lệ. Bản nhập liệu `RatingInput` dùng `<input type="radio">` thật (ẩn bằng `sr-only`) để bàn phím và screen reader dùng được.

`Pagination`: rút gọn dãy số trang, luôn giữ trang đầu/cuối + trang kề, phần cắt thay bằng `…`.

`ProductCard`: link phủ toàn thẻ bằng `after:absolute after:inset-0`, nhưng nút thêm giỏ phải `relative z-10` — nếu không cú bấm rơi vào link phủ:

```tsx
<Button
  size="icon-sm"
  // z-10 để nút nổi lên trên link phủ card, nếu không cú bấm sẽ rơi vào link.
  className="relative z-10"
  ...
```

`OrderStatusBadge`: badge luôn gồm **icon + chữ**, không phân biệt trạng thái bằng riêng màu.

---

## 8. Hạng mục 4 — Storefront catalog

`ProductListPage`: toàn bộ trạng thái lọc nằm trên URL (`deep-linking`), keyword debounce 400ms rồi mới đẩy lên URL:

```tsx
// Gõ tới đâu gọi API tới đó sẽ spam backend → chờ người dùng ngừng gõ 400ms mới đẩy lên URL.
useEffect(() => {
  if (keywordInput === keywordParam) return
  const timer = setTimeout(() => {
    updateParams({ keyword: keywordInput || undefined, page: undefined })
  }, 400)
  return () => clearTimeout(timer)
}, [keywordInput])
```

Đổi bộ lọc thì **luôn reset về trang 1**, nếu không sẽ rơi vào trang trống của tập kết quả mới. Query dùng `placeholderData: (previous) => previous` để giữ lưới cũ trong lúc tải trang mới, tránh nháy layout.

Storefront cố định `status: "ACTIVE"` — hàng ngừng bán chỉ thấy trong khu quản trị.

`ProductDetailPage`: khi sản phẩm có biến thể thì **giá, tồn kho và ảnh lấy theo biến thể đang chọn**, và nút thêm giỏ bị chặn tới khi người dùng chọn:

```tsx
const price = selectedVariant?.price ?? product?.price ?? 0
const stock = selectedVariant?.stock ?? product?.stock ?? 0
const mustPickVariant = hasVariants && !selectedVariant
```

---

## 9. Hạng mục 5 — Cart, Checkout, VNPay

### Vấn đề return-url của VNPay

`application.yaml` cũ:

```yaml
return-url: http://localhost:8080/api/payments/vnpay/return
```

VNPay redirect **trình duyệt user** về đúng URL này → user thấy JSON thô `{"success":true,"data":"Thanh toan thanh cong"}`. Sửa 1 dòng để trỏ về trang UI:

```yaml
return-url: ${VNPAY_RETURN_URL:http://localhost:5173/payment/vnpay/return}
```

Trang FE đọc query, gọi ngược `GET /api/payments/vnpay/return` để **backend verify chữ ký** rồi hiển thị kết quả. Muốn giữ hành vi cũ chỉ cần set env `VNPAY_RETURN_URL`.

`vnp_OrderInfo` backend đặt là `"Thanh toan don hang {orderId}"`, nên FE lấy lại `orderId` để link về đơn:

```ts
function parseOrderId(orderInfo: string | null): number | null {
  if (!orderInfo) return null
  const match = orderInfo.match(/(\d+)\s*$/)
  return match ? Number(match[1]) : null
}
```

Trang kết quả nói rõ với user: **trạng thái ở đây chỉ để tham khảo, IPN mới là nguồn sự thật** — đúng thiết kế đã học ở buổi 5.

---

## 10. Hạng mục 6, 7 — Tài khoản và khu quản trị

`OrderDetailPage`: timeline 4 bước (`PENDING → CONFIRMED → SHIPPING → DELIVERED`), `CANCELLED` nằm ngoài luồng nên hiển thị riêng. Nút hành động sinh từ `nextStatuses()`; khách chỉ thấy "Huỷ đơn", staff thấy nút đẩy trạng thái tiến.

`SellerProductFormPage`: 3 tab — Thông tin / Ảnh / Biến thể. Tạo mới xong điều hướng thẳng sang tab Ảnh.

Có một lỗi type gặp thật khi làm form này: `z.coerce.number()` khiến phía input của schema thành `unknown`, `<Input value={field.value}>` không nhận. Cách xử lý:

```ts
/**
 * Mọi field giữ kiểu string đúng như giá trị thật của <input>/<Select>;
 * việc đổi sang number dồn vào lúc submit. Cách này tránh kiểu `unknown`
 * mà z.coerce sinh ra ở phía input của form.
 */
price: z.string().trim().min(1, "Vui lòng nhập giá")
  .refine((value) => Number(value) > 0, "Giá phải lớn hơn 0"),
```

`AdminCategoriesPage`: cây danh mục đệ quy. Lưu ý nghiệp vụ: **backend không trả `parentId`** trong `CategoryResponse`, nên khi sửa, select cha có thêm lựa chọn `"Giữ nguyên vị trí hiện tại"` và chỉ gửi `parentId` khi người dùng chủ động đổi.

`SellerOrdersPage`: đây là chỗ **API còn thiếu** nên UI phải thú nhận giới hạn thay vì giả vờ có dữ liệu:

```
Backend chưa có endpoint liệt kê đơn cho người bán và GET /api/orders/{id} chỉ trả về
đơn của chính chủ đơn. Vì vậy trang này thao tác theo mã đơn: chọn hành động, backend
kiểm tra quyền và luồng trạng thái rồi trả về đơn sau khi cập nhật.
```

Endpoint duy nhất mở cho staff là `PUT /api/orders/{id}/status`, và nó trả về `OrderDetailResponse` — nên trang lấy dữ liệu hiển thị từ chính response đó.

---

## 11. Hạng mục 8 — Router, code-split, chất lượng

Build lần đầu:

```
dist/assets/index-CayWVvfS.js   771.02 kB │ gzip: 231.51 kB
(!) Some chunks are larger than 500 kB after minification
```

Tách chunk theo route bằng `React.lazy` + `Suspense` (fallback là skeleton giữ đúng kích thước):

```
dist/assets/index-BaSIVwdp.js   449.19 kB │ gzip: 139.58 kB
+ 25 chunk nhỏ theo từng trang
```

ESLint bắt 2 lỗi thật do assistant viết:

```
error: Calling setState synchronously within an effect can trigger cascading renders
  ProductListPage.tsx:51  setKeywordInput(keywordParam)
  ProductFilters.tsx:35   setMinPrice(...)
```

Sửa bằng pattern "điều chỉnh state khi render" của React docs, gói thành hook:

```ts
/**
 * State cục bộ nhưng tự nhận giá trị mới khi nguồn bên ngoài đổi (ví dụ query param
 * thay đổi vì người dùng bấm link khác hoặc nút back).
 *
 * Dùng cách "điều chỉnh state khi render" thay vì useEffect + setState: React xử lý
 * ngay trong lượt render đó nên không tốn thêm một lượt render thừa.
 */
export function useSyncedState<T>(external: T): [T, (value: T) => void] {
  const [value, setValue] = useState(external)
  const [previous, setPrevious] = useState(external)
  if (!Object.is(previous, external)) {
    setPrevious(external)
    setValue(external)
  }
  return [value, setValue]
}
```

3 lỗi `react-refresh/only-export-components` còn lại nằm ở `components/ui/*` do chính quy ước shadcn (export kèm `buttonVariants`, `badgeVariants`, `useFormField`) → tắt rule cho đúng thư mục đó, kèm giải thích trong `eslint.config.js`.

Bổ sung sau đó: skip-link, `errorElement` (`RouteError`) chống trắng trang, `ScrollRestoration`, `CategoryMenu` ở header, chip lọc đang áp dụng, khối danh mục ở trang chủ, `theme-color` cho 2 theme.

---

## 12. Người học hỏi tiến độ

> làm đến đâu r

> Triển khai đến đâu r nhỉ, triển khai tiếp đi cho tôi

> Triển khai tiếp cho tôi mà ko dùng claude browser

Vì không dùng browser automation, việc kiểm chứng chuyển sang gọi HTTP thật.

---

## 13. Verify runtime bằng HTTP

Chuẩn bị: `docker start ecommerce-mysql`, `./mvnw spring-boot:run`, `npm run dev`.

Tài khoản trong DB chỉ có `admin@test.com` và `seller@test.com` (không biết mật khẩu — hash trong `seed-data.sql` không phải của `123456`). Nên tạo user test qua API public `POST /api/users`:

```
{"success":true,"message":"Create Successful","data":{"id":3,"email":"uitest@test.com",...,"role":"CUSTOMER","status":"ACTIVE"}}
```

Kết quả các bước (nguyên văn phần chính):

```
1) GET /carts
{"cartId":2,"items":[{"id":9,"productId":7,...,"quantity":2,"unitPrice":199000.00,"subTotal":398000.00}],
 "totalAmount":398000.00,"totalItems":2}

2) POST /orders (checkout)
{"id":8,"totalAmount":398000.00,"status":"PENDING","shippingAddress":"12 Nguyen Trai, Thanh Xuan, Ha Noi",
 "note":"Giao gio hanh chinh","items":[{"quantity":2,"price":199000.00,...}]}

3) GET /orders?page=0&size=5&sort=createdAt,desc
{"content":[{"id":8,"status":"PENDING","totalAmount":398000.00,...}],
 "page":{"number":0,"size":5,"totalElements":1,"totalPages":1,"first":true,"last":true}}

5) POST /payments/vnpay/create
{"paymentUrl":"https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?vnp_Amount=39800000&...
 &vnp_OrderInfo=Thanh+toan+don+hang+8&vnp_ReturnUrl=htt..."}

9) GET /users/3
{"id":3,"email":"uitest@test.com","fullName":"UI Test User",...,"role":"CUSTOMER","status":"ACTIVE"}

10) PUT /orders/8/status {"status":"CANCELLED"}
{"id":8,...,"status":"CANCELLED",...}
```

JWT claims kiểm tra được:

```json
{"sub":"uitest@test.com","userId":3,"role":"CUSTOMER","iat":1786554966,"exp":1786555866}
```

→ khớp `JwtPayload` phía FE, `userId` có thật.

---

## 14. Ba phát hiện ở backend

### 14.1 Module Review 6.1 lỗi runtime — `can-review` VÀ `POST review` đều trả 500

```
GET /api/products/7/reviews/can-review   → HTTP 500
POST /api/products/7/reviews             → HTTP 500
```

Stacktrace lấy từ log:

```
ERROR ... GlobalExceptionHandler : class java.lang.Long cannot be cast to class java.lang.Boolean
java.lang.ClassCastException: class java.lang.Long cannot be cast to class java.lang.Boolean
	at com.ecommerce.api.services.ReviewService.checkEligibility(ReviewService.java:145)
	at com.ecommerce.api.services.ReviewService.canReview(ReviewService.java:61)
	at com.ecommerce.api.controllers.ReviewController.canReview(ReviewController.java:75)
```

Dòng 145 là:

```java
if (!orderRepository.verifiedPurchase(userId, productId, OrderStatus.DELIVERED.name()))
    return ReviewEligibility.NOT_PURCHASED;
```

Repository khai:

```java
@Query(value = """
        SELECT EXISTS (
            SELECT 1 FROM orders o
            INNER JOIN order_items oi on oi.order_id = o.id
            WHERE o.customer_id= :userId AND oi.product_id= :productId AND o.status= :status )
        """, nativeQuery = true)
boolean verifiedPurchase(...)
```

**Nguyên nhân:** `EXISTS` trong native query trả về kiểu **số** (JDBC đưa lên thành `Long`), Hibernate không tự cast sang `boolean` mà repository khai báo → `ClassCastException`. Vì cả `canReview` và `createReview` đều đi qua `checkEligibility`, **toàn bộ tính năng đánh giá đang không dùng được** — chưa lộ ra vì test E2E của 6.1 còn nợ (13 kịch bản trong `docs/HANDOFF.md`).

Đối chiếu MySQL:

```sql
CREATE TEMPORARY TABLE t AS SELECT EXISTS (SELECT 1 FROM orders) AS r; DESCRIBE t;
-- r | int | NO | | 0 |
```

**Ba hướng sửa (người học tự chọn):**
1. Đổi kiểu trả về repository thành `long`/`Integer`, service so `> 0` — ít sửa nhất
2. Bọc lại query: `SELECT CASE WHEN EXISTS (...) THEN true ELSE false END`
3. Bỏ native, dùng JPQL: `SELECT COUNT(oi) > 0 FROM Order o JOIN o.orderItems oi WHERE ...` — JPQL trả boolean thật, sạch nhất và giữ được kiểm tra của compiler cho tên field

Phía FE đã xử lý để lỗi này không làm hỏng trải nghiệm: `useCanReview` đặt `retry: false`, và khi query lỗi thì **vẫn mở form đánh giá** kèm cảnh báo, thay vì chặn oan người đủ điều kiện:

```tsx
// Không xác định được quyền (endpoint can-review lỗi): vẫn mở form thay vì chặn oan.
// Nếu thực sự chưa đủ điều kiện, backend từ chối lúc gửi và toast hiện đúng lý do.
```

### 14.2 `POST /api/carts/items` trả item có `id: null`

```
POST /api/carts/items {"productId":7,"quantity":2}
→ {"cartId":2,"items":[{"id":null,"productId":7,...}],"totalAmount":398000.00,"totalItems":2}

GET /api/carts (sau đó)
→ {"cartId":2,"items":[{"id":9,...}]}
```

DTO được map **trước khi flush** nên `id` chưa sinh. FE cần `id` để sửa/xoá dòng đó, nên hook không tin response:

```ts
onSuccess: () => {
  // KHÔNG set thẳng response vào cache: item vừa thêm được backend map sang DTO
  // trước khi flush nên `id` còn null, mà UI cần id để sửa/xoá dòng đó.
  queryClient.invalidateQueries({ queryKey: CART_KEY })
}
```

Ở backend, cách gọn hơn là flush trước khi map (hoặc trả lại giỏ đọc lại sau khi lưu).

### 14.3 Thiếu 2 endpoint cho vai seller

- `ProductFilterRequest` không có `sellerId` → trang quản lý phải lấy tối đa 100 sản phẩm (`maxPageSize` của `Pageable`) rồi lọc ở client. Đã ghi cảnh báo trong UI khi tổng số vượt 100.
- Không có API liệt kê đơn cho người bán, và `getOrderDetail` chỉ cho chủ đơn (`checkOwnership`) → xem mục 10.

Cả ba đã ghi vào `UI/README.md` mục "Giới hạn còn phụ thuộc backend".

---

## 15. Kết quả cuối

```
npx tsc -b     → sạch
npx eslint .   → sạch
npm run build  → sạch, bundle chính 449 kB (gzip 139 kB) + 25 chunk theo route
```

Phủ **100% endpoint** đã code từ module 1 → 6.1: 45 file mới, 24 file sửa trong `UI/`.

Thay đổi phía backend chỉ đúng 1 dòng config (`return-url` của VNPay) — không sửa code Java nào.

## 16. Việc còn lại

- [ ] Sửa `verifiedPurchase` (mục 14.1) rồi test lại luồng đánh giá từ UI
- [ ] Flush trước khi map ở `CartService` (mục 14.2)
- [ ] Thêm `sellerId` vào `ProductFilterRequest` + Specification
- [ ] Endpoint list đơn cho SELLER/ADMIN + cho staff xem detail mọi đơn (đã nằm trong nợ của `HANDOFF.md`)
- [ ] Chạy nốt 13 kịch bản E2E của 6.1 sau khi sửa xong
