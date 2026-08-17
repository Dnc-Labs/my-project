import { Suspense } from "react"
import { Outlet, ScrollRestoration } from "react-router-dom"
import { Header } from "./Header"
import { Footer } from "./Footer"
import { RouteFallback } from "@/components/common/RouteFallback"

export function MainLayout() {
  return (
    <div className="flex min-h-dvh flex-col">
      {/* Đi tới trang mới thì về đầu trang, còn bấm back thì trả lại đúng vị trí cũ. */}
      <ScrollRestoration />
      {/* Người dùng bàn phím/screen reader nhảy thẳng vào nội dung, không phải tab qua cả header. */}
      <a
        href="#main-content"
        className="sr-only focus:not-sr-only focus:absolute focus:top-3 focus:left-3 focus:z-50 focus:rounded-md focus:bg-background focus:px-4 focus:py-2 focus:text-sm focus:font-medium focus:ring-[3px] focus:ring-ring/50"
      >
        Bỏ qua tới nội dung chính
      </a>
      <Header />
      <main id="main-content" className="flex-1">
        <Suspense fallback={<RouteFallback />}>
          <Outlet />
        </Suspense>
      </main>
      <Footer />
    </div>
  )
}
