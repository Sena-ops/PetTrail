import { useState, useRef } from 'preact/hooks'
import { MapView } from './MapView'
import { WalkControls } from './WalkControls'
import { GeoPoint } from '../lib/geo'
import { composeAndShare, ShareMetrics } from '../lib/sharePng'
import L from 'leaflet'

export const MapWalkPage = () => {
  const [currentPosition, setCurrentPosition] = useState<GeoPoint | null>(null)
  const [routePoints, setRoutePoints] = useState<GeoPoint[]>([])
  const [isFullscreen, setIsFullscreen] = useState(false)
  const [walkResult, setWalkResult] = useState<any>(null)
  const [showSummary, setShowSummary] = useState(false)
  const [statusMessage, setStatusMessage] = useState<string | null>(null)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)
  const mapRef = useRef<L.Map | null>(null)

  const handleWalkStart = (walkId: string) => {
    setRoutePoints([])
    setCurrentPosition(null)
    setWalkResult(null)
    setShowSummary(false)
    setStatusMessage(`Walk ${walkId} started`)
  }

  const handleWalkStop = (result: any) => {
    setWalkResult(result)
    setShowSummary(true)
    setStatusMessage(`Walk completed! Distance: ${formatDistance(result.distance)}`)
  }

  const handlePointReceived = (point: GeoPoint) => {
    setCurrentPosition(point)
    setRoutePoints(prev => [...prev, point])
  }

  const handleStatusChange = (status: string) => {
    setStatusMessage(status)
  }

  const handleError = (error: string) => {
    setErrorMessage(error)
    setTimeout(() => setErrorMessage(null), 5000)
  }

  const handleMapReady = (map: L.Map) => {
    mapRef.current = map
  }

  const handleToggleFullscreen = () => {
    setIsFullscreen(!isFullscreen)
  }

  const handleShareWalk = async () => {
    if (!mapRef.current || !walkResult) return

    try {
      const mapContainer = mapRef.current.getContainer()
      const metrics: ShareMetrics = {
        distance: walkResult.distance,
        duration: walkResult.duration,
        averagePace: walkResult.averagePace
      }

      await composeAndShare({
        mapContainer,
        metrics,
        badges: walkResult.badges,
        petName: 'Your Pet', // Could be enhanced to get actual pet name
        walkDate: new Date().toLocaleDateString()
      })
    } catch (error) {
      console.error('Failed to share walk:', error)
      setErrorMessage('Failed to share walk')
    }
  }

  const formatDistance = (meters: number) => {
    if (meters >= 1000) {
      return `${(meters / 1000).toFixed(1)} km`
    }
    return `${Math.round(meters)} m`
  }

  const formatDuration = (seconds: number) => {
    const hours = Math.floor(seconds / 3600)
    const minutes = Math.floor((seconds % 3600) / 60)
    if (hours > 0) {
      return `${hours}h ${minutes}m`
    }
    return `${minutes}m`
  }

  const formatPace = (secondsPerKm: number) => {
    const minutes = Math.floor(secondsPerKm / 60)
    const seconds = Math.floor(secondsPerKm % 60)
    return `${minutes}:${seconds.toString().padStart(2, '0')}/km`
  }

  return (
    <div class="page">
      <div class="page-title">Map & Walk</div>

      {/* Status messages */}
      {statusMessage && (
        <div class="card" style="background-color: #e8f5e8; color: #2e7d32; border: 1px solid #c8e6c9;">
          <div class="card-content">{statusMessage}</div>
        </div>
      )}

      {errorMessage && (
        <div class="card" style="background-color: #ffebee; color: #c62828; border: 1px solid #ffcdd2;">
          <div class="card-title">Error</div>
          <div class="card-content">{errorMessage}</div>
        </div>
      )}

      {/* Walk summary modal */}
      {showSummary && walkResult && (
        <div class="card" style="background: linear-gradient(135deg, #fff3e0, #ffe0b2); border: 2px solid #ff9800;">
          <div class="card-title">🎉 Walk Completed!</div>
          <div class="card-content">
            <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(150px, 1fr)); gap: var(--spacing-md); margin-bottom: var(--spacing-md);">
              <div style="text-align: center;">
                <div style="font-size: 1.5rem; font-weight: bold; color: #333;">
                  {formatDistance(walkResult.distance)}
                </div>
                <div style="font-size: 0.875rem; color: #666;">Distance</div>
              </div>
              <div style="text-align: center;">
                <div style="font-size: 1.5rem; font-weight: bold; color: #333;">
                  {formatDuration(walkResult.duration)}
                </div>
                <div style="font-size: 0.875rem; color: #666;">Duration</div>
              </div>
              <div style="text-align: center;">
                <div style="font-size: 1.5rem; font-weight: bold; color: #333;">
                  {formatPace(walkResult.averagePace)}
                </div>
                <div style="font-size: 0.875rem; color: #666;">Pace</div>
              </div>
            </div>

            {walkResult.badges && walkResult.badges.length > 0 && (
              <div style="margin-bottom: var(--spacing-md);">
                <div style="font-weight: 600; margin-bottom: var(--spacing-sm);">
                  🏆 Achievements Earned:
                </div>
                <div style="display: flex; gap: var(--spacing-sm); flex-wrap: wrap;">
                  {walkResult.badges.map((badge: string) => (
                    <div style="background: linear-gradient(135deg, #ff7a00, #ff9500); color: white; padding: 6px 12px; border-radius: 16px; font-size: 0.875rem; font-weight: 500;">
                      {badge}
                    </div>
                  ))}
                </div>
              </div>
            )}

            <div style="display: flex; gap: var(--spacing-md);">
              <button class="btn btn-secondary" onClick={handleShareWalk}>
                📤 Share Walk
              </button>
              <button 
                class="btn" 
                onClick={() => setShowSummary(false)}
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Map */}
      <MapView
        onMapReady={handleMapReady}
        currentPosition={currentPosition}
        routePoints={routePoints}
        isFullscreen={isFullscreen}
        onToggleFullscreen={handleToggleFullscreen}
      />

      {/* Walk controls */}
      <WalkControls
        onWalkStart={handleWalkStart}
        onWalkStop={handleWalkStop}
        onPointReceived={handlePointReceived}
        onStatusChange={handleStatusChange}
        onError={handleError}
      />
    </div>
  )
}
