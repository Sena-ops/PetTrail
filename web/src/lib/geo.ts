export interface GeoPoint {
  latitude: number
  longitude: number
  timestamp: number
  elevation?: number
  accuracy?: number
}

export interface GeolocationOptions {
  enableHighAccuracy?: boolean
  maximumAge?: number
  timeout?: number
}

export interface WatchOptions extends GeolocationOptions {
  onPoint: (point: GeoPoint) => void
  onError: (error: GeolocationPositionError) => void
  outlierFilter?: boolean
  maxSpeed?: number // m/s
}

class GeolocationService {
  private watchId: number | null = null
  private lastPoint: GeoPoint | null = null
  private batch: GeoPoint[] = []
  private batchTimeout: number | null = null
  private readonly BATCH_SIZE = 10
  private readonly BATCH_TIMEOUT = 5000 // 5 seconds
  private readonly DEFAULT_MAX_SPEED = 50 // m/s

  private calculateDistance(point1: GeoPoint, point2: GeoPoint): number {
    const R = 6371e3 // Earth's radius in meters
    const φ1 = (point1.latitude * Math.PI) / 180
    const φ2 = (point2.latitude * Math.PI) / 180
    const Δφ = ((point2.latitude - point1.latitude) * Math.PI) / 180
    const Δλ = ((point2.longitude - point1.longitude) * Math.PI) / 180

    const a = Math.sin(Δφ / 2) * Math.sin(Δφ / 2) +
              Math.cos(φ1) * Math.cos(φ2) *
              Math.sin(Δλ / 2) * Math.sin(Δλ / 2)
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

    return R * c
  }

  private calculateSpeed(point1: GeoPoint, point2: GeoPoint): number {
    const distance = this.calculateDistance(point1, point2)
    const timeDiff = (point2.timestamp - point1.timestamp) / 1000 // seconds
    return distance / timeDiff // m/s
  }

  private isOutlier(newPoint: GeoPoint, maxSpeed: number): boolean {
    if (!this.lastPoint) return false

    const speed = this.calculateSpeed(this.lastPoint, newPoint)
    return speed > maxSpeed
  }

  private addToBatch(point: GeoPoint, onPoint: (point: GeoPoint) => void): void {
    this.batch.push(point)
    
    // Clear existing timeout
    if (this.batchTimeout) {
      clearTimeout(this.batchTimeout)
    }

    // Send batch if size reached
    if (this.batch.length >= this.BATCH_SIZE) {
      this.sendBatch(onPoint)
    } else {
      // Set timeout for batch sending
      this.batchTimeout = window.setTimeout(() => {
        this.sendBatch(onPoint)
      }, this.BATCH_TIMEOUT)
    }
  }

  private sendBatch(onPoint: (point: GeoPoint) => void): void {
    if (this.batch.length === 0) return

    // Send each point individually
    for (const point of this.batch) {
      onPoint(point)
    }

    this.batch = []
    this.batchTimeout = null
  }

  async getCurrentPosition(options: GeolocationOptions = {}): Promise<GeoPoint> {
    const defaultOptions: PositionOptions = {
      enableHighAccuracy: true,
      maximumAge: 2000,
      timeout: 10000,
      ...options
    }

    return new Promise((resolve, reject) => {
      if (!navigator.geolocation) {
        reject(new Error('Geolocation not supported'))
        return
      }

      navigator.geolocation.getCurrentPosition(
        (position) => {
          const point: GeoPoint = {
            latitude: position.coords.latitude,
            longitude: position.coords.longitude,
            timestamp: position.timestamp,
            elevation: position.coords.altitude || undefined,
            accuracy: position.coords.accuracy || undefined
          }
          resolve(point)
        },
        (error) => {
          reject(error)
        },
        defaultOptions
      )
    })
  }

  startWatch(options: WatchOptions): void {
    if (this.watchId) {
      this.stopWatch()
    }

    const defaultOptions: PositionOptions = {
      enableHighAccuracy: true,
      maximumAge: 2000,
      timeout: 10000,
      ...options
    }

    const maxSpeed = options.maxSpeed || this.DEFAULT_MAX_SPEED

    if (!navigator.geolocation) {
      options.onError({
        code: 2,
        message: 'Geolocation not supported',
        PERMISSION_DENIED: 1,
        POSITION_UNAVAILABLE: 2,
        TIMEOUT: 3
      } as GeolocationPositionError)
      return
    }

    this.watchId = navigator.geolocation.watchPosition(
      (position) => {
        const point: GeoPoint = {
          latitude: position.coords.latitude,
          longitude: position.coords.longitude,
          timestamp: position.timestamp,
          elevation: position.coords.altitude || undefined,
          accuracy: position.coords.accuracy || undefined
        }

        // Apply outlier filter if enabled
        if (options.outlierFilter && this.isOutlier(point, maxSpeed)) {
          console.warn('Outlier point filtered out:', point)
          return
        }

        this.lastPoint = point
        this.addToBatch(point, options.onPoint)
      },
      (error) => {
        options.onError(error)
      },
      defaultOptions
    )
  }

  stopWatch(): void {
    if (this.watchId) {
      navigator.geolocation.clearWatch(this.watchId)
      this.watchId = null
    }

    // Send any remaining points in batch
    if (this.batch.length > 0) {
      this.sendBatch(() => {}) // Empty callback since we're stopping
    }

    if (this.batchTimeout) {
      clearTimeout(this.batchTimeout)
      this.batchTimeout = null
    }

    this.lastPoint = null
    this.batch = []
  }

  isWatching(): boolean {
    return this.watchId !== null
  }

  getBatchSize(): number {
    return this.batch.length
  }
}

export const geoService = new GeolocationService()
