import { http } from './http'

export interface Pet {
  id: string
  name: string
  species: 'CACHORRO' | 'GATO'
  age: number
  race: string
}

export interface CreatePetRequest {
  name: string
  species: 'CACHORRO' | 'GATO'
  age: number
  race: string
}

export interface UpdatePetRequest {
  name?: string
  species?: 'CACHORRO' | 'GATO'
  age?: number
  race?: string
}

export const petsApi = {
  // List all pets
  listPets: (): Promise<Pet[]> => 
    http.get<Pet[]>('/pets'),
    
  // Get pet by ID
  getPet: (id: string): Promise<Pet> => 
    http.get<Pet>(`/pets/${id}`),
    
  // Create new pet
  createPet: (data: CreatePetRequest): Promise<Pet> => 
    http.post<Pet>('/pets', data),
    
  // Update pet
  updatePet: (id: string, data: UpdatePetRequest): Promise<Pet> => 
    http.put<Pet>(`/pets/${id}`, data),
    
  // Delete pet
  deletePet: (id: string): Promise<void> => 
    http.delete<void>(`/pets/${id}`)
}
