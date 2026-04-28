import { Link, Outlet } from "react-router-dom"
import { ShoppingBag } from "lucide-react"

export function AuthLayout() {
  return (
    <div className="flex min-h-dvh flex-col bg-muted/40">
      <header className="border-b border-border bg-background">
        <div className="mx-auto flex h-16 max-w-7xl items-center px-4 md:px-8">
          <Link to="/" className="flex items-center gap-2 font-heading text-xl font-semibold tracking-tight">
            <ShoppingBag className="h-5 w-5" aria-hidden="true" />
            <span>Marketplace</span>
          </Link>
        </div>
      </header>
      <main className="flex flex-1 items-center justify-center px-4 py-12">
        <Outlet />
      </main>
    </div>
  )
}
