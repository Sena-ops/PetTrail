import { useEffect, useRef, useState } from 'preact/hooks'
import L from 'leaflet'
import { GeoPoint } from '../lib/geo'

interface MapViewProps {
  onMapReady?: (map: L.Map) => void
  currentPosition?: GeoPoint | null
  routePoints?: GeoPoint[]
  isFullscreen?: boolean
  onToggleFullscreen?: () => void
}

export const MapView = ({ 
  onMapReady, 
  currentPosition, 
  routePoints = [], 
  isFullscreen = false,
  onToggleFullscreen 
}: MapViewProps) => {
  const mapRef = useRef<HTMLDivElement>(null)
  const mapInstanceRef = useRef<L.Map | null>(null)
  const markerRef = useRef<L.Marker | null>(null)
  const polylineRef = useRef<L.Polyline | null>(null)
  const [mapReady, setMapReady] = useState(false)
  const [hasInitialLocation, setHasInitialLocation] = useState(false)
  const [isLoadingLocation, setIsLoadingLocation] = useState(true)

  useEffect(() => {
    if (!mapRef.current || mapInstanceRef.current) return

    // Initialize map
    const map = L.map(mapRef.current, {
      preferCanvas: true,
      zoomControl: true,
      attributionControl: true
    })

    // Add OSM tiles
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
      maxZoom: 19
    }).addTo(map)

    // Create current position marker
    const marker = L.marker([0, 0], {
      icon: L.divIcon({
        className: 'location-marker',
        html: '<div style="width: 20px; height: 20px; background-color: #2196F3; border: 3px solid white; border-radius: 50%; box-shadow: 0 2px 4px rgba(0,0,0,0.3);"></div>',
        iconSize: [20, 20],
        iconAnchor: [10, 10]
      })
    }).addTo(map)

    // Create route polyline
    const polyline = L.polyline([], {
      color: '#ff7a00',
      weight: 4,
      opacity: 0.8
    }).addTo(map)

    mapInstanceRef.current = map
    markerRef.current = marker
    polylineRef.current = polyline

    setMapReady(true)
    onMapReady?.(map)

    // Get user's location immediately when map loads
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          const latLng = L.latLng(position.coords.latitude, position.coords.longitude)
          marker.setLatLng(latLng)
          map.setView(latLng, 16)
          setHasInitialLocation(true)
          setIsLoadingLocation(false)
        },
        (error) => {
          console.warn('Could not get initial location:', error)
          // Fallback to a reasonable default (e.g., a major city)
          const fallbackLocation = L.latLng(40.7128, -74.0060) // New York City
          map.setView(fallbackLocation, 10)
          setHasInitialLocation(true)
          setIsLoadingLocation(false)
        },
        {
          enableHighAccuracy: true,
          timeout: 10000,
          maximumAge: 60000
        }
      )
    } else {
      // Fallback if geolocation is not supported
      const fallbackLocation = L.latLng(40.7128, -74.0060) // New York City
      map.setView(fallbackLocation, 10)
      setHasInitialLocation(true)
      setIsLoadingLocation(false)
    }

    return () => {
      map.remove()
      mapInstanceRef.current = null
      markerRef.current = null
      polylineRef.current = null
    }
  }, [onMapReady])

  // Update current position marker
  useEffect(() => {
    if (!mapReady || !currentPosition || !markerRef.current) return

    const latLng = L.latLng(currentPosition.latitude, currentPosition.longitude)
    markerRef.current.setLatLng(latLng)

    // Center map on first position if we haven't set initial location yet
    if (!hasInitialLocation) {
      mapInstanceRef.current?.setView(latLng, 16)
      setHasInitialLocation(true)
    }
  }, [currentPosition, mapReady, hasInitialLocation])

  // Update route polyline
  useEffect(() => {
    if (!mapReady || !polylineRef.current) return

    if (routePoints.length > 0) {
      const latLngs = routePoints.map(point => 
        L.latLng(point.latitude, point.longitude)
      )
      polylineRef.current.setLatLngs(latLngs)

      // Fit map to show entire route
      if (latLngs.length > 1) {
        const bounds = L.latLngBounds(latLngs)
        mapInstanceRef.current?.fitBounds(bounds, { padding: [20, 20] })
      }
    } else {
      polylineRef.current.setLatLngs([])
    }
  }, [routePoints, mapReady])

  return (
    <div 
      ref={mapRef} 
      class={`map-container ${isFullscreen ? 'fullscreen' : ''}`}
      style={{ height: isFullscreen ? '100vh' : '400px' }}
    >
      {isLoadingLocation && (
        <div style={{
          position: 'absolute',
          top: '50%',
          left: '50%',
          transform: 'translate(-50%, -50%)',
          zIndex: 1000,
          background: 'rgba(255, 255, 255, 0.9)',
          padding: '12px 20px',
          borderRadius: '8px',
          boxShadow: '0 2px 8px rgba(0,0,0,0.2)',
          display: 'flex',
          alignItems: 'center',
          gap: '8px',
          fontSize: '14px',
          color: '#333'
        }}>
          <div style={{
            width: '16px',
            height: '16px',
            border: '2px solid #2196F3',
            borderTop: '2px solid transparent',
            borderRadius: '50%',
            animation: 'spin 1s linear infinite'
          }}></div>
          Getting your location...
        </div>
      )}
      
      {onToggleFullscreen && (
        <button
          onClick={onToggleFullscreen}
          style={{
            position: 'absolute',
            top: '10px',
            right: '10px',
            zIndex: 1000,
            background: 'white',
            border: 'none',
            borderRadius: '4px',
            padding: '8px',
            cursor: 'pointer',
            boxShadow: '0 2px 4px rgba(0,0,0,0.2)'
          }}
        >
          {isFullscreen ? '⤓' : '⤢'}
        </button>
      )}
    </div>
  )
}
