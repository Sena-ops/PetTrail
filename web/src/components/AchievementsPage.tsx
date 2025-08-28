import { useState, useEffect } from 'preact/hooks'
import { achievementsApi, Achievement } from '../api/achievements'
import { petsApi, Pet } from '../api/pets'
import { HttpError } from '../api/http'

export const AchievementsPage = () => {
  const [pets, setPets] = useState<Pet[]>([])
  const [selectedPetId, setSelectedPetId] = useState<number | null>(null)
  const [achievements, setAchievements] = useState<Achievement[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    loadPets()
  }, [])

  useEffect(() => {
    if (selectedPetId) {
      loadAchievements(selectedPetId)
    } else {
      setAchievements([])
    }
  }, [selectedPetId])

  const loadPets = async () => {
    try {
      setLoading(true)
      setError(null)
      const petsList = await petsApi.listPets()
      setPets(petsList)
      if (petsList.length > 0 && !selectedPetId) {
        setSelectedPetId(petsList[0].id)
      }
    } catch (err) {
      setError(err instanceof HttpError ? err.message : 'Failed to load pets')
    } finally {
      setLoading(false)
    }
  }

  const loadAchievements = async (petId: number) => {
    try {
      setError(null)
      const achievementsList = await achievementsApi.listAchievements(petId)
      setAchievements(achievementsList)
    } catch (err) {
      setError(err instanceof HttpError ? err.message : 'Failed to load achievements')
    }
  }

  const selectedPet = pets.find(pet => pet.id === selectedPetId)

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
      <div class="page-title">Achievements</div>
      
      {error && (
        <div class="card" style="background-color: #ffebee; color: #c62828; border: 1px solid #ffcdd2;">
          <div class="card-title">Error</div>
          <div class="card-content">{error}</div>
        </div>
      )}

      {pets.length === 0 ? (
        <div class="card">
          <div class="card-content" style="text-align: center; padding: var(--spacing-xl);">
            No pets found. Add a pet first to see their achievements!
          </div>
        </div>
      ) : (
        <>
          <div class="card">
            <div class="form-group">
              <label class="form-label">Select Pet</label>
              <select
                class="form-select"
                value={selectedPetId || ''}
                onChange={(e) => setSelectedPetId(parseInt((e.target as HTMLSelectElement).value) || null)}
              >
                {pets.map(pet => (
                  <option key={pet.id} value={pet.id}>
                    {pet.name} {pet.species === 'CACHORRO' ? '🐕' : '🐱'}
                  </option>
                ))}
              </select>
            </div>
          </div>

          {selectedPet && (
            <div class="card">
              <div class="card-title">
                {selectedPet.name}'s Achievements
              </div>
              <div class="card-content">
                {achievements.length === 0 ? (
                  <div style="text-align: center; padding: var(--spacing-xl); color: var(--text-light);">
                    No achievements earned yet. Go for a walk to start earning achievements!
                  </div>
                ) : (
                  <div style="display: grid; gap: var(--spacing-md);">
                    {achievements.map(achievement => (
                      <div 
                        key={achievement.id} 
                        class="card"
                        style={achievement.earnedAt ? 
                          'background: linear-gradient(135deg, #fff3e0, #ffe0b2); border: 2px solid #ff9800;' : 
                          'background-color: #f5f5f5; opacity: 0.7;'
                        }
                      >
                        <div style="display: flex; align-items: center; gap: var(--spacing-md);">
                          <div style="font-size: 2rem;">
                            {achievement.earnedAt ? '🏆' : '🔒'}
                          </div>
                          <div style="flex: 1;">
                            <div style="font-weight: 600; margin-bottom: var(--spacing-xs);">
                              {achievement.name}
                            </div>
                            <div style="color: var(--text-light); font-size: 0.875rem;">
                              {achievement.description}
                            </div>
                            {achievement.earnedAt && (
                              <div style="color: #ff9800; font-size: 0.75rem; margin-top: var(--spacing-xs);">
                                Earned on {new Date(achievement.earnedAt).toLocaleDateString()}
                              </div>
                            )}
                            {achievement.progress !== undefined && achievement.target && (
                              <div style="margin-top: var(--spacing-sm);">
                                <div style="display: flex; justify-content: space-between; font-size: 0.75rem; margin-bottom: var(--spacing-xs);">
                                  <span>Progress</span>
                                  <span>{achievement.progress} / {achievement.target}</span>
                                </div>
                                <div style="width: 100%; height: 4px; background-color: #e0e0e0; border-radius: 2px; overflow: hidden;">
                                  <div 
                                    style={{
                                      width: `${Math.min((achievement.progress / achievement.target) * 100, 100)}%`,
                                      height: '100%',
                                      backgroundColor: achievement.earnedAt ? '#ff9800' : '#4CAF50',
                                      transition: 'width 0.3s ease'
                                    }}
                                  ></div>
                                </div>
                              </div>
                            )}
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </div>
          )}
        </>
      )}
    </div>
  )
}
