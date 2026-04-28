import { useMutation } from "@tanstack/react-query"
import { login as loginApi, register as registerApi } from "@/lib/api/auth"
import { useAuthStore } from "@/store/authStore"
import { decodeJwt } from "@/lib/jwt"
import type {
  AuthResponse,
  AuthUser,
  CreateUserRequest,
  LoginRequest,
  UserResponse,
} from "@/types/api"

interface LoginResult {
  tokens: AuthResponse
  user: AuthUser
}

export function useLogin() {
  const setAuth = useAuthStore((s) => s.setAuth)
  return useMutation<LoginResult, Error, LoginRequest>({
    mutationFn: async (payload) => {
      const tokens = await loginApi(payload)
      const claims = decodeJwt(tokens.accessToken)
      if (!claims) throw new Error("Token không hợp lệ")
      const user: AuthUser = { email: claims.sub, role: claims.role }
      return { tokens, user }
    },
    onSuccess: ({ tokens, user }) => {
      setAuth({
        accessToken: tokens.accessToken,
        refreshToken: tokens.refreshToken,
        user,
      })
    },
  })
}

export function useRegister() {
  return useMutation<UserResponse, Error, CreateUserRequest>({
    mutationFn: registerApi,
  })
}
