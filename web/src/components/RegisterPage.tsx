import { useState } from 'preact/hooks'
import { authService, RegisterRequest } from '../api/auth'
import { HttpError } from '../api/http'

interface RegisterPageProps {
  onRegister: () => void
  onSwitchToLogin: () => void
}

export function RegisterPage({ onRegister, onSwitchToLogin }: RegisterPageProps) {
  const [formData, setFormData] = useState<RegisterRequest>({
    email: '',
    password: '',
    firstName: '',
    lastName: ''
  })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const handleSubmit = async (e: Event) => {
    e.preventDefault()
    setLoading(true)
    setError(null)

    try {
      await authService.register(formData)
      onRegister()
    } catch (err) {
      if (err instanceof HttpError) {
        setError(err.message)
      } else {
        setError('An unexpected error occurred')
      }
    } finally {
      setLoading(false)
    }
  }

  const handleInputChange = (e: Event) => {
    const target = e.target as HTMLInputElement
    setFormData(prev => ({
      ...prev,
      [target.name]: target.value
    }))
  }

  return (
    <div class="auth-container">
      <div class="auth-card">
        <div class="auth-header">
          <div class="auth-logo">
            <div class="paw-prints">
              <span class="paw-print paw-print-1">🐾</span>
              <span class="paw-print paw-print-2">🐾</span>
            </div>
            <h1 class="auth-title">PetTrail</h1>
          </div>
          <h2 class="auth-subtitle">Join the adventure!</h2>
          <p class="auth-description">
            Create your account to start tracking your pet's journeys
          </p>
        </div>

        <form class="auth-form" onSubmit={handleSubmit}>
          {error && (
            <div class="auth-error">
              <svg class="error-icon" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clip-rule="evenodd"></path>
              </svg>
              {error}
            </div>
          )}

          <div class="form-row">
            <div class="form-group">
              <label for="firstName" class="form-label">First Name</label>
              <div class="input-wrapper">
                <svg class="input-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path>
                </svg>
                <input
                  id="firstName"
                  name="firstName"
                  type="text"
                  autocomplete="given-name"
                  required
                  class="form-input"
                  placeholder="Enter your first name"
                  value={formData.firstName}
                  onInput={handleInputChange}
                />
              </div>
            </div>

            <div class="form-group">
              <label for="lastName" class="form-label">Last Name</label>
              <div class="input-wrapper">
                <svg class="input-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path>
                </svg>
                <input
                  id="lastName"
                  name="lastName"
                  type="text"
                  autocomplete="family-name"
                  required
                  class="form-input"
                  placeholder="Enter your last name"
                  value={formData.lastName}
                  onInput={handleInputChange}
                />
              </div>
            </div>
          </div>

          <div class="form-group">
            <label for="email" class="form-label">Email Address</label>
            <div class="input-wrapper">
              <svg class="input-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 12a4 4 0 10-8 0 4 4 0 008 0zm0 0v1.5a2.5 2.5 0 005 0V12a9 9 0 10-9 9m4.5-1.206a8.959 8.959 0 01-4.5 1.207"></path>
              </svg>
              <input
                id="email"
                name="email"
                type="email"
                autocomplete="email"
                required
                class="form-input"
                placeholder="Enter your email"
                value={formData.email}
                onInput={handleInputChange}
              />
            </div>
          </div>

          <div class="form-group">
            <label for="password" class="form-label">Password</label>
            <div class="input-wrapper">
              <svg class="input-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"></path>
              </svg>
              <input
                id="password"
                name="password"
                type="password"
                autocomplete="new-password"
                required
                minLength={8}
                class="form-input"
                placeholder="Create a password (min 8 characters)"
                value={formData.password}
                onInput={handleInputChange}
              />
            </div>
            <p class="form-hint">Password must be at least 8 characters long</p>
          </div>

          <button
            type="submit"
            disabled={loading}
            class="auth-button"
          >
            {loading ? (
              <>
                <svg class="button-spinner" fill="none" viewBox="0 0 24 24">
                  <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                  <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
                Creating account...
              </>
            ) : (
              'Create Account'
            )}
          </button>
        </form>

        <div class="auth-footer">
          <p class="auth-switch-text">
            Already have an account?{' '}
            <button
              onClick={onSwitchToLogin}
              class="auth-switch-link"
            >
              Sign in here
            </button>
          </p>
        </div>
      </div>
    </div>
  )
}
