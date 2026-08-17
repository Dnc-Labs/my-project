import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query"
import { login as loginApi, register as registerApi } from "@/lib/api/auth"
import { getUser, updateUser } from "@/lib/api/users"
import { useAuthStore } from "@/store/authStore"
import { decodeJwt } from "@/lib/jwt"
import type {
  AuthResponse,
  AuthUser,
  CreateUserRequest,
  LoginRequest,
  UpdateUserRequest,
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
      const user: AuthUser = { id: claims.userId, email: claims.sub, role: claims.role }
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

/** Hồ sơ đầy đủ của user đang đăng nhập — JWT chỉ có id/email/role nên phải gọi API riêng. */
export function useMe() {
  const userId = useAuthStore((s) => s.user?.id)
  return useQuery({
    queryKey: ["users", userId],
    queryFn: () => getUser(userId!),
    enabled: Boolean(userId),
  })
}

export function useUpdateProfile() {
  const userId = useAuthStore((s) => s.user?.id)
  const queryClient = useQueryClient()
  return useMutation<UserResponse, Error, UpdateUserRequest>({
    mutationFn: (payload) => updateUser(userId!, payload),
    onSuccess: (data) => {
      queryClient.setQueryData(["users", userId], data)
    },
  })
}
