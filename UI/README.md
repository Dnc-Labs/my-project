# UI — Frontend cho E-commerce API

Giao diện React cho backend Spring Boot ở thư mục `../api`. Bao gồm **storefront** (mua sắm, giỏ hàng, đặt hàng, thanh toán VNPay, đánh giá) và **khu quản lý** cho SELLER/ADMIN.

## Công nghệ

| Thành phần | Lựa chọn |
|---|---|
| Build | Vite 8 + React 19 + TypeScript |
| Style | Tailwind CSS v4 (design token qua CSS variable) + shadcn/ui (new-york) |
| Data fetching | TanStack Query v5 |
| State đăng nhập | Zustand (persist vào localStorage) |
| Form | React Hook Form + Zod |
| Router | React Router v7 (`createBrowserRouter`, lazy theo route) |

## Chạy dự án

```bash
# 1. Database + backend (ở thư mục gốc repo)
docker compose up -d mysql
cd api && ./mvnw spring-boot:run     # http://localhost:8080

# 2. Frontend
cd UI
npm install
npm run dev                          # http://localhost:5173
```

Frontend gọi backend qua **dev proxy của Vite** (`/api` và `/uploads` → `http://localhost:8080`), nên backend **không cần cấu hình CORS**. Đổi cổng backend bằng biến môi trường:

```bash
VITE_API_PROXY_TARGET=http://localhost:9090 npm run dev
```

## Lệnh

```bash
npm run dev      # dev server
npm run build    # tsc -b && vite build
npm run lint     # eslint
npm run preview  # xem thử bản build
```

## Cấu trúc

```
src/
├─ lib/api/        # 1 file cho mỗi nhóm endpoint (products, cart, orders, reviews...)
├─ hooks/          # React Query hook bọc lib/api + xử lý cache/toast
├─ components/
│  ├─ ui/          # primitive shadcn (button, dialog, sheet, tabs...)
│  ├─ common/      # dùng chung theo domain (RatingStars, Pagination, EmptyState...)
│  ├─ products/    # thẻ sản phẩm, gallery, bộ lọc
│  ├─ reviews/     # form + danh sách đánh giá
│  ├─ seller/      # quản lý ảnh, biến thể
│  └─ layout/      # Header, Footer, MainLayout, SellerLayout, AuthLayout
├─ pages/          # 1 thư mục cho mỗi khu vực chức năng
├─ routes/         # khai báo lazy import cho từng route
├─ store/          # Zustand (auth)
└─ types/api.ts    # mirror DTO của backend
```

## Bản đồ màn hình ↔ API

| Màn hình | Endpoint |
|---|---|
| Trang chủ, danh sách sản phẩm | `GET /api/products` (keyword, categoryId, minPrice, maxPrice, minRating, page, size, sort) |
| Chi tiết sản phẩm | `GET /api/products/{id}`, `GET /api/products/{id}/variants` |
| Đánh giá | `GET/POST /api/products/{id}/reviews`, `GET .../can-review`, `PUT/DELETE /api/reviews/{id}`, `GET /api/reviews/me` |
| Giỏ hàng | `GET/DELETE /api/carts`, `POST /api/carts/items`, `PUT/DELETE /api/carts/items/{id}` |
| Đặt hàng & đơn hàng | `POST /api/orders`, `GET /api/orders`, `GET /api/orders/{id}`, `PUT /api/orders/{id}/status` |
| Thanh toán | `POST /api/payments/vnpay/create`, `GET /api/payments/vnpay/return` |
| Hồ sơ | `GET/PUT /api/users/{id}` |
| Quản lý sản phẩm | `POST/PUT/DELETE /api/products`, variants, images |
| Quản lý danh mục (ADMIN) | `POST/PUT/DELETE /api/categories` |

## Xác thực

- Đăng nhập trả `accessToken` + `refreshToken`; `id`, `email`, `role` được đọc từ claim của access token (`userId`, `sub`, `role`).
- `apiClient` tự gắn `Authorization: Bearer`, và khi gặp **401** sẽ tự gọi `/api/auth/refresh` một lần rồi phát lại request. Nhiều request 401 cùng lúc dùng chung một promise refresh để không refresh nhiều lần.
- Refresh thất bại → xoá token và chuyển về `/login`.

## Thanh toán VNPay

`payment-gate.vnpay.return-url` trong `api/src/main/resources/application.yaml` trỏ về **frontend**:

```yaml
return-url: ${VNPAY_RETURN_URL:http://localhost:5173/payment/vnpay/return}
```

Nhờ vậy sau khi thanh toán, VNPay redirect trình duyệt về trang kết quả có giao diện; trang này gọi ngược `GET /api/payments/vnpay/return` để backend verify chữ ký. Trạng thái đơn thật vẫn do **IPN** (server-to-server) quyết định.

## Giới hạn còn phụ thuộc backend

- **Danh sách sản phẩm của seller**: `ProductFilterRequest` chưa có `sellerId`, nên trang quản lý phải lấy tối đa 100 sản phẩm rồi lọc ở client.
- **Đơn hàng phía seller**: chưa có endpoint liệt kê đơn cho người bán, và `GET /api/orders/{id}` chỉ cho chủ đơn. Trang "Xử lý đơn hàng" vì thế thao tác theo mã đơn qua `PUT /api/orders/{id}/status`.
- **`POST /api/carts/items`** trả về item có `id: null` (DTO được map trước khi flush), nên sau khi thêm giỏ, frontend refetch lại thay vì dùng thẳng response.
