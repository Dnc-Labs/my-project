import { create } from "zustand"
import { persist } from "zustand/middleware"
import type { AuthUser, Role } from "@/types/api"

interface AuthState {
  accessToken: string | null
  refreshToken: string | null
  user: AuthUser | null
  setAuth: (payload: { accessToken: string; refreshToken: string; user: AuthUser }) => void
  setTokens: (payload: { accessToken: string; refreshToken: string }) => void
  clear: () => void
  isAuthenticated: () => boolean
  hasRole: (role: Role) => boolean
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      accessToken: null,
      refreshToken: null,
      user: null,
      setAuth: ({ accessToken, refreshToken, user }) =>
        set({ accessToken, refreshToken, user }),
      setTokens: ({ accessToken, refreshToken }) =>
        set({ accessToken, refreshToken }),
      clear: () => set({ accessToken: null, refreshToken: null, user: null }),
      isAuthenticated: () => Boolean(get().accessToken && get().user),
      hasRole: (role) => get().user?.role === role,
    }),
    {
      name: "ecommerce-auth",
      partialize: (state) => ({
        accessToken: state.accessToken,
        refreshToken: state.refreshToken,
        user: state.user,
      }),
    }
  )
)
