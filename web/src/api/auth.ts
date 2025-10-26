import { http } from './http'

export interface LoginRequest {
  email: string
  password: string
}

export interface RegisterRequest {
  email: string
  password: string
  firstName: string
  lastName: string
}

export interface AuthResponse {
  token: string
  tokenType: string
  userId: string
  email: string
  firstName: string
  lastName: string
  fullName: string
  role: 'USER' | 'ADMIN'
}

export interface User {
  id: string
  email: string
  firstName: string
  lastName: string
  fullName: string
  role: 'USER' | 'ADMIN'
}

class AuthService {
  private token: string | null = null
  private user: User | null = null

  constructor() {
    // Load token and user from localStorage on initialization
    this.token = localStorage.getItem('auth_token')
    const userStr = localStorage.getItem('auth_user')
    if (userStr) {
      try {
        this.user = JSON.parse(userStr)
      } catch (e) {
        console.error('Failed to parse user from localStorage:', e)
        this.clearAuth()
      }
    }
  }

  async login(credentials: LoginRequest): Promise<AuthResponse> {
    const response = await http.post<AuthResponse>('/auth/login', credentials)
    this.setAuth(response)
    return response
  }

  async register(userData: RegisterRequest): Promise<AuthResponse> {
    const response = await http.post<AuthResponse>('/auth/register', userData)
    this.setAuth(response)
    return response
  }

  async logout(): Promise<void> {
    this.clearAuth()
  }

  isAuthenticated(): boolean {
    return this.token !== null && this.user !== null
  }

  getToken(): string | null {
    return this.token
  }

  getUser(): User | null {
    return this.user
  }

  getAuthHeaders(): Record<string, string> {
    if (!this.token) {
      return {}
    }
    return {
      'Authorization': `Bearer ${this.token}`
    }
  }

  private setAuth(authResponse: AuthResponse): void {
    this.token = authResponse.token
    this.user = {
      id: authResponse.userId,
      email: authResponse.email,
      firstName: authResponse.firstName,
      lastName: authResponse.lastName,
      fullName: authResponse.fullName,
      role: authResponse.role
    }
    
    localStorage.setItem('auth_token', this.token)
    localStorage.setItem('auth_user', JSON.stringify(this.user))
  }

  private clearAuth(): void {
    this.token = null
    this.user = null
    localStorage.removeItem('auth_token')
    localStorage.removeItem('auth_user')
  }
}

export const authService = new AuthService()
