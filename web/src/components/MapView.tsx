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

    // Set initial view to a default location (can be updated when we get first position)
    map.setView([0, 0], 13)

    setMapReady(true)
    onMapReady?.(map)

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

    // Center map on first position
    if (routePoints.length === 0) {
      mapInstanceRef.current?.setView(latLng, 16)
    }
  }, [currentPosition, mapReady, routePoints.length])

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
