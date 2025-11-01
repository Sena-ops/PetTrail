import { useState, useEffect } from 'preact/hooks'
import { petsApi, Pet, CreatePetRequest } from '../api/pets'
import { HttpError } from '../api/http'

export const PetsPage = () => {
  const [pets, setPets] = useState<Pet[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [showCreateForm, setShowCreateForm] = useState(false)
  const [editingPet, setEditingPet] = useState<Pet | null>(null)

  // Form state
  const [formData, setFormData] = useState<CreatePetRequest>({
    name: '',
    species: 'CACHORRO',
    age: 0,
    race: '',
    pictureUrl: ''
  })

  useEffect(() => {
    loadPets()
  }, [])

  const loadPets = async () => {
    try {
      setLoading(true)
      setError(null)
      const petsList = await petsApi.listPets()
      setPets(petsList)
    } catch (err) {
      setError(err instanceof HttpError ? err.message : 'Failed to load pets')
    } finally {
      setLoading(false)
    }
  }

  const handleCreatePet = async (e: Event) => {
    e.preventDefault()
    try {
      setError(null)
      console.log('Sending pet data:', formData) // Debug log
      const newPet = await petsApi.createPet(formData)
      setPets([...pets, newPet])
      setFormData({ name: '', species: 'CACHORRO', age: 0, race: '', pictureUrl: '' })
      setShowCreateForm(false)
    } catch (err) {
      console.error('Error creating pet:', err) // Debug log
      setError(err instanceof HttpError ? err.message : 'Failed to create pet')
    }
  }

  const handleUpdatePet = async (e: Event) => {
    e.preventDefault()
    if (!editingPet) return

    try {
      setError(null)
      console.log('Updating pet data:', formData) // Debug log
      const updatedPet = await petsApi.updatePet(editingPet.id, formData)
      setPets(pets.map(pet => pet.id === editingPet.id ? updatedPet : pet))
      setEditingPet(null)
      setFormData({ name: '', species: 'CACHORRO', age: 0, race: '', pictureUrl: '' })
    } catch (err) {
      console.error('Error updating pet:', err) // Debug log
      setError(err instanceof HttpError ? err.message : 'Failed to update pet')
    }
  }

  const handleDeletePet = async (petId: string) => {
    if (!confirm('Are you sure you want to delete this pet?')) return

    try {
      setError(null)
      await petsApi.deletePet(petId)
      setPets(pets.filter(pet => pet.id !== petId))
    } catch (err) {
      setError(err instanceof HttpError ? err.message : 'Failed to delete pet')
    }
  }

  const startEdit = (pet: Pet) => {
    setEditingPet(pet)
    setFormData({
      name: pet.name,
      species: pet.species,
      age: pet.age,
      race: pet.race,
      pictureUrl: pet.pictureUrl || ''
    })
  }

  const cancelEdit = () => {
    setEditingPet(null)
    setFormData({ name: '', species: 'CACHORRO', age: 0, race: '', pictureUrl: '' })
  }

  if (loading) {
    return (
      <div class="page">
        <div class="loading">
          <div class="spinner"></div>
          Loading pets...
        </div>
      </div>
    )
  }

  return (
    <div class="page">
      <div class="page-title">My Pets</div>
      
      {error && (
        <div class="card" style="background-color: #ffebee; color: #c62828; border: 1px solid #ffcdd2;">
          <div class="card-title">Error</div>
          <div class="card-content">{error}</div>
        </div>
      )}

      {!showCreateForm && !editingPet && (
        <button 
          class="btn btn-secondary" 
          onClick={() => setShowCreateForm(true)}
        >
          ➕ Add New Pet
        </button>
      )}

      {(showCreateForm || editingPet) && (
        <div class="card">
          <div class="card-title">
            {editingPet ? 'Edit Pet' : 'Add New Pet'}
          </div>
          <form onSubmit={editingPet ? handleUpdatePet : handleCreatePet}>
            <div class="form-group">
              <label class="form-label">Name</label>
              <input
                type="text"
                class="form-input"
                value={formData.name}
                onChange={(e) => setFormData({ ...formData, name: (e.target as HTMLInputElement).value })}
                required
                maxLength={60}
              />
            </div>

            <div class="form-group">
              <label class="form-label">Species</label>
              <select
                class="form-select"
                value={formData.species}
                onChange={(e) => setFormData({ ...formData, species: (e.target as HTMLSelectElement).value as 'CACHORRO' | 'GATO' })}
                required
              >
                <option value="CACHORRO">Dog</option>
                <option value="GATO">Cat</option>
              </select>
            </div>

            <div class="form-group">
              <label class="form-label">Age (years)</label>
              <input
                type="number"
                class="form-input"
                value={formData.age}
                onChange={(e) => setFormData({ ...formData, age: parseInt((e.target as HTMLInputElement).value) || 0 })}
                min="0"
                max="30"
                required
              />
            </div>

            <div class="form-group">
              <label class="form-label">Breed</label>
              <input
                type="text"
                class="form-input"
                value={formData.race}
                onChange={(e) => setFormData({ ...formData, race: (e.target as HTMLInputElement).value })}
                required
                maxLength={50}
              />
            </div>

            <div class="form-group">
              <label class="form-label">Pet Picture</label>
              <input
                type="file"
                class="form-input"
                accept="image/*"
                onChange={(e) => {
                  const file = (e.target as HTMLInputElement).files?.[0]
                  if (file) {
                    // Check file size (limit to 1MB)
                    if (file.size > 1024 * 1024) {
                      setError('Image size must be less than 1MB')
                      return
                    }
                    
                    const reader = new FileReader()
                    reader.onload = (event) => {
                      const result = event.target?.result as string
                      setFormData({ ...formData, pictureUrl: result })
                    }
                    reader.readAsDataURL(file)
                  }
                }}
              />
              <div style="font-size: 0.8em; color: #666; margin-top: var(--spacing-xs);">
                Maximum file size: 1MB. Supported formats: JPG, PNG, GIF
              </div>
              {formData.pictureUrl && (
                <div style="margin-top: var(--spacing-sm);">
                  <img 
                    src={formData.pictureUrl} 
                    alt="Pet preview" 
                    style="max-width: 150px; max-height: 150px; border-radius: var(--border-radius); object-fit: cover;"
                  />
                </div>
              )}
            </div>

            <div style="display: flex; gap: var(--spacing-md);">
              <button type="submit" class="btn">
                {editingPet ? 'Update Pet' : 'Create Pet'}
              </button>
              <button 
                type="button" 
                class="btn btn-secondary"
                onClick={editingPet ? cancelEdit : () => setShowCreateForm(false)}
              >
                Cancel
              </button>
            </div>
          </form>
        </div>
      )}

      <div class="pet-list">
        {pets.length === 0 ? (
          <div class="card">
            <div class="card-content" style="text-align: center; padding: var(--spacing-xl);">
              No pets yet. Add your first pet to get started!
            </div>
          </div>
        ) : (
          pets.map(pet => (
            <div key={pet.id} class="pet-item">
              <div class="pet-info">
                <div class="pet-name">
                  {pet.pictureUrl ? (
                    <img 
                      src={pet.pictureUrl} 
                      alt={`${pet.name} picture`}
                      style="width: 40px; height: 40px; border-radius: 50%; object-fit: cover; margin-right: var(--spacing-sm); vertical-align: middle;"
                    />
                  ) : (
                    <span style="margin-right: var(--spacing-sm); font-size: 24px;">
                      {pet.species === 'CACHORRO' ? '🐕' : '🐱'}
                    </span>
                  )}
                  {pet.name}
                </div>
                <div class="pet-details">
                  {pet.race} • {pet.age} year{pet.age !== 1 ? 's' : ''} old
                </div>
              </div>
              <div class="pet-actions">
                <button 
                  class="btn btn-sm btn-secondary"
                  onClick={() => startEdit(pet)}
                >
                  Edit
                </button>
                <button 
                  class="btn btn-sm btn-danger"
                  onClick={() => handleDeletePet(pet.id)}
                >
                  Delete
                </button>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  )
}
