# Giai đoạn 5 — Thanh toán (VNPay)

> Bản ghi nguyên văn buổi học tích hợp cổng thanh toán VNPay: entity `Payment`, ký/verify HMAC-SHA512, build URL thanh toán, IPN handler + idempotency, return URL, SecurityConfig. Gồm toàn bộ lý thuyết assistant giảng + câu hỏi/trả lời của người học, đúng thứ tự thời gian.

---

## Mở đầu: chọn hướng đi

**Bối cảnh khi bắt đầu:**
- `Order` có `status` (PENDING → CONFIRMED → SHIPPING → DELIVERED / CANCELLED) và `totalAmount` snapshot.
- `checkout()` tạo Order ở trạng thái **PENDING**, trừ stock, gửi mail.
- **Chưa có gì về tiền** — đơn tạo ra coi như "đặt thành công" mà không qua thanh toán.

Giờ lắp tầng **Thanh toán** vào giữa.

---

# Giai đoạn 5 — Thanh toán (Payment)

## 1. Bức tranh tổng thể: tại sao Payment lại KHÓ hơn mọi thứ đã làm

Mọi feature trước đây (Cart, Order) đều nằm **trong tầm kiểm soát của bạn**: request vào → xử lý trong DB của bạn → trả response. Một vòng đồng bộ, một transaction, xong.

Payment phá vỡ điều đó vì có **bên thứ ba (payment gateway)** và **tiền thật**:

```
        HỆ THỐNG CỦA BẠN              BÊN THỨ BA (VNPay/Stripe)        NGÂN HÀNG
        ───────────────              ─────────────────────────        ─────────
  (1) tạo Payment, ký URL  ───────►
  (2) redirect user        ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─►  user nhập thẻ/OTP
                                                              │
                                                              ▼
                              (3a) redirect user về web bạn ◄─┘   (kênh KHÔNG tin được)
  (4) webhook/IPN server→server ◄──── (3b) gọi thẳng API bạn      (kênh TIN được)
  (5) cập nhật Order = PAID
```

Ba cái "khó" cốt lõi — và đây là **tư duy senior** bạn phải nắm:

**① Bất đồng bộ + hai kênh trả kết quả.** Sau khi user thanh toán, gateway báo kết quả cho bạn qua **2 đường khác nhau**:
- **Return URL (3a):** trình duyệt user được redirect về. Đây là kênh **KHÔNG đáng tin** — user có thể đóng tab giữa chừng, mạng rớt, hoặc tệ hơn: tự sửa URL giả mạo "thành công".
- **Webhook / IPN (3b)** (Instant Payment Notification): gateway **tự gọi server bạn**, server-to-server. Đây mới là **nguồn sự thật (source of truth)** để cập nhật trạng thái đơn. Return URL chỉ để hiển thị UI cho user xem.

> So với Node.js: y hệt Stripe webhook bạn có thể từng gặp — `app.post('/webhook', ...)` với `stripe.webhooks.constructEvent()` verify chữ ký. Khái niệm giống hệt, chỉ khác cú pháp Spring.

**② Idempotency (tính bất biến khi lặp).** Webhook có thể bị gateway gửi **nhiều lần** (retry khi nó không nhận được 200 OK từ bạn, hoặc mạng chập chờn). Nếu code bạn "nhận webhook PAID → cộng tiền/giao hàng" mà không chống lặp → **giao hàng 2 lần, tính doanh thu 2 lần**. Phải đảm bảo: xử lý webhook N lần = xử lý 1 lần.

**③ Bảo mật / chống giả mạo.** Vì có tiền, kẻ xấu sẽ cố gọi `/payment/callback?status=success&orderId=5` giả. Phải verify **chữ ký (signature/checksum)** mà gateway ký bằng secret key chung — không có chữ ký hợp lệ thì vứt.

---

## 2. Khái niệm mới quan trọng nhất: **tách `Payment` ra khỏi `Order`**

Câu hỏi thiết kế đầu tiên: lưu trạng thái thanh toán ở đâu? Hai lựa chọn:

**Cách ngây thơ:** thêm field `paymentStatus` vào luôn `Order`.

**Cách senior:** tạo **entity `Payment` riêng**, quan hệ với `Order`. Lý do:
- Một đơn có thể có **nhiều lần thử thanh toán** (lần 1 fail, user thử lại lần 2 thành công) → cần lịch sử, không ghi đè.
- Payment có **vòng đời riêng** (PENDING → SUCCESS / FAILED) độc lập với Order status (giao hàng).
- Tách trách nhiệm: `Order` lo logic đơn hàng, `Payment` lo logic tiền. Single Responsibility.

```
Order (1) ──────< (N) Payment
  status: giao hàng         status: tiền
  PENDING/CONFIRMED/...     PENDING/SUCCESS/FAILED
```

### Q&A định hướng #1

**Hỏi:** Chọn payment gateway nào để tích hợp đầu tiên?
**Trả lời (người học):** VNPay (sandbox).

**Hỏi:** Theo em, khi nhận được kết quả "thanh toán thành công", đâu là nguồn đáng tin để cập nhật Order = PAID?
**Trả lời (người học):** Webhook/IPN (server-to-server).

---

## Câu trả lời #2 — ĐÚNG, và đây là lý do sâu

Em chọn **Webhook/IPN là source of truth** — đúng tư duy senior. Giải thích kỹ tại sao Return URL **không được** dùng để cập nhật DB:

```
Kịch bản hỏng nếu tin Return URL:
  user thanh toán xong ──► gateway redirect về web bạn
                              │
                              ✗ user TẮT WIFI / đóng tab tại đây
                              │
                          Return URL KHÔNG BAO GIỜ chạy
                          → Order mãi mãi PENDING dù tiền ĐÃ TRỪ
```

Return URL chạy **trên trình duyệt user** → phụ thuộc việc user có về tới nơi không, và user có thể **chỉnh sửa**. Webhook (VNPay gọi là **IPN — Instant Payment Notification**) chạy **server-to-server**, VNPay tự gọi thẳng API bạn, **không qua trình duyệt** → tin được. Quy tắc vàng:

> **Return URL → chỉ để hiển thị UI ("Cảm ơn, đơn đang xử lý"). IPN → mới được đụng vào DB.**

Trên thực tế production vẫn xử lý **cả hai**, nhưng vì cả hai đều đi qua cùng một hàm cập nhật có **idempotency** + **verify chữ ký**, nên gọi 2 lần cũng an toàn. Còn nguồn *quyết định* vẫn là IPN.

---

## 3. VNPay hoạt động thế nào

VNPay **không có SDK Java chính thức** — bạn tự build URL + tự verify checksum bằng **HMAC-SHA512**. Đây thực ra là điều tốt cho việc học: bạn thấy rõ từng bước thay vì bị SDK giấu đi.

**Flow VNPay đầy đủ:**

```
┌─ BƯỚC 1: Tạo URL thanh toán ────────────────────────────────────┐
│ Client gọi:  POST /api/payments/vnpay/create  {orderId: 5}       │
│ Server:                                                           │
│   - tạo Payment(PENDING) cho order 5                             │
│   - gom params: vnp_Amount, vnp_TxnRef (mã GD), vnp_OrderInfo,   │
│     vnp_ReturnUrl, vnp_IpAddr, vnp_CreateDate...                 │
│   - SORT params theo alphabet → ghép thành query string          │
│   - ký: vnp_SecureHash = HMAC_SHA512(secretKey, queryString)    │
│   - trả về: https://sandbox.vnpayment.vn/...?...&vnp_SecureHash=xx│
└──────────────────────────────────────────────────────────────────┘
                          │ client redirect tới URL đó
                          ▼
┌─ BƯỚC 2: User nhập thẻ test trên trang VNPay ───────────────────┐
└──────────────────────────────────────────────────────────────────┘
                          │
            ┌─────────────┴──────────────┐
            ▼                             ▼
┌─ BƯỚC 3a: Return URL ──────┐  ┌─ BƯỚC 3b: IPN (source of truth)─┐
│ GET /vnpay/return?...      │  │ GET /vnpay/ipn?...               │
│ → verify hash              │  │ → verify hash                    │
│ → CHỈ hiển thị UI          │  │ → check idempotency              │
│   (không update DB)        │  │ → update Payment=SUCCESS         │
│                            │  │ → update Order=CONFIRMED         │
│                            │  │ → trả {"RspCode":"00"} cho VNPay │
└────────────────────────────┘  └──────────────────────────────────┘
```

**Hai khái niệm kỹ thuật mới sẽ gặp:**

1. **HMAC-SHA512 checksum** — hàm băm có khóa. Bạn và VNPay cùng giữ 1 `secretKey`. Bạn ký query string, VNPay ký lại bằng cùng key → nếu khớp nghĩa là dữ liệu không bị sửa giữa đường. Trong Java dùng `javax.crypto.Mac`. (Bên Node.js chính là `crypto.createHmac('sha512', key)`.)

2. **`vnp_TxnRef`** — mã giao dịch **duy nhất** bạn tự sinh cho mỗi lần thanh toán. Đây sẽ là **chìa khóa idempotency**: khi IPN báo về, bạn tra `vnp_TxnRef` → tìm đúng `Payment` → nếu nó đã `SUCCESS` rồi thì bỏ qua (không xử lý lại).

---

# Bước 1: Entity `Payment` + enums

### Q&A định hướng: Payment fields

**Hỏi:** Theo em, entity `Payment` nên có những field cốt lõi nào?
**Trả lời (người học):** chọn cả 4 nhóm — `id, order, amount, status` + `txnRef` + `provider + transactionNo` + `paidAt + rawResponse`.

Em chọn **cả 4 nhóm** — instinct rất tốt, đúng chuẩn một bảng `Payment` production.

Chốt thiết kế từng field + lưu ý kỹ thuật:

| Field | Kiểu | Lưu ý senior |
|---|---|---|
| `id` | `Long` | tự khai (`BaseEntity` không chứa id — giống `Order`) |
| `order` | `@ManyToOne` | nhiều Payment / một Order → cho phép thử lại |
| `amount` | `BigDecimal` | `precision=19, scale=2` — **không bao giờ** dùng `double` cho tiền |
| `status` | `PaymentStatus` | `@Enumerated(STRING)` |
| `txnRef` | `String` | **`unique = true`** — khóa idempotency, mỗi lần trả là 1 mã |
| `provider` | `PaymentProvider` | để sau cắm thêm Stripe không phải sửa schema |
| `transactionNo` | `String` | mã VNPay trả về (`vnp_TransactionNo`) — đối soát với VNPay |
| `paidAt` | `LocalDateTime` | thời điểm **thanh toán thành công** (khác `createdAt` = lúc tạo row) |
| `rawResponse` | `String` `TEXT` | lưu nguyên payload callback để debug/đối soát |

Lưu ý quan trọng nhất: **`txnRef` phải `unique`** — đây là cột làm nên idempotency.

`BaseEntity` chứa 4 audit field + `@Version`, **không** có `id` (mỗi entity tự khai id, giống `Order`).

**Enums tạo sẵn:**
```java
public enum PaymentStatus { PENDING, SUCCESS, FAILED }
public enum PaymentProvider { VNPAY }
```

---

## Người học hỏi: Tại sao 1 order có nhiều payment?

> "Tại sao trong payment thì quan hệ là 1 order có nhiều payment, tôi tưởng 1 order có 1 payment thôi chứ"

### Trực giác của em đúng... ở trường hợp lý tưởng

Trong **happy path** (mọi thứ trơn tru): user bấm thanh toán → trả tiền thành công → 1 Order có đúng 1 Payment. Em không sai.

Nhưng thực tế thanh toán **fail rất nhiều**, và khi fail thì user **thử lại**. Đó là lúc 1 Order sinh ra nhiều Payment.

### Kịch bản thực tế: tại sao cần nhiều Payment

```
Order #5 (totalAmount = 500k, status = PENDING)
│
├─ Payment #1  txnRef=ORD5-001  status=FAILED   (user nhập sai OTP)
├─ Payment #2  txnRef=ORD5-002  status=FAILED   (thẻ hết tiền)
└─ Payment #3  txnRef=ORD5-003  status=SUCCESS  ← lần này mới được
```

Mỗi lần user bấm "Thanh toán lại", bạn phải tạo **một `vnp_TxnRef` MỚI** gửi sang VNPay (VNPay **cấm** dùng lại txnRef cũ — mỗi giao dịch là duy nhất). Mà mỗi txnRef = một bản ghi Payment. Nếu model là `@OneToOne`, bạn buộc phải **ghi đè / xóa** Payment cũ → **mất lịch sử**.

### Vì sao giữ lại lịch sử lại quan trọng (góc nhìn senior)

**① Đối soát (reconciliation) với VNPay.** Cuối ngày kế toán đối chiếu: "VNPay báo có 3 giao dịch cho đơn này, hệ thống mình ghi mấy cái?". Nếu chỉ giữ cái cuối → không khớp được số liệu, không trace được tiền.

**② Tranh chấp / hoàn tiền.** User khiếu nại "tôi bị trừ tiền 2 lần". Bạn cần thấy **tất cả** các lần Payment để điều tra, kể cả cái FAILED.

**③ Race condition tiền thật.** Có khi Payment #1 user tưởng fail (mạng rớt lúc redirect) nhưng VNPay thực ra **đã trừ tiền** và gửi IPN về sau. Trong khi đó user đã thử Payment #2 thành công. Giờ đơn bị trả 2 lần → phải có cả 2 bản ghi mới phát hiện và hoàn 1 cái.

**④ Phân tích.** "Tỷ lệ fail của cổng VNPay là bao nhiêu? User thường fail mấy lần mới thành công?" — chỉ trả lời được nếu lưu cả lần fail.

### Nguyên tắc tổng quát rút ra

> **Bản ghi giao dịch tài chính là APPEND-ONLY (chỉ thêm, không sửa/xóa).** Mỗi lần thử = 1 dòng mới, bất biến. Không bao giờ update đè lên lần thử trước.

Đây là pattern chung của mọi hệ thống tiền (ngân hàng, ví điện tử, Stripe `PaymentIntent` cũng có nhiều `Charge`...). `@ManyToOne` (nhiều Payment → một Order) chính là cách JPA biểu diễn "một đơn, nhiều lần thử".

### Vậy "đơn đã thanh toán chưa" hỏi ở đâu?

Không hỏi ở Payment, mà hỏi ở **`Order.status`**. Khi **bất kỳ** Payment nào của đơn chuyển `SUCCESS`, IPN handler set `Order.status = CONFIRMED`. Order là nơi chốt "đã trả tiền", Payment là sổ nhật ký các lần thử. Tách bạch trách nhiệm.

---

## Review entity `Payment` (vòng 1)

Sau khi người học điền field, review phát hiện **2 lỗi nghiêm trọng** + vài chỗ cần siết:

### 🔴 Lỗi 1: Thiếu `@Enumerated(EnumType.STRING)` cho cả 2 enum

Đây là **cái bẫy kinh điển của JPA**. Khi không khai `@Enumerated`, mặc định JPA dùng **`EnumType.ORDINAL`** — tức lưu enum theo **vị trí số** (0, 1, 2...) thay vì tên:

```
PaymentStatus { PENDING, SUCCESS, FAILED }
                   0        1        2
DB lưu: status = 0  ←  hoàn toàn vô nghĩa khi đọc DB, và NGUY HIỂM:
```

Ngày nào đó chèn thêm value vào giữa enum, ví dụ `{ PENDING, PROCESSING, SUCCESS, FAILED }` → giờ `SUCCESS` thành số 2, nhưng **mọi row cũ lưu số 1 (vốn là SUCCESS) giờ bị đọc thành PROCESSING**. **Toàn bộ dữ liệu tiền bị sai lệch** mà không có exception nào báo.

→ Cả `status` và `provider` đều phải có `@Enumerated(EnumType.STRING)`.

### 🔴 Lỗi 2: `txnRef` thiếu `@Column(unique = true, nullable = false)`

Đây là **toàn bộ lý do idempotency hoạt động**. Khi IPN của VNPay bị gửi lặp, ta dựa vào `txnRef` để tra "đã xử lý chưa". `unique` ở tầng DB là **lưới an toàn cuối cùng**: kể cả khi 2 webhook chạy song song lọt qua check ở code (race condition), DB sẽ ném lỗi ở row thứ 2 → không bao giờ ghi 2 lần.

### 🟡 Lỗi 3: `amount` thiếu ràng buộc precision

Tiền **bắt buộc** khai rõ độ chính xác, y hệt `Order.totalAmount`:
```java
@Column(precision = 19, scale = 2, nullable = false)
private BigDecimal amount;
```

### 🟢 Naming

`paymentProvider` đặt trong class `Payment` bị lặp ngữ nghĩa → đặt gọn là `provider`.

**Quy tắc ghi nhớ:** *mọi enum trong JPA luôn `@Enumerated(EnumType.STRING)`*, không có ngoại lệ.

**Kết quả sau sửa** — entity production-grade:
```java
@Getter @Setter @Entity @Table(name = "payments")
public class Payment extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(unique = true, nullable = false)
    private String txnRef;

    @Enumerated(EnumType.STRING)
    private PaymentProvider provider = PaymentProvider.VNPAY;

    private String transactionNo;
    private LocalDateTime paidAt;

    @Column(columnDefinition = "TEXT")
    private String rawResponse;
}
```

Điểm khen về tư duy: để `paidAt` nullable thay vì gộp vào `createdAt`. Đúng — `createdAt` là lúc *tạo bản ghi* (lúc bấm thanh toán), `paidAt` là lúc *tiền về thật* (IPN báo SUCCESS). Hai mốc thời gian khác nhau, tách ra mới đối soát được.

---

# Bước 2: `PaymentRepository` + cấu hình VNPay

### Q&A: repository cần method gì?

**Trả lời (người học):**
1. Tìm payment theo `vnp_TxnRef` xem đã có chưa, nếu có rồi xem trạng thái để xử lý tiếp.
2. Method tìm payment theo `order_id`.

Cả hai đều **đúng chính xác**.

**① Tìm theo `txnRef`** — method xương sống của idempotency. Kiểu trả về nên là **`Optional<Payment>`**: tìm thấy → kiểm tra `status`; không thấy → `txnRef` lạ/giả mạo → từ chối. `Optional` ép xử lý cả 2 nhánh, không NPE.

**② Tìm theo `order_id`** — trả **`List<Payment>`** (một đơn nhiều payment). Thêm sort: lần thử mới nhất lên đầu → `findByOrderIdOrderByCreatedAtDesc`.

> Bổ sung: khi tạo Payment mới, nếu đơn **đã có** một Payment đang `SUCCESS` rồi thì phải chặn — cái này dùng lại được method ②.

### Config VNPay vào `application.yaml`

```yaml
vnpay:
  tmn-code: ${VNPAY_TMN_CODE:...}          # mã merchant (sandbox demo); override bằng env khi cần
  hash-secret: ${VNPAY_HASH_SECRET:...}    # khóa ký/verify HMAC-SHA512 — KHÔNG để lộ ở prod
  pay-url: https://sandbox.vnpayment.vn/paymentv2/vpcpay.html  # trang nhập thẻ VNPay
  return-url: http://localhost:8080/api/payments/vnpay/return  # nơi VNPay redirect user về (chỉ hiển thị UI)
  ipn-url: http://localhost:8080/api/payments/vnpay/ipn        # nơi VNPay gọi server-to-server (source of truth)
  version: "2.1.0"
  command: pay
  locale: vn
```

### Review `PaymentRepository`

**🔴 Lỗi: sai kiểu tham số `txnRef`**
```java
Optional<Payment> findByTxnRef(Long txnRef);   // ❌ Long
```
Field `txnRef` là **`String`**. Tham số phải là `String`. Quy tắc senior: **ID/mã định danh luôn là String, kể cả khi trông giống số** (giống số điện thoại — không lưu `0987...` thành `Long` vì mất số 0 đầu).

**🟡 Sort `Asc` vs `Desc`** — khi xem lịch sử, lần thử gần nhất nên hiện trên đầu → `Desc` tự nhiên hơn.

**🟢 Tên tham số `OrderId` → `orderId`** (camelCase).

**Điểm làm tốt:** cái bẫy cụm `OrderByCreatedAt` (sort) đứng cạnh entity `Order` — người học vượt qua đúng. Spring Data parse `findByOrderId` + `OrderBy...` chính xác: nó nhận ra `OrderBy` là **từ khóa sắp xếp**, phần `OrderId` còn lại resolve thành `order.id`.

**Kết quả sau sửa:**
```java
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByTxnRef(String txnRef);
    List<Payment> findByOrderIdOrderByCreatedAtDesc(Long orderId);
}
```

---

# Bước 3: HMAC-SHA512 + build URL thanh toán

Đây là **trái tim của tích hợp VNPay** và là phần dễ sai nhất.

## 3.1. HMAC-SHA512 là gì — và tại sao cần nó

Vấn đề cốt lõi: VNPay và bạn trao đổi dữ liệu qua **URL trên trình duyệt user** — kênh mà user (hoặc hacker) **nhìn thấy và sửa được**. Làm sao VNPay biết cái URL gửi sang nó không bị chỉnh? Làm sao bạn biết kết quả VNPay trả về không bị giả mạo?

→ **Chữ ký (signature)**. Cả hai bên cùng giữ một `hash-secret`. Bên gửi tính "dấu vân tay" của toàn bộ dữ liệu bằng khóa đó, đính kèm. Bên nhận tính lại bằng cùng khóa → khớp thì dữ liệu nguyên vẹn, không khớp thì vứt.

```
HMAC = Hash-based Message Authentication Code

  data = "vnp_Amount=50000&vnp_OrderInfo=..."   (query string đã sort)
  key  = "SECRETKEY..."                          (hash-secret, chỉ 2 bên biết)

  signature = HMAC_SHA512(key, data)  →  chuỗi hex 128 ký tự

  Hacker sửa amount=50000 → 1 ?
    → data đổi → signature tính ra khác hoàn toàn
    → nhưng hacker KHÔNG có key → không ký lại được
    → VNPay verify thấy không khớp → từ chối
```

> **So với Node.js:** giống hệt `crypto.createHmac('sha512', key).update(data).digest('hex')`. Trong Java thuần là class `javax.crypto.Mac`.

**Khác biệt then chốt HMAC vs hash thường (SHA-512 trần):** SHA-512 ai cũng tính được (không cần khóa) → hacker sửa data rồi hash lại bình thường. HMAC **trộn khóa bí mật vào** → không có khóa thì không ký được. Đó là chữ "keyed" trong "keyed-hash".

## 3.2. Cái bẫy chết người: **thứ tự tham số phải SORT**

```
Bạn build query: vnp_OrderInfo=...&vnp_Amount=...&vnp_TxnRef=...
VNPay verify lại: nó SORT các param theo alphabet rồi mới hash
```

Nếu bạn hash theo thứ tự bạn thêm, còn VNPay hash theo thứ tự đã sort → **hai data khác nhau → chữ ký khác nhau → fail**.

> **Trước khi ký, LUÔN sort tất cả tham số `vnp_*` theo thứ tự alphabet.** Trong Java dùng `TreeMap` (tự sort key) thay vì `HashMap`.

## 3.3. Các tham số `vnp_*` cần build

| Param | Giá trị | Ghi chú |
|---|---|---|
| `vnp_Amount` | `amount × 100` | ⚠️ VNPay tính theo **đơn vị nhỏ nhất** — phải **nhân 100**. 50.000đ → gửi `5000000` |
| `vnp_Command` | `pay` | từ config |
| `vnp_CreateDate` | `yyyyMMddHHmmss` | thời điểm tạo |
| `vnp_CurrCode` | `VND` | |
| `vnp_IpAddr` | IP client | lấy từ request |
| `vnp_Locale` | `vn` | từ config |
| `vnp_OrderInfo` | mô tả đơn | vd "Thanh toan don hang 5" |
| `vnp_OrderType` | `other` | loại hàng |
| `vnp_ReturnUrl` | return URL | từ config |
| `vnp_TmnCode` | mã merchant | từ config |
| `vnp_TxnRef` | mã GD duy nhất | = `txnRef` ta tự sinh |
| `vnp_Version` | `2.1.0` | từ config |

## 3.4. Kiến trúc class — tách trách nhiệm

```
VnpayProperties        →  @ConfigurationProperties("vnpay") — bind config typesafe
VnpayService           →  build URL thanh toán + verify chữ ký callback
  ├─ createPaymentUrl(order, ipAddr) → String url
  ├─ hmacSha512(key, data)           → String  (hàm ký lõi)
  └─ verifySignature(params)         → boolean
```

### Q&A định hướng: sinh txnRef + cấu trúc sort

**Hỏi:** `vnp_TxnRef` nên được sinh ra như thế nào?
**Trả lời (người học):** Timestamp + random.

**Hỏi:** Để đảm bảo cùng thứ tự tham số, dùng cấu trúc dữ liệu nào?
**Trả lời (người học):** TreeMap.

Cả hai đều **đúng**.

**① `txnRef` = Timestamp + random** — đúng tư duy bảo mật. Lý do không dùng id Payment:
- id tuần tự (1, 2, 3...) → hacker đoán được mã GD của người khác.
- Để sinh txnRef từ id, phải **lưu Payment trước** để DB cấp id → thêm 1 vòng DB. Còn timestamp+random sinh được **ngay trong memory**.
- VNPay khuyến nghị format có yếu tố thời gian để đối soát theo ngày. Dùng `yyyyMMddHHmmss` + vài số random.

**② `TreeMap`** — tự sort key theo alphabet → đúng yêu cầu VNPay, khỏi sort tay. `HashMap` thứ tự ngẫu nhiên (sai chữ ký), `LinkedHashMap` giữ thứ tự insert (phải tự insert đúng thứ tự — dễ quên).

## Tại sao `@ConfigurationProperties` > `@Value`

```
@Value("${vnpay.tmn-code}")  String tmnCode;     ← rải mỗi nơi 1 dòng, gõ sai key → lỗi runtime
vs.
@ConfigurationProperties("vnpay")  ← bind cả block 1 lần, typesafe, IDE autocomplete
```

> So Node.js: giống việc gom tất cả `process.env.VNPAY_*` vào một object `config.vnpay` validate sẵn lúc khởi động. Spring "relaxed binding" tự map `tmn-code` (yaml, kebab-case) ↔ `tmnCode` (Java, camelCase).

## Review `VnpayService` (vòng 1)

`bytesToHex` viết chuẩn, `hmacSha512` lõi đúng, `TreeMap` chuẩn. Nhưng **2 lỗi làm chữ ký chắc chắn sai** + 1 chỗ exception:

**🔴 Lỗi 1: `vnp_Amount` còn dư `.00`**
```java
params.put("vnp_Amount", amount.multiply(BigDecimal.valueOf(100)).toString());  // "5000000.00"
```
`amount` scale 2, nhân 100 vẫn giữ scale 2 → `.toString()` ra `"5000000.00"` có dấu chấm. VNPay yêu cầu **số nguyên thuần**. Sửa: `String.valueOf(amount.multiply(BigDecimal.valueOf(100)).longValue())`.

**🔴 Lỗi 2: KHÔNG URL-encode value**
```java
.append(entry.getValue())   // ❌ value để THÔ
```
Làm **ngược**: encode key (vô hại vì key toàn ASCII), nhưng value lại để thô. `vnp_OrderInfo` có dấu cách, `vnp_ReturnUrl` có `:` và `/` → URL gãy + VNPay verify encode value rồi mới hash → **sai chữ ký 100%**. Sửa: encode **value** bằng `US_ASCII`.

**🟡 Lỗi 3: Sai loại exception** — `BusinessRuleException` (4xx) dùng sai chỗ. HMAC fail là lỗi cấu hình server (5xx). Để `hmacSha512` tự bọc checked exception thành `IllegalStateException`, `createPaymentUrl` không cần try/catch.

**Điểm làm tốt:** `bytesToHex` pad `0` + mask `0xff` chính xác (chỗ người mới hay quên); tách hằng `ALGORITHM`.

**Kết quả sau sửa (đoạn cốt lõi):**
```java
params.put("vnp_Amount", String.valueOf(amount.multiply(BigDecimal.valueOf(100)).longValue()));
...
for (Map.Entry<String, String> entry : params.entrySet()) {
    hashData.append(entry.getKey()).append("=")
            .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8)).append("&");
}
String query = hashData.substring(0, hashData.length() - 1);
String vnp_SecureHash = hmacSha512(hashSecret, query);
return vnpayProperties.getPayUrl() + "?" + query + "&vnp_SecureHash=" + vnp_SecureHash;

public String hmacSha512(String key, String data) {
    try {
        Mac mac = Mac.getInstance(ALGORITHM);
        mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), ALGORITHM));
        return bytesToHex(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException | InvalidKeyException e) {
        throw new IllegalStateException("Khong the ky HMAC-SHA512", e);
    }
}
```

> Ghi chú: `UTF_8` vs `US_ASCII` — do `vnp_OrderInfo` đã bỏ dấu tiếng Việt nên 2 charset cho kết quả y hệt với ký tự ASCII → giữ `UTF_8` cũng chạy đúng. Điều quan trọng là **create và verify dùng CÙNG một encoding**.

---

# Bước 4: `PaymentService.createPayment()` + Controller

## Luồng

```
POST /api/payments/vnpay/create   { orderId: 5 }   (user đã login)
      │
      ▼  PaymentService.createPayment(userId, orderId, ipAddr)
  1. Load Order theo orderId
  2. CHECK ownership: order.customer.id == userId? (KHÔNG → 404)
  3. CHECK status: phải PENDING (đã CONFIRMED/CANCELLED → chặn)
  4. Sinh txnRef
  5. Tạo Payment(PENDING), save vào DB
  6. Gọi vnpayService.createPaymentUrl(...) → URL
  7. Trả URL về client
```

**Điểm thiết kế:**
- **Ownership check:** `userId` từ token, `orderId` từ request → verify đơn đúng của user. Không khớp → **404** (không phải 403 — để không lộ "đơn tồn tại nhưng của người khác").
- **Status guard:** chỉ đơn `PENDING` mới tạo URL. Đây là **idempotency ở tầng nghiệp vụ**.
- **`ipAddr` lấy ở Controller (tầng web), không ở Service.** Service là business logic thuần, không phụ thuộc HTTP → nếu sau gọi từ scheduled job (không có request) vẫn chạy.

### Q&A định hướng: double-click "Thanh toán"

**Hỏi:** User bấm "Thanh toán" 2 lần cho cùng đơn PENDING. createPayment nên xử lý sao?
**Trả lời (người học):** Tái sử dụng txnRef cũ.

Đây là chỗ cần chỉnh — vì nó **mâu thuẫn với mô hình append-only** và vướng ràng buộc VNPay:

**① VNPay không cho gửi lại txnRef đã submit.** Gửi lại đúng txnRef cũ → VNPay trả lỗi "Giao dịch đã tồn tại" (code 02). Mỗi `vnp_TxnRef` là **một phiên giao dịch dùng một lần**.

**② Mâu thuẫn với "1 Order → N Payment".** Tái sử dụng txnRef + ghi đè payment cũ → quay lại đúng mô hình `@OneToOne` đã bác bỏ.

**③ Mất lịch sử.**

**Đáp án khớp hệ thống: Tạo Payment mới mỗi lần.**
```
User bấm "Thanh toán" 3 lần cho Order #5 (đang PENDING):
  → Payment #1, #2, #3  mỗi lần 1 record + txnRef riêng, đều PENDING
Idempotency THẬT nằm ở tầng IPN:
  → dù 3 cái PENDING, chỉ cái nào VNPay báo SUCCESS mới confirm đơn (đúng 1 lần)
```

> **`createPayment` cứ tạo thoải mái (rẻ, chỉ là "ý định trả tiền"). Idempotency dồn về tầng IPN** — nơi tiền thật sự về. Tách "ý định" (nhiều, append) khỏi "xác nhận" (đúng 1 lần) là pattern chuẩn của payment.

## Review `PaymentService.createPayment` (vòng 1)

**🔴 Lỗi chí mạng: KHÔNG `save()` Payment**
```java
Payment payment = new Payment();
payment.setOrder(order); ...
// ❌ thiếu paymentRepository.save(payment);
```

Liên quan tới **dirty checking**: nó **chỉ áp dụng cho entity MANAGED** (load từ DB qua `findById`). Một entity `new Payment()` là **transient** → `@Transactional` commit cũng **không** tự lưu. Phải gọi `save()` tường minh.

So sánh: `OrderService.updateStatus` chỉ `order.setStatus(...)` mà **không** `save()` được — vì `order` là managed. Nhưng `Payment` ở đây là `new` → **bắt buộc** `save()`.

Hậu quả nếu thiếu: Payment(PENDING) + txnRef **không vào DB** → đến bước IPN, `findByTxnRef(txnRef)` → **empty** → không confirm được đơn. Sập toàn bộ idempotency.

Sửa: thêm `save()`, đặt **trước** khi build URL (đảm bảo bản ghi PENDING tồn tại rồi mới đưa URL cho user).

**🟡 `findWithItemById` là over-fetch** — kéo về toàn bộ orderItems + product + variant nhưng createPayment chỉ cần `customer`, `status`, `totalAmount` → dùng `findById` nhẹ hơn.

**Kết quả sau sửa:**
```java
Order order = orderRepository.findById(orderId).orElseThrow(() -> new ResourceNotFoundException("Order not found"));
if(!order.getCustomer().getId().equals(userId)) throw new ResourceNotFoundException("The order is not belong to user");
if(!order.getStatus().equals(OrderStatus.PENDING)) throw new BusinessRuleException("The order is not pending");
String txnRef = genTxnRef();
Payment payment = new Payment();
payment.setOrder(order);
payment.setAmount(order.getTotalAmount());
payment.setProvider(PaymentProvider.VNPAY);
payment.setTxnRef(txnRef);
paymentRepository.save(payment);
String url = vnpayService.createPaymentUrl(orderId, txnRef, order.getTotalAmount(), ipAddr);
return new PaymentUrlResponse(url);
```

`genTxnRef`:
```java
private String genTxnRef() {
    return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
         + ThreadLocalRandom.current().nextInt(100000, 999999);
}
```

Controller (`getClientIp` helper mình cho sẵn — có bẫy `X-Forwarded-For`):
```java
@PostMapping("/vnpay/create")
public ResponseEntity<BaseResponse<PaymentUrlResponse>> createPayment(
        @Valid @RequestBody CreatePaymentRequest request,
        @AuthenticationPrincipal CustomUserDetails userDetails,
        HttpServletRequest httpRequest) {
    return ResponseEntity.ok(BaseResponse.success(
        paymentService.createPayment(userDetails.getId(), request.getOrderId(), getClientIp(httpRequest))));
}
```

---

# Bước 5: IPN handler + `verifySignature` + Idempotency

## 5.1. Nhắc lại: 2 kênh trả kết quả

```
User trả tiền xong trên trang VNPay
        │
        ├──────────────► Return URL  (GET /vnpay/return)
        │                 chạy trên TRÌNH DUYỆT user
        │                 → CHỈ hiển thị UI, KHÔNG đụng DB
        │
        └──────────────► IPN  (GET /vnpay/ipn)
                          VNPay gọi THẲNG server bạn (server-to-server)
                          → source of truth
                          → verify chữ ký → idempotency → update DB
                          → trả RspCode cho VNPay
```

## 5.2. `verifySignature` hoạt động thế nào

```
VNPay gửi về:  { vnp_Amount, vnp_TxnRef, ..., vnp_SecureHash: "abc123..." }
                                              └─ chữ ký của VNPay
verifySignature:
  1. TÁCH vnp_SecureHash ra khỏi map  ◄── QUAN TRỌNG: không được ký cả chính nó
  2. (cũng bỏ vnp_SecureHashType nếu có)
  3. Đưa param còn lại vào TreeMap (auto-sort) → build hashData (encode y hệt lúc tạo URL)
  4. myHash = hmacSha512(hashSecret, hashData)
  5. so myHash == vnp_SecureHash?  → khớp = thật, lệch = giả mạo/hỏng
```

**Bẫy #1:** phải **loại `vnp_SecureHash`** (và `vnp_SecureHashType`) trước khi ký, nếu không ký cả chữ ký của chính nó → không bao giờ khớp.
**Bẫy #2:** cách encode + sort phải **giống hệt** lúc `createPaymentUrl`.
**Bẫy #3:** so sánh nên `equalsIgnoreCase` (VNPay trả hex, có thể hoa/thường khác).

## 5.3. IPN handler — trái tim của idempotency

VNPay quy định IPN phải trả về **JSON có `RspCode` + `Message`**. Nếu **không trả `00`**, VNPay coi như thất bại và **gửi lại IPN nhiều lần** (retry).

```
GET /vnpay/ipn?vnp_ResponseCode=00&vnp_TxnRef=...&vnp_SecureHash=...

  1. verifySignature(params)?
       KHÔNG → {"RspCode":"97","Message":"Invalid Checksum"}
  2. findByTxnRef(txnRef)?
       KHÔNG có → {"RspCode":"01","Message":"Order not found"}
  3. payment.status đã SUCCESS/FAILED rồi? (ĐÃ XỬ LÝ)   ◄── IDEMPOTENCY
       RỒI → {"RspCode":"02","Message":"Order already confirmed"}  (KHÔNG xử lý lại)
  4. so khớp số tiền? amount == payment.amount * 100?
       KHÔNG → {"RspCode":"04","Message":"Invalid Amount"}
  5. vnp_ResponseCode == "00"? (VNPay báo giao dịch thành công?)
       CÓ  → payment.SUCCESS + paidAt + transactionNo + order.CONFIRMED
       KHÔNG → payment.FAILED
       → cả 2 nhánh: lưu rawResponse
       → trả {"RspCode":"00","Message":"Confirm Success"}
```

**Idempotency (bước 3):** VNPay gọi IPN nhiều lần cho cùng `txnRef`. Lần đầu: payment `PENDING` → xử lý → set `SUCCESS`. Lần 2, 3...: payment đã `SUCCESS` → **bỏ qua**, chỉ trả `RspCode 02`. Nhờ vậy: **xử lý N lần = xử lý 1 lần**.

**Vì sao `RspCode` quan trọng:** nó là "biên nhận" gửi lại VNPay. Trả `00` = "tôi đã ghi nhận, ngừng gửi".

**Vì sao so khớp số tiền:** defense in depth — phòng ai đó sửa `vnp_Amount` (dù đã verify chữ ký).

## 5.4. Return handler — chỉ hiển thị UI

Verify chữ ký (để không bị user giả), rồi **chỉ trả thông báo** — **tuyệt đối không update DB**. Vì user có thể **không bao giờ** về tới return (tắt tab).

## 5.5. SecurityConfig — mở public cho IPN + return

VNPay gọi `/vnpay/ipn` và `/vnpay/return` **không có token** → phải `permitAll()`:
```java
.requestMatchers(HttpMethod.GET, "/api/payments/vnpay/ipn", "/api/payments/vnpay/return").permitAll()
```

⚠️ Mở public **không** nghĩa "ai gọi cũng tin". Ta tin vì **verify chữ ký HMAC**, không phải vì nó public. Chữ ký thay thế cho việc không có token.

### Q&A định hướng: idempotency check dựa vào đâu?

**Hỏi:** Bước "idempotency check" nên dựa vào đâu để biết đã xử lý rồi?
**Trả lời (người học):** `payment.status != PENDING`.

Đúng và gọn nhất. Lý do tốt hơn 2 phương án kia:
- **So với `order.status == CONFIRMED`:** một Order có **nhiều** Payment. Nhìn `order.status` không biết *chính xác txnRef này* đã xử lý chưa. Check trên **bản ghi Payment của đúng txnRef** là chính xác nhất.
- **So với bảng log riêng:** thừa. Bản thân `Payment` (tra theo `txnRef` unique) **đã là** cái log; `status` chính là cờ "đã xử lý chưa".

### Gotcha Jackson với `VnpayIpnResponse`

VNPay đòi JSON đúng `{"RspCode":"..","Message":".."}` (viết hoa chữ đầu). Lombok sinh getter `getRspCode()` → Jackson suy ra field `rspCode` (thường) → sai. Phải ép bằng `@JsonProperty`:
```java
@Getter @AllArgsConstructor
public class VnpayIpnResponse {
    @JsonProperty("RspCode") private String rspCode;
    @JsonProperty("Message") private String message;
}
```

---

## Người học hỏi: VNPay biết IPN URL của mình bằng cách nào?

> "Trong code chưa thấy dùng đến ipn-url, nếu không thêm vào param gửi sang VNPay thì VNPay biết URL của mình là gì để gọi vào?"

Câu hỏi rất hay. Trả lời ngắn:

> **IPN URL KHÔNG gửi kèm mỗi giao dịch. Nó được đăng ký MỘT LẦN trên cổng quản trị merchant của VNPay**, gắn với `vnp_TmnCode`.

```
┌─ return-url ────────────────────────────────────────────────┐
│ GỬI THEO TỪNG GIAO DỊCH — là param vnp_ReturnUrl trong URL   │
│ → là nơi TRÌNH DUYỆT user redirect về (client-side)          │
│ → CÓ trong createPaymentUrl                                  │
└──────────────────────────────────────────────────────────────┘
┌─ ipn-url ───────────────────────────────────────────────────┐
│ ĐĂNG KÝ MỘT LẦN trên portal VNPay                            │
│ → VNPay LƯU SẴN theo TmnCode → KHÔNG gửi theo giao dịch      │
│ → KHÔNG có trong code build URL                              │
└──────────────────────────────────────────────────────────────┘
```

**Vì sao thiết kế khác nhau:**
- **Return URL để linh hoạt UI:** cùng merchant có nhiều luồng (web, mobile, app) → cho gửi động.
- **IPN URL để bảo mật:** đây là kênh đụng vào tiền + DB. Nếu cho gửi động theo request thì hacker sửa param `vnp_IpnUrl` để VNPay gọi vào server của hắn. Nên VNPay **khóa cứng** ở phía nó.

**`ipn-url` trong `application.yaml` để làm gì?** Hiện chưa được code dùng — đóng vai trò **tài liệu tham chiếu** (ghi lại URL đã/sẽ đăng ký trên portal).

**Hệ quả khi test:** VNPay gọi từ server của nó → **không thể gọi tới `localhost:8080`**. Phải dùng **tunnel** (ngrok) để phơi localhost ra URL public.

---

## Người học hỏi: Dùng ngrok thì phải cập nhật lại IPN URL trên portal?

> "Nếu dùng ngrok thì phải cập nhật lại link mới cho ipn-url trên portal VNPay à?"

**Có**, nếu dùng ngrok free kiểu cũ (subdomain ngẫu nhiên mỗi lần chạy). Cách né:

**Cách 1: URL tunnel cố định** — ngrok free static domain (1 domain tĩnh miễn phí/tài khoản): `ngrok http --url=your-name.ngrok-free.app 8080` → đăng ký portal 1 lần. Hoặc Cloudflare Tunnel.

**Cách 2 (hay nhất cho học): tự giả lập IPN — KHÔNG cần ngrok.** Vì **mình cũng giữ `hash-secret` giống VNPay** → tự ký một request IPN giả rồi bắn thẳng vào `localhost:8080` bằng Postman:
```
Bình thường:  VNPay ──(ký bằng secret)──► IPN của bạn
Khi test:     BẠN   ──(tự ký bằng secret)──► IPN của bạn (localhost)
```
→ `verifySignature` pass (chữ ký hợp lệ), toàn bộ `handleIpn` chạy đúng ngay trên localhost.

**`return-url` khi đổi domain:** đọc từ config gửi theo request → chỉ sửa `application.yaml`, **không đụng portal**. Chỉ IPN mới cần portal.

---

## Người học hỏi: `@RequestParam Map` — Spring tự tạo map param?

> "Khi request đến dạng url?a=1&b=2&c=3, dùng `@RequestParam Map<String, String> params` thì Spring tự tạo map theo key/value đúng không? Muốn lấy từng giá trị thì dùng `@RequestParam` với đúng key?"

Đúng cả hai ý.

**1. `@RequestParam Map<String, String>`** — Spring gom hết param vào map, value **đã URL-decode**. (So Express: chính là `req.query`.)

**2. Lấy từng giá trị** — `@RequestParam("vnp_Amount") String amount`. ⚠️ Bẫy: tên param VNPay có dấu gạch dưới (`vnp_Amount`) không hợp lệ làm tên biến camelCase → **bắt buộc ghi rõ key trong ngoặc**. Mặc định `required=true` → thiếu → 400.

**3. Vì sao IPN dùng `Map` chứ không liệt kê từng `@RequestParam`?**
- **Verify chữ ký cần TẤT CẢ param, kể cả cái chưa biết tên.** VNPay qua các version có thể **thêm param mới**. Hardcode 15 `@RequestParam` → VNPay thêm param → không đưa vào hashData → **chữ ký lệch, fail**. Dùng `Map` thì **có gì nhận nấy** → luôn ký đúng.
- **Gọn.**

→ Quy tắc: **không kiểm soát/không biết trước** tập param (callback bên thứ ba) → dùng `Map`. **Biết chính xác** vài param cố định (API của mình) → dùng `@RequestParam` từng cái.

**4. Gotcha decode/encode:** `Map` chứa value đã decode, verify lại encode → vòng "decode rồi encode lại". Với param ASCII thì ổn định → chữ ký khớp.

---

## Review bước 5 (vòng 1)

`verifySignature` gần đúng, nhưng `handleIpn` có **2 bug chí mạng** + 1 bug BigDecimal tinh vi.

**🔴 Bug 1: sai key `txnRef`**
```java
String txnRef = params.get("txnRef");   // ❌ phải là "vnp_TxnRef"
```
VNPay gửi key `vnp_TxnRef`. `params.get("txnRef")` → luôn `null` → **mọi IPN trả "01 Order not found"**.

**🔴 Bug 2: nhân 100 hai lần**
```java
BigDecimal vnpAmount = new BigDecimal(params.get("vnp_Amount")).multiply(BigDecimal.valueOf(100));  // ❌
```
`vnp_Amount` VNPay gửi **đã** là tiền × 100. Nhân 100 lần nữa → sai → **mọi IPN trả "04 Invalid Amount"**.

**🔴 Bug 3: `BigDecimal.equals()` so cả scale**
```java
new BigDecimal("5000000").equals(new BigDecimal("5000000.00"))   // → FALSE ❗ (scale 0 vs scale 2)
```
`payment.getAmount()` load từ DB có scale 2 (`5000000.00`), `new BigDecimal("5000000")` scale 0 → dù giá trị bằng, `.equals()` vẫn FALSE.

**Cách sửa — so bằng `long`:**
```java
long vnpAmount = Long.parseLong(params.get("vnp_Amount"));            // đã ×100
long expected  = payment.getAmount().multiply(BigDecimal.valueOf(100)).longValue();
if (vnpAmount != expected) return new VnpayIpnResponse("04", "Invalid Amount");
```

> Ghi nhớ vàng: **so sánh giá trị BigDecimal luôn dùng `compareTo(x) == 0`, KHÔNG dùng `.equals()`** (trừ khi cố ý muốn phân biệt scale).

**🟡 Bug 4: `verifySignature` mutate map của caller + thiếu remove `vnp_SecureHashType`**
```java
params.remove("vnp_SecureHash");            // ❌ xóa thẳng trên map gốc
```
`verifySignature` được kỳ vọng chỉ "đọc và kiểm tra" nhưng đang mutate map gốc → `rawResponse` mất chữ ký, và nếu Spring trả map immutable thì ném exception. Sửa: **copy trước, xóa trên bản copy**, xóa cả `vnp_SecureHashType`.

**✅ Chỗ làm ĐÚNG (tinh tế):**
- **Trả `"00"` kể cả khi payment FAILED** — `RspCode 00` nghĩa là *"tôi đã NHẬN và xử lý IPN"*, không phải *"giao dịch thành công"*. Giao dịch fail nhưng IPN hợp lệ → vẫn báo 00 để VNPay ngừng retry.
- **Không `save()`** — `payment` từ `findByTxnRef` + `order` từ `payment.getOrder()` đều managed → dirty checking tự flush.
- **Idempotency check** đúng vị trí, đúng thứ tự.
- `handleReturn` không đụng DB.

**Kết quả sau sửa — `verifySignature`:**
```java
public boolean verifySignature(Map<String, String> params) {
    String hashSecret = vnpayProperties.getHashSecret();
    String received = params.get("vnp_SecureHash");
    Map<String, String> sortedParams = new TreeMap<>(params);   // copy trước
    sortedParams.remove("vnp_SecureHash");
    sortedParams.remove("vnp_SecureHashType");
    StringBuilder sb = new StringBuilder();
    for (Map.Entry<String, String> e : sortedParams.entrySet()) {
        sb.append(e.getKey()).append("=")
          .append(URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8)).append("&");
    }
    String query = sb.substring(0, sb.length() - 1);
    return hmacSha512(hashSecret, query).equalsIgnoreCase(received);
}
```

`handleIpn` (sau sửa) + `handleReturn` + Controller endpoints (`vnpayIpn` trả thẳng `VnpayIpnResponse` không bọc `BaseResponse`; `vnpayReturn` bọc `BaseResponse`). Build pass.

---

## Người học hỏi: `vnp_TransactionNo` mình không gửi đi thì sao 2 hash bằng nhau?

> "vnp_TransactionNo là giá trị VNPay gửi cho mình, nếu chúng ta không gửi sang thì khi so sánh 2 giá trị hash làm sao bằng nhau được?"

Câu hỏi ẩn giả định: *"chữ ký lúc create và chữ ký lúc IPN là cùng một cái."* → **Không phải.** Đây là **HAI chữ ký độc lập, ký trên HAI bộ params khác nhau**, hai thời điểm, hai bên khác nhau.

```
┌─ CHIỀU ĐI (create) ─────────────────────────────────────────┐
│ BẠN ký params CỦA BẠN (không có vnp_TransactionNo):          │
│   sign( vnp_Amount, vnp_TxnRef, vnp_ReturnUrl, ... ) → hash A│
│ VNPay verify hash A bằng chính bộ params đó → khớp → OK      │
└──────────────────────────────────────────────────────────────┘
     ── (user thanh toán, VNPay xử lý, sinh thông tin MỚI) ──
┌─ CHIỀU VỀ (IPN) ────────────────────────────────────────────┐
│ VNPay ký params CỦA VNPAY (THÊM field VNPay tự sinh):        │
│   sign( vnp_Amount, vnp_TxnRef, vnp_TransactionNo,          │
│         vnp_ResponseCode, vnp_PayDate, vnp_BankCode, ... )   │
│   → hash B → gửi kèm IPN cho BẠN                             │
│ BẠN verify hash B bằng CHÍNH bộ params IPN đó → khớp → OK    │
└──────────────────────────────────────────────────────────────┘
```

**Mấu chốt: hash A và hash B KHÔNG liên quan gì nhau.** Không so hash B với hash A. Lúc IPN, tính một hash **mới** từ **đúng params VNPay vừa gửi** (gồm `vnp_TransactionNo`), rồi so với hash VNPay cũng tính từ đúng bộ đó → khớp.

**Vì sao "không gửi `vnp_TransactionNo` đi" không gây lệch:** vì nó **không tồn tại ở chiều đi**. Nó là thứ VNPay **tự sinh sau khi giao dịch xảy ra**. Ở chiều về, nó xuất hiện trong bộ params IPN, và **cả hai bên đều đưa nó vào hash**.

**Đây chính là lý do `verifySignature` dùng `Map` "nhận gì ký nấy"** — VNPay thêm cả đống field mình không gửi (`vnp_TransactionNo`, `vnp_ResponseCode`, `vnp_PayDate`, `vnp_BankCode`, `vnp_BankTranNo`, `vnp_CardType`...). Nếu hardcode "chỉ ký lại field tôi từng gửi" → bỏ sót → sai chữ ký.

---

## Đối xứng vs bất đối xứng (bonus — người học hỏi về shared secret)

> "Khi VNPay cấp mã bí mật để hash, VNPay cũng phải giữ mã này để hash bên nó và so sánh chuỗi đúng không?"

Chính xác. Đây gọi là **shared secret** (khóa dùng chung) — đặc điểm cốt lõi của HMAC (**mã hóa đối xứng**).

| | Symmetric (HMAC — VNPay dùng) | Asymmetric (RSA/ECDSA) |
|---|---|---|
| Số khóa | **1** khóa chung | **2** khóa (private + public) |
| Ai ký / ai verify | Cùng 1 khóa làm cả 2 | Ký bằng private, verify bằng public |
| Tốc độ | Rất nhanh | Chậm hơn |
| Điều kiện | Phải **chia sẻ trước** khóa an toàn | Public key phát tán thoải mái |

**Cầu nối:** JWT của dự án dùng `HS256` = HMAC-SHA256 — cùng cơ chế shared secret (chỉ khác: JWT cùng một server giữ khóa, VNPay hai bên khác nhau cùng giữ).

**Hệ quả bảo mật:**
1. **Secret KHÔNG BAO GIỜ rời server** (không xuống frontend, không commit git, không log). Ai có secret là giả được cả hai chiều.
2. Chữ ký chứng minh "người có secret tạo ra cái này" — không phân biệt được mình với VNPay (vì chung khóa).
3. Mỗi merchant một secret riêng.
4. Đây là lý do **tự giả lập IPN được** — hệ quả trực tiếp của tính đối xứng.

---

## Tổng kết Giai đoạn 5

**Đã hoàn thành (code xong, test E2E để sau):**
- `Payment` entity (append-only, 1 Order → N Payment) + enums `PaymentStatus`/`PaymentProvider`.
- `PaymentRepository` (`findByTxnRef` idempotency key, `findByOrderIdOrderByCreatedAtDesc`).
- `VnpayProperties` (`@ConfigurationProperties`) + config `vnpay` trong `application.yaml`.
- `VnpayService`: `createPaymentUrl` (sort TreeMap + URL-encode + HMAC-SHA512), `hmacSha512`, `verifySignature` (nhận gì ký nấy).
- `PaymentService`: `createPayment` (ownership + status guard + save PENDING), `handleIpn` (verify → tra txnRef → idempotency → khớp tiền → cập nhật, trả RspCode), `handleReturn` (chỉ UI).
- `PaymentController`: 3 endpoint (`vnpay/create` cần login, `vnpay/ipn` + `vnpay/return` public).
- `SecurityConfig`: mở public IPN + return.

**Kiến thức cốt lõi rút ra:**
- Payment tách khỏi Order (append-only financial log); idempotency dồn về tầng IPN.
- IPN là source of truth; Return URL chỉ hiển thị UI.
- HMAC-SHA512 = shared secret đối xứng; sort param (TreeMap) + URL-encode value nhất quán 2 chiều.
- `@Enumerated(EnumType.STRING)` bắt buộc; `BigDecimal` so bằng `compareTo`/`long` chứ không `.equals()`.
- Dirty checking chỉ áp cho entity managed → `new` entity phải `save()`.
- IPN URL đăng ký ở portal (không gửi theo request); return URL gửi theo request.
- Verify callback "nhận gì ký nấy" bằng `Map` → robust khi VNPay thêm field.

**Việc còn nợ (làm sau):**
- Test E2E: tự ký IPN giả bắn vào localhost (không cần ngrok), test toàn luồng create → return → ipn → idempotency (gọi IPN 2 lần).
- Xử lý retry/timeout đơn (đơn PENDING quá lâu không thanh toán → auto-cancel + hoàn stock).
- Refactor DRY: `buildHashData` dùng chung giữa `createPaymentUrl` và `verifySignature` (đang lặp).
