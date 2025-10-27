import { useState, useEffect } from 'preact/hooks'
import { petsApi, Pet } from '../api/pets'
import { walksApi, WalkPointsBatchRequest } from '../api/walks'
import { geoService, GeoPoint } from '../lib/geo'
import { idbQueue } from '../lib/idbQueue'
import { HttpError } from '../api/http'

interface WalkControlsProps {
  onWalkStart?: (walkId: string) => void
  onWalkStop?: (result: any) => void
  onPointReceived?: (point: GeoPoint) => void
  onStatusChange?: (status: string) => void
  onError?: (error: string) => void
}

export const WalkControls = ({ 
  onWalkStart, 
  onWalkStop, 
  onPointReceived,
  onStatusChange,
  onError 
}: WalkControlsProps) => {
  const [pets, setPets] = useState<Pet[]>([])
  const [selectedPetId, setSelectedPetId] = useState<string | null>(null)
  const [isRecording, setIsRecording] = useState(false)
  const [walkId, setWalkId] = useState<string | null>(null)
  const [startTime, setStartTime] = useState<number | null>(null)
  const [recordingTime, setRecordingTime] = useState(0)
  const [pointsCount, setPointsCount] = useState(0)
  const [queuedBatches, setQueuedBatches] = useState(0)
  const [isOnline, setIsOnline] = useState(navigator.onLine)
  const [isSyncing, setIsSyncing] = useState(false)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    loadPets()
    setupOnlineStatus()
    setupTimeUpdate()
  }, [])

  useEffect(() => {
    if (walkId) {
      updateQueueStats()
    }
  }, [walkId])

  const loadPets = async () => {
    try {
      setLoading(true)
      const petsList = await petsApi.listPets()
      setPets(petsList)
      if (petsList.length > 0) {
        setSelectedPetId(petsList[0].id)
      }
    } catch (err) {
      onError?.(err instanceof HttpError ? err.message : 'Failed to load pets')
    } finally {
      setLoading(false)
    }
  }

  const setupOnlineStatus = () => {
    const updateOnlineStatus = () => {
      setIsOnline(navigator.onLine)
      if (navigator.onLine && walkId) {
        syncQueuedBatches()
      }
    }

    window.addEventListener('online', updateOnlineStatus)
    window.addEventListener('offline', updateOnlineStatus)

    return () => {
      window.removeEventListener('online', updateOnlineStatus)
      window.removeEventListener('offline', updateOnlineStatus)
    }
  }

  const setupTimeUpdate = () => {
    const interval = setInterval(() => {
      if (startTime) {
        setRecordingTime(Math.floor((Date.now() - startTime) / 1000))
      }
    }, 1000)

    return () => clearInterval(interval)
  }

  const updateQueueStats = async () => {
    if (!walkId) return
    try {
      const stats = await idbQueue.getStats(walkId)
      setQueuedBatches(stats.count)
    } catch (error) {
      console.error('Failed to update queue stats:', error)
    }
  }

  const syncQueuedBatches = async () => {
    if (!walkId || !isOnline) return

    setIsSyncing(true)
    try {
      const result = await idbQueue.drain(walkId, async (batch: WalkPointsBatchRequest) => {
        try {
          await walksApi.sendPoints(walkId, batch)
          return true
        } catch (error) {
          console.error('Failed to send batch:', error)
          return false
        }
      })

      if (result.sent > 0) {
        onStatusChange?.(`Synced ${result.sent} batches`)
      }
    } catch (error) {
      console.error('Failed to sync queued batches:', error)
    } finally {
      setIsSyncing(false)
      updateQueueStats()
    }
  }

  const handleStartWalk = async () => {
    if (!selectedPetId) {
      onError?.('Please select a pet first')
      return
    }

    try {
      onStatusChange?.('Starting walk...')
      
      // Start walk on server
      const response = await walksApi.startWalk(selectedPetId)
      setWalkId(response.walkId)
      setStartTime(Date.now())
      setIsRecording(true)
      setPointsCount(0)
      setRecordingTime(0)

      // Start GPS tracking
      geoService.startWatch({
        onPoint: async (point: GeoPoint) => {
          setPointsCount(prev => prev + 1)
          onPointReceived?.(point)

          // Convert to API format
          const apiPoint = {
            latitude: point.latitude,
            longitude: point.longitude,
            timestamp: new Date(point.timestamp).toISOString(),
            elevation: point.elevation
          }

          // Try to send immediately if online
          if (isOnline) {
            try {
              await walksApi.sendPoints(response.walkId, { points: [apiPoint] })
            } catch (error) {
              // Queue for later if failed
              await idbQueue.enqueue(response.walkId, { points: [apiPoint] })
              updateQueueStats()
            }
          } else {
            // Queue for later if offline
            await idbQueue.enqueue(response.walkId, { points: [apiPoint] })
            updateQueueStats()
          }
        },
        onError: (error: GeolocationPositionError) => {
          let errorMessage = 'Location error'
          switch (error.code) {
            case error.PERMISSION_DENIED:
              errorMessage = 'Location permission denied'
              break
            case error.POSITION_UNAVAILABLE:
              errorMessage = 'Location unavailable'
              break
            case error.TIMEOUT:
              errorMessage = 'Location timeout'
              break
          }
          onError?.(errorMessage)
        },
        outlierFilter: true,
        maxSpeed: 50
      })

      onWalkStart?.(response.walkId)
      onStatusChange?.('Recording walk...')
    } catch (err) {
      onError?.(err instanceof HttpError ? err.message : 'Failed to start walk')
    }
  }

  const handleStopWalk = async () => {
    if (!walkId) return

    try {
      onStatusChange?.('Stopping walk...')
      
      // Stop GPS tracking
      geoService.stopWatch()
      setIsRecording(false)

      // Sync any remaining queued batches
      if (isOnline) {
        await syncQueuedBatches()
      }

      // Stop walk on server
      const result = await walksApi.stopWalk(walkId)
      
      // Clear queue
      await idbQueue.clearWalk(walkId)
      setQueuedBatches(0)

      // Reset state
      setWalkId(null)
      setStartTime(null)
      setRecordingTime(0)
      setPointsCount(0)

      onWalkStop?.(result)
      onStatusChange?.('Walk completed!')
    } catch (err) {
      onError?.(err instanceof HttpError ? err.message : 'Failed to stop walk')
    }
  }

  const handleForceStop = async () => {
    if (!walkId) return

    try {
      onStatusChange?.('Force stopping walk...')
      
      // Stop GPS tracking
      geoService.stopWatch()
      setIsRecording(false)

      // Try to sync any remaining queued batches
      if (isOnline) {
        try {
          await syncQueuedBatches()
        } catch (error) {
          console.warn('Failed to sync batches during force stop:', error)
        }
      }

      // Try to stop walk on server, but don't fail if it doesn't work
      try {
        await walksApi.stopWalk(walkId)
      } catch (error) {
        console.warn('Failed to stop walk on server during force stop:', error)
      }
      
      // Clear queue
      try {
        await idbQueue.clearWalk(walkId)
      } catch (error) {
        console.warn('Failed to clear queue during force stop:', error)
      }
      setQueuedBatches(0)

      // Reset state
      setWalkId(null)
      setStartTime(null)
      setRecordingTime(0)
      setPointsCount(0)

      onStatusChange?.('Walk force stopped')
    } catch (err) {
      console.error('Error during force stop:', err)
      // Even if there's an error, reset the local state
      setWalkId(null)
      setStartTime(null)
      setRecordingTime(0)
      setPointsCount(0)
      setIsRecording(false)
      geoService.stopWatch()
      onStatusChange?.('Walk force stopped (with errors)')
    }
  }

  const formatTime = (seconds: number) => {
    const hours = Math.floor(seconds / 3600)
    const minutes = Math.floor((seconds % 3600) / 60)
    const secs = seconds % 60
    
    if (hours > 0) {
      return `${hours}:${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
    }
    return `${minutes}:${secs.toString().padStart(2, '0')}`
  }

  if (loading) {
    return (
      <div class="walk-controls">
        <div class="loading">
          <div class="spinner"></div>
          Loading pets...
        </div>
      </div>
    )
  }

  return (
    <div class="walk-controls">
      <div class="control-panel">
        <div class="status-display">
          <div class="status-text">
            {isRecording ? 'Recording Walk' : 'Ready to Record'}
          </div>
          <div class="status-details">
            {isRecording ? `Walk ID: ${walkId}` : 'Select a pet and start recording'}
          </div>
          {!isOnline && (
            <div style="color: var(--warning-color); font-size: 0.875rem; margin-top: var(--spacing-xs);">
              ⚠ Offline mode - data will sync when connection is restored
            </div>
          )}
        </div>

        <div class="pet-selection">
          <label class="pet-label">Select Pet:</label>
          <select
            class="form-select"
            value={selectedPetId || ''}
            onChange={(e) => setSelectedPetId((e.target as HTMLSelectElement).value || null)}
            disabled={isRecording}
          >
            {pets.map(pet => (
              <option key={pet.id} value={pet.id}>
                {pet.name} {pet.species === 'CACHORRO' ? '🐕' : '🐱'}
              </option>
            ))}
          </select>
        </div>

        <div class="button-group">
          <button
            class="btn btn-lg"
            onClick={handleStartWalk}
            disabled={isRecording || !selectedPetId}
          >
            ▶ Start Recording
          </button>
          <button
            class="btn btn-lg btn-danger"
            onClick={handleStopWalk}
            disabled={!isRecording}
          >
            ⏹ Stop Recording
          </button>
          <button
            class="btn btn-lg btn-warning"
            onClick={handleForceStop}
            disabled={!isRecording}
            style="background-color: #ff9800;"
          >
            ⚠ Force Stop
          </button>
        </div>

        {isRecording && (
          <div class="recording-info">
            <div class="info-item">
              <span class="info-label">Points</span>
              <span class="info-value">{pointsCount}</span>
            </div>
            <div class="info-item">
              <span class="info-label">Queued</span>
              <span class="info-value">{queuedBatches}</span>
            </div>
            <div class="info-item">
              <span class="info-label">Time</span>
              <span class="info-value">{formatTime(recordingTime)}</span>
            </div>
          </div>
        )}
      </div>

      {/* Status indicators */}
      <div class="status-indicators">
        {isRecording && (
          <div class="status-indicator recording">
            <span class="indicator-icon">●</span>
            <span class="indicator-text">Recording</span>
          </div>
        )}
        {!isOnline && (
          <div class="status-indicator offline">
            <span class="indicator-icon">⚠</span>
            <span class="indicator-text">Offline</span>
          </div>
        )}
        {isSyncing && (
          <div class="status-indicator syncing">
            <span class="indicator-icon">↻</span>
            <span class="indicator-text">Syncing</span>
          </div>
        )}
      </div>
    </div>
  )
}
