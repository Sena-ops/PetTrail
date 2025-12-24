import { http } from './http'

export interface StartWalkResponse {
  walkId: string
  startedAt: string
}

export interface StopWalkResponse {
  walkId: string
  stoppedAt: string
  distance: number
  duration: number
  averagePace: number
  badges?: string[]
}

export interface WalkPoint {
  latitude: number
  longitude: number
  timestamp: string
  elevation?: number
}

export interface WalkPointsBatchRequest {
  points: WalkPoint[]
}

export interface WalkPointsBatchResponse {
  received: number
  accepted: number
  discarded: number
}

export interface WalkListItem {
  id: string
  startedAt: string
  finishedAt?: string
  distanciaM?: number
  duracaoS?: number
  velMediaKmh?: number
}

export interface WalksPageResponse {
  content: WalkListItem[]
  totalElements: number
  totalPages: number
  currentPage: number
  size: number
}

export type GeoFeature = GeoJSON.Feature<GeoJSON.LineString>

export const walksApi = {
  // Start a walk for a pet
  startWalk: (petId: string): Promise<StartWalkResponse> => 
    http.post<StartWalkResponse>(`/walks/start?petId=${petId}`),
    
  // Get active walk for a pet (for recovery after page refresh)
  getActiveWalk: (petId: string): Promise<StartWalkResponse | null> => 
    http.get<StartWalkResponse>(`/walks/active?petId=${petId}`).catch(error => {
      if (error.status === 404) {
        return null; // No active walk found
      }
      throw error; // Re-throw other errors
    }),
    
  // Send GPS points for a walk
  sendPoints: (walkId: string, batch: WalkPointsBatchRequest): Promise<WalkPointsBatchResponse> => 
    http.post<WalkPointsBatchResponse>(`/walks/${walkId}/points`, batch),
    
  // Stop a walk
  stopWalk: (walkId: string): Promise<StopWalkResponse> => 
    http.post<StopWalkResponse>(`/walks/${walkId}/stop`),
    
  // List walks for a pet (paginated)
  listWalks: (petId: string, page: number = 0, size: number = 20): Promise<WalksPageResponse> => 
    http.get<WalksPageResponse>(`/walks?petId=${petId}&page=${page}&size=${size}`),
    
  // Get walk details
  getWalk: (walkId: string): Promise<WalkListItem> => 
    http.get<WalkListItem>(`/walks/${walkId}`),

  // Get walk GeoJSON
  fetchWalkGeoJSON: (id: string): Promise<GeoFeature> => 
    http.get<GeoFeature>(`/walks/${id}/geojson`)
}
