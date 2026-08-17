import { lazy } from "react"

/**
 * Mỗi route một chunk riêng: lần tải đầu chỉ kéo trang chủ, khu quản trị và trang
 * thanh toán tải khi thực sự cần. File này chỉ export component để không phá Fast Refresh.
 * Các page dùng named export nên phải map lại thành `default` cho React.lazy.
 */
export const LoginPage = lazy(() =>
  import("@/pages/auth/LoginPage").then((m) => ({ default: m.LoginPage }))
)
export const RegisterPage = lazy(() =>
  import("@/pages/auth/RegisterPage").then((m) => ({ default: m.RegisterPage }))
)
export const ProductListPage = lazy(() =>
  import("@/pages/products/ProductListPage").then((m) => ({ default: m.ProductListPage }))
)
export const ProductDetailPage = lazy(() =>
  import("@/pages/products/ProductDetailPage").then((m) => ({ default: m.ProductDetailPage }))
)
export const CartPage = lazy(() =>
  import("@/pages/cart/CartPage").then((m) => ({ default: m.CartPage }))
)
export const CheckoutPage = lazy(() =>
  import("@/pages/checkout/CheckoutPage").then((m) => ({ default: m.CheckoutPage }))
)
export const OrdersPage = lazy(() =>
  import("@/pages/orders/OrdersPage").then((m) => ({ default: m.OrdersPage }))
)
export const OrderDetailPage = lazy(() =>
  import("@/pages/orders/OrderDetailPage").then((m) => ({ default: m.OrderDetailPage }))
)
export const PaymentReturnPage = lazy(() =>
  import("@/pages/payment/PaymentReturnPage").then((m) => ({ default: m.PaymentReturnPage }))
)
export const ProfilePage = lazy(() =>
  import("@/pages/account/ProfilePage").then((m) => ({ default: m.ProfilePage }))
)
export const MyReviewsPage = lazy(() =>
  import("@/pages/account/MyReviewsPage").then((m) => ({ default: m.MyReviewsPage }))
)
export const SellerProductListPage = lazy(() =>
  import("@/pages/seller/SellerProductListPage").then((m) => ({ default: m.SellerProductListPage }))
)
export const SellerProductFormPage = lazy(() =>
  import("@/pages/seller/SellerProductFormPage").then((m) => ({ default: m.SellerProductFormPage }))
)
export const SellerOrdersPage = lazy(() =>
  import("@/pages/seller/SellerOrdersPage").then((m) => ({ default: m.SellerOrdersPage }))
)
export const AdminCategoriesPage = lazy(() =>
  import("@/pages/admin/AdminCategoriesPage").then((m) => ({ default: m.AdminCategoriesPage }))
)
