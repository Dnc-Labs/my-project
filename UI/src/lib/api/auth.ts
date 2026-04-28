import { apiClient, unwrap } from "./client"
import type {
  AuthResponse,
  BaseResponse,
  CreateUserRequest,
  LoginRequest,
  UserResponse,
} from "@/types/api"

export async function login(payload: LoginRequest): Promise<AuthResponse> {
  const res = await apiClient.post<BaseResponse<AuthResponse>>("/auth/login", payload)
  return unwrap(res)
}

export async function register(payload: CreateUserRequest): Promise<UserResponse> {
  const res = await apiClient.post<BaseResponse<UserResponse>>("/users", payload)
  return unwrap(res)
}
