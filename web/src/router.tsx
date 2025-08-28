import { createContext } from 'preact'
import { useContext, useEffect, useState } from 'preact/hooks'

export type Route = 'pets' | 'achievements' | 'map'

interface RouterContextType {
  currentRoute: Route
  navigate: (route: Route) => void
}

const RouterContext = createContext<RouterContextType>({
  currentRoute: 'pets',
  navigate: () => {}
})

export const useRouter = () => useContext(RouterContext)

export const RouterProvider = ({ children }: { children: preact.ComponentChildren }) => {
  const [currentRoute, setCurrentRoute] = useState<Route>('pets')

  const navigate = (route: Route) => {
    window.location.hash = `#${route}`
    setCurrentRoute(route)
  }

  useEffect(() => {
    const handleHashChange = () => {
      const hash = window.location.hash.slice(1) as Route
      if (hash && ['pets', 'achievements', 'map'].includes(hash)) {
        setCurrentRoute(hash)
      } else {
        // Default to pets if no valid hash
        window.location.hash = '#pets'
        setCurrentRoute('pets')
      }
    }

    // Set initial route
    handleHashChange()

    // Listen for hash changes
    window.addEventListener('hashchange', handleHashChange)
    return () => window.removeEventListener('hashchange', handleHashChange)
  }, [])

  return (
    <RouterContext.Provider value={{ currentRoute, navigate }}>
      {children}
    </RouterContext.Provider>
  )
}
