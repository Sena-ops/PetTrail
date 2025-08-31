import { createContext } from 'preact'
import { useContext, useEffect, useState } from 'preact/hooks'

export type Route = 'pets' | 'achievements' | 'map' | 'walk-details' | 'walks'

interface RouterContextType {
  currentRoute: Route
  navigate: (route: Route, params?: Record<string, string>) => void
  getParams: () => URLSearchParams
}

const RouterContext = createContext<RouterContextType>({
  currentRoute: 'pets',
  navigate: () => {},
  getParams: () => new URLSearchParams()
})

export const useRouter = () => useContext(RouterContext)

export const RouterProvider = ({ children }: { children: preact.ComponentChildren }) => {
  const [currentRoute, setCurrentRoute] = useState<Route>('pets')

  const navigate = (route: Route, params?: Record<string, string>) => {
    let hash = `#${route}`
    if (params && Object.keys(params).length > 0) {
      const searchParams = new URLSearchParams(params)
      hash += `?${searchParams.toString()}`
    }
    window.location.hash = hash
    setCurrentRoute(route)
  }

  const getParams = () => {
    const hash = window.location.hash
    const queryIndex = hash.indexOf('?')
    if (queryIndex !== -1) {
      return new URLSearchParams(hash.substring(queryIndex + 1))
    }
    return new URLSearchParams()
  }

  useEffect(() => {
    const handleHashChange = () => {
      const hash = window.location.hash
      const routeEnd = hash.indexOf('?')
      const routePart = routeEnd !== -1 ? hash.substring(1, routeEnd) : hash.substring(1)
      
      if (routePart && ['pets', 'achievements', 'map', 'walk-details', 'walks'].includes(routePart)) {
        setCurrentRoute(routePart as Route)
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
    <RouterContext.Provider value={{ currentRoute, navigate, getParams }}>
      {children}
    </RouterContext.Provider>
  )
}
