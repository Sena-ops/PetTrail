import { RouterProvider, useRouter } from './router.tsx'
import { NavBar } from './components/NavBar'
import { PetsPage } from './components/PetsPage'
import { AchievementsPage } from './components/AchievementsPage'
import { MapWalkPage } from './components/MapWalkPage'
import WalkDetails from './components/WalkDetails'
import { WalksPage } from './components/WalksPage'
import { LoginPage } from './components/LoginPage'
import { RegisterPage } from './components/RegisterPage'
import { AuthProvider, useAuth } from './contexts/AuthContext'
import { ThemeProvider } from './contexts/ThemeContext'
import { useState } from 'preact/hooks'

const AppContent = () => {
  const { currentRoute } = useRouter()
  const { isAuthenticated, loading } = useAuth()
  const [authMode, setAuthMode] = useState<'login' | 'register'>('login')

  const renderPage = () => {
    switch (currentRoute) {
      case 'pets':
        return <PetsPage />
      case 'achievements':
        return <AchievementsPage />
      case 'map':
        return <MapWalkPage />
      case 'walk-details':
        return <WalkDetails />
      case 'walks':
        return <WalksPage />
      default:
        return <PetsPage />
    }
  }

  if (loading) {
    return (
      <div class="min-h-screen flex items-center justify-center">
        <div class="text-center">
          <div class="animate-spin rounded-full h-32 w-32 border-b-2 border-indigo-600 mx-auto"></div>
          <p class="mt-4 text-gray-600">Loading...</p>
        </div>
      </div>
    )
  }

  if (!isAuthenticated) {
    return (
      <div id="app">
        {authMode === 'login' ? (
          <LoginPage 
            onLogin={() => window.location.reload()} 
            onSwitchToRegister={() => setAuthMode('register')} 
          />
        ) : (
          <RegisterPage 
            onRegister={() => window.location.reload()} 
            onSwitchToLogin={() => setAuthMode('login')} 
          />
        )}
      </div>
    )
  }

  return (
    <div id="app">
      <NavBar />
      <main class="main-content">
        {renderPage()}
      </main>
    </div>
  )
}

export const App = () => {
  return (
    <ThemeProvider>
      <AuthProvider>
        <RouterProvider>
          <AppContent />
        </RouterProvider>
      </AuthProvider>
    </ThemeProvider>
  )
}
