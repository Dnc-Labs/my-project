import { createBrowserRouter } from "react-router-dom"
import { MainLayout } from "@/components/layout/MainLayout"
import { AuthLayout } from "@/components/layout/AuthLayout"
import { SellerLayout } from "@/components/layout/SellerLayout"
import { ProtectedRoute } from "@/components/common/ProtectedRoute"
import { HomePage } from "@/pages/HomePage"
import { LoginPage } from "@/pages/auth/LoginPage"
import { RegisterPage } from "@/pages/auth/RegisterPage"
import { ProductListPage } from "@/pages/products/ProductListPage"
import { ProductDetailPage } from "@/pages/products/ProductDetailPage"
import { SellerProductListPage } from "@/pages/seller/SellerProductListPage"
import { SellerProductFormPage } from "@/pages/seller/SellerProductFormPage"
import { NotFoundPage } from "@/pages/NotFoundPage"

export const router = createBrowserRouter([
  {
    element: <MainLayout />,
    children: [
      { path: "/", element: <HomePage /> },
      { path: "/products", element: <ProductListPage /> },
      { path: "/products/:id", element: <ProductDetailPage /> },
    ],
  },
  {
    element: <AuthLayout />,
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
    children: [
      { path: "/seller/products", element: <SellerProductListPage /> },
      { path: "/seller/products/new", element: <SellerProductFormPage /> },
      { path: "/seller/products/:id/edit", element: <SellerProductFormPage /> },
    ],
  },
  { path: "*", element: <NotFoundPage /> },
])
