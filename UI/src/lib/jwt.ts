import type { JwtPayload } from "@/types/api"

export function decodeJwt(token: string): JwtPayload | null {
  try {
    const [, payload] = token.split(".")
    if (!payload) return null
    const json = atob(payload.replace(/-/g, "+").replace(/_/g, "/"))
    return JSON.parse(decodeURIComponent(escape(json))) as JwtPayload
  } catch {
    return null
  }
}
