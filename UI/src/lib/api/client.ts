import axios, { AxiosError, type AxiosRequestConfig, type InternalAxiosRequestConfig } from "axios"
import { useAuthStore } from "@/store/authStore"
import type { AuthResponse, BaseResponse, RefreshTokenRequest } from "@/types/api"

const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "/api"

export const apiClient = axios.create({
  baseURL: BASE_URL,
  headers: { "Content-Type": "application/json" },
})

apiClient.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = useAuthStore.getState().accessToken
  if (token && config.headers) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

let refreshPromise: Promise<string> | null = null

async function performRefresh(): Promise<string> {
  const refreshToken = useAuthStore.getState().refreshToken
  if (!refreshToken) throw new Error("No refresh token")

  const body: RefreshTokenRequest = { token: refreshToken }
  const { data } = await axios.post<BaseResponse<AuthResponse>>(
    `${BASE_URL}/auth/refresh`,
    body,
    { headers: { "Content-Type": "application/json" } }
  )
  if (!data?.data?.accessToken) throw new Error("Refresh failed")

  useAuthStore.getState().setTokens({
    accessToken: data.data.accessToken,
    refreshToken: data.data.refreshToken,
  })
  return data.data.accessToken
}

apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const original = error.config as (AxiosRequestConfig & { _retry?: boolean }) | undefined
    const status = error.response?.status

    const isAuthRoute = original?.url?.includes("/auth/")
    if (status !== 401 || !original || original._retry || isAuthRoute) {
      return Promise.reject(error)
    }

    original._retry = true
    try {
      refreshPromise ??= performRefresh().finally(() => {
        refreshPromise = null
      })
      const newToken = await refreshPromise
      original.headers = { ...(original.headers ?? {}), Authorization: `Bearer ${newToken}` }
      return apiClient.request(original)
    } catch (refreshErr) {
      useAuthStore.getState().clear()
      if (typeof window !== "undefined") {
        window.location.href = "/login"
      }
      return Promise.reject(refreshErr)
    }
  }
)

export function unwrap<T>(res: { data: BaseResponse<T> }): T {
  return res.data.data
}
