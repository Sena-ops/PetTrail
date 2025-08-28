import { RouterProvider, useRouter } from './router.tsx'
import { NavBar } from './components/NavBar'
import { PetsPage } from './components/PetsPage'
import { AchievementsPage } from './components/AchievementsPage'
import { MapWalkPage } from './components/MapWalkPage'

const AppContent = () => {
  const { currentRoute } = useRouter()

  const renderPage = () => {
    switch (currentRoute) {
      case 'pets':
        return <PetsPage />
      case 'achievements':
        return <AchievementsPage />
      case 'map':
        return <MapWalkPage />
      default:
        return <PetsPage />
    }
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
    <RouterProvider>
      <AppContent />
    </RouterProvider>
  )
}
