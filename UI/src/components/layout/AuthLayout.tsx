import { Suspense } from "react"
import { Link, Outlet } from "react-router-dom"
import { ShoppingBagIcon } from "lucide-react"
import { Skeleton } from "@/components/ui/skeleton"

export function AuthLayout() {
  return (
    <div className="flex min-h-dvh flex-col bg-muted/40">
      <header className="border-b border-border bg-background">
        <div className="container-page flex h-16 items-center">
          <Link
            to="/"
            className="flex items-center gap-2 font-heading text-xl font-semibold tracking-tight"
          >
            <ShoppingBagIcon className="size-5" aria-hidden="true" />
            <span>Marketplace</span>
          </Link>
        </div>
      </header>
      <main className="flex flex-1 items-center justify-center px-4 py-12">
        <Suspense fallback={<Skeleton className="h-96 w-full max-w-md rounded-xl" />}>
          <Outlet />
        </Suspense>
      </main>
    </div>
  )
}
