import { http } from './http'

export interface Achievement {
  id: number
  name: string
  description: string
  icon?: string
  earnedAt?: string
  progress?: number
  target?: number
}

export const achievementsApi = {
  // List achievements for a pet
  listAchievements: (petId: string): Promise<Achievement[]> => 
    http.get<Achievement[]>(`/achievements?petId=${petId}`)
}
