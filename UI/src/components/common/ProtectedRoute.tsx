import { Navigate, useLocation } from "react-router-dom"
import type { ReactNode } from "react"
import { useAuthStore } from "@/store/authStore"
import type { Role } from "@/types/api"

interface Props {
  children: ReactNode
  requireRole?: Role | Role[]
}

export function ProtectedRoute({ children, requireRole }: Props) {
  const location = useLocation()
  const { user, isAuthenticated } = useAuthStore()

  if (!isAuthenticated()) {
    return <Navigate to="/login" replace state={{ from: location }} />
  }

  if (requireRole && user) {
    const allowed = Array.isArray(requireRole) ? requireRole : [requireRole]
    if (!allowed.includes(user.role)) {
      return <Navigate to="/" replace />
    }
  }

  return <>{children}</>
}
