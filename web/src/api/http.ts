import { authService } from './auth'

const API_BASE = import.meta.env.VITE_API_BASE || '/api'

interface ApiError {
  code: string
  message: string
  details?: Array<{ field: string; issue: string }>
}

class HttpError extends Error {
  constructor(
    public status: number,
    public message: string,
    public code?: string,
    public details?: Array<{ field: string; issue: string }>
  ) {
    super(message)
    this.name = 'HttpError'
  }
}

async function request<T>(
  endpoint: string,
  options: RequestInit = {}
): Promise<T> {
  const url = `${API_BASE}${endpoint}`
  
  const config: RequestInit = {
    headers: {
      'Content-Type': 'application/json',
      ...authService.getAuthHeaders(),
      ...options.headers,
    },
    ...options,
  }

  try {
    const response = await fetch(url, config)
    
    if (!response.ok) {
      let errorData: ApiError | null = null
      
      try {
        errorData = await response.json()
      } catch {
        // If response is not JSON, use status text
      }
      
      throw new HttpError(
        response.status,
        errorData?.message || response.statusText,
        errorData?.code,
        errorData?.details
      )
    }
    
    // Handle empty responses
    const contentType = response.headers.get('content-type')
    if (contentType && contentType.includes('application/json')) {
      return await response.json()
    }
    
    return {} as T
  } catch (error) {
    if (error instanceof HttpError) {
      throw error
    }
    
    // Network or other errors
    throw new HttpError(
      0,
      error instanceof Error ? error.message : 'Network error',
      'NETWORK_ERROR'
    )
  }
}

export const http = {
  get: <T>(endpoint: string): Promise<T> => 
    request<T>(endpoint, { method: 'GET' }),
    
  post: <T>(endpoint: string, data?: any): Promise<T> => 
    request<T>(endpoint, { 
      method: 'POST', 
      body: data ? JSON.stringify(data) : undefined 
    }),
    
  put: <T>(endpoint: string, data?: any): Promise<T> => 
    request<T>(endpoint, { 
      method: 'PUT', 
      body: data ? JSON.stringify(data) : undefined 
    }),
    
  delete: <T>(endpoint: string): Promise<T> => 
    request<T>(endpoint, { method: 'DELETE' }),
}

export { HttpError }
