import { createBrowserRouter } from "react-router-dom"
import { MainLayout } from "@/components/layout/MainLayout"
import { AuthLayout } from "@/components/layout/AuthLayout"
import { SellerLayout } from "@/components/layout/SellerLayout"
import { ProtectedRoute } from "@/components/common/ProtectedRoute"
import { RouteError } from "@/components/common/RouteError"
import { HomePage } from "@/pages/HomePage"
import { NotFoundPage } from "@/pages/NotFoundPage"
import {
  AdminCategoriesPage,
  CartPage,
  CheckoutPage,
  LoginPage,
  MyReviewsPage,
  OrderDetailPage,
  OrdersPage,
  PaymentReturnPage,
  ProductDetailPage,
  ProductListPage,
  ProfilePage,
  RegisterPage,
  SellerOrdersPage,
  SellerProductFormPage,
  SellerProductListPage,
} from "@/routes/lazyPages"

export const router = createBrowserRouter([
  {
    element: <MainLayout />,
    // Lỗi render/lazy-load trong bất kỳ page con nào cũng hiện trang lỗi thân thiện
    // thay vì màn hình trắng.
    errorElement: <RouteError />,
    children: [
      { path: "/", element: <HomePage /> },
      { path: "/products", element: <ProductListPage /> },
      { path: "/products/:id", element: <ProductDetailPage /> },
      // VNPay redirect trình duyệt về đây (public) — trang tự gọi API để lấy kết quả.
      { path: "/payment/vnpay/return", element: <PaymentReturnPage /> },
      {
        element: <ProtectedRoute />,
        children: [
          { path: "/cart", element: <CartPage /> },
          { path: "/checkout", element: <CheckoutPage /> },
          { path: "/orders", element: <OrdersPage /> },
          { path: "/orders/:id", element: <OrderDetailPage /> },
          { path: "/account/profile", element: <ProfilePage /> },
          { path: "/account/reviews", element: <MyReviewsPage /> },
        ],
      },
    ],
  },
  {
    element: <AuthLayout />,
    errorElement: <RouteError />,
    children: [
      { path: "/login", element: <LoginPage /> },
      { path: "/register", element: <RegisterPage /> },
    ],
  },
  {
    element: (
      <ProtectedRoute requireRole={["SELLER", "ADMIN"]}>
        <SellerLayout />
      </ProtectedRoute>
    ),
    errorElement: <RouteError />,
    children: [
      { path: "/seller/products", element: <SellerProductListPage /> },
      { path: "/seller/products/new", element: <SellerProductFormPage /> },
      { path: "/seller/products/:id/edit", element: <SellerProductFormPage /> },
      { path: "/seller/orders", element: <SellerOrdersPage /> },
    ],
  },
  {
    element: (
      <ProtectedRoute requireRole="ADMIN">
        <SellerLayout />
      </ProtectedRoute>
    ),
    errorElement: <RouteError />,
    children: [{ path: "/admin/categories", element: <AdminCategoriesPage /> }],
  },
  { path: "*", element: <NotFoundPage /> },
])
