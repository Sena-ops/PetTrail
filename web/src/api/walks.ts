import { http } from './http'

export interface StartWalkResponse {
  walkId: number
  startedAt: string
}

export interface StopWalkResponse {
  walkId: number
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
  id: number
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
  startWalk: (petId: number): Promise<StartWalkResponse> => 
    http.post<StartWalkResponse>(`/walks/start?petId=${petId}`),
    
  // Send GPS points for a walk
  sendPoints: (walkId: number, batch: WalkPointsBatchRequest): Promise<WalkPointsBatchResponse> => 
    http.post<WalkPointsBatchResponse>(`/walks/${walkId}/points`, batch),
    
  // Stop a walk
  stopWalk: (walkId: number): Promise<StopWalkResponse> => 
    http.post<StopWalkResponse>(`/walks/${walkId}/stop`),
    
  // List walks for a pet (paginated)
  listWalks: (petId: number, page: number = 0, size: number = 20): Promise<WalksPageResponse> => 
    http.get<WalksPageResponse>(`/walks?petId=${petId}&page=${page}&size=${size}`),
    
  // Get walk details
  getWalk: (walkId: number): Promise<WalkListItem> => 
    http.get<WalkListItem>(`/walks/${walkId}`),

  // Get walk GeoJSON
  fetchWalkGeoJSON: async (id: string): Promise<GeoFeature> => {
    const res = await fetch(`/api/walks/${id}/geojson`, { headers: { Accept: 'application/json' } })
    if (!res.ok) {
      let msg = `Erro ao carregar rota (HTTP ${res.status})`
      try { const err = await res.json(); msg = err?.message || msg; } catch {}
      throw new Error(msg)
    }
    return res.json()
  }
}
