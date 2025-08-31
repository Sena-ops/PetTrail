import { useState, useEffect } from 'preact/hooks'
import { petsApi, Pet } from '../api/pets'
import { walksApi, WalkListItem, WalksPageResponse } from '../api/walks'
import { HttpError } from '../api/http'
import { useRouter } from '../router'

export const WalksPage = () => {
  const [pets, setPets] = useState<Pet[]>([])
  const [selectedPetId, setSelectedPetId] = useState<number | null>(null)
  const [walks, setWalks] = useState<WalkListItem[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [currentPage, setCurrentPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)
  const { navigate } = useRouter()

  useEffect(() => {
    loadPets()
  }, [])

  useEffect(() => {
    if (selectedPetId) {
      loadWalks(selectedPetId, currentPage)
    }
  }, [selectedPetId, currentPage])

  const loadPets = async () => {
    try {
      setLoading(true)
      setError(null)
      const petsList = await petsApi.listPets()
      setPets(petsList)
      if (petsList.length > 0) {
        setSelectedPetId(petsList[0].id)
      }
    } catch (err) {
      setError(err instanceof HttpError ? err.message : 'Failed to load pets')
    } finally {
      setLoading(false)
    }
  }

  const loadWalks = async (petId: number, page: number) => {
    try {
      setLoading(true)
      setError(null)
      const response: WalksPageResponse = await walksApi.listWalks(petId, page, 10)
      setWalks(response.content)
      setTotalPages(response.totalPages)
      setTotalElements(response.totalElements)
    } catch (err) {
      setError(err instanceof HttpError ? err.message : 'Failed to load walks')
    } finally {
      setLoading(false)
    }
  }

  const handlePetChange = (petId: number) => {
    setSelectedPetId(petId)
    setCurrentPage(0)
  }

  const handlePageChange = (page: number) => {
    setCurrentPage(page)
  }

  const handleViewWalkDetails = (walkId: number) => {
    navigate('walk-details', { id: walkId.toString() })
  }

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('pt-BR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    })
  }

  const formatDistance = (distance?: number) => {
    if (!distance) return 'N/A'
    return distance >= 1000 
      ? `${(distance / 1000).toFixed(1)} km`
      : `${Math.round(distance)} m`
  }

  const formatDuration = (duration?: number) => {
    if (!duration) return 'N/A'
    const hours = Math.floor(duration / 3600)
    const minutes = Math.floor((duration % 3600) / 60)
    if (hours > 0) {
      return `${hours}h ${minutes}m`
    }
    return `${minutes}m`
  }

  const formatPace = (pace?: number) => {
    if (!pace) return 'N/A'
    return `${pace.toFixed(1)} km/h`
  }

  if (loading && walks.length === 0) {
    return (
      <div class="page">
        <div class="loading">
          <div class="spinner"></div>
          Loading walks...
        </div>
      </div>
    )
  }

  return (
    <div class="page">
      <div class="page-title">Walk History</div>
      
      {error && (
        <div class="card" style="background-color: #ffebee; color: #c62828; border: 1px solid #ffcdd2;">
          <div class="card-title">Error</div>
          <div class="card-content">{error}</div>
        </div>
      )}

      {pets.length === 0 ? (
        <div class="card">
          <div class="card-content" style="text-align: center; padding: var(--spacing-xl);">
            No pets found. Add a pet first to view walk history.
          </div>
        </div>
      ) : (
        <>
          <div class="card">
            <div class="card-title">Select Pet</div>
            <div class="card-content">
              <select
                class="form-select"
                value={selectedPetId || ''}
                onChange={(e) => handlePetChange(parseInt((e.target as HTMLSelectElement).value))}
              >
                {pets.map(pet => (
                  <option key={pet.id} value={pet.id}>
                    {pet.name} {pet.species === 'CACHORRO' ? '🐕' : '🐱'}
                  </option>
                ))}
              </select>
            </div>
          </div>

          {walks.length === 0 ? (
            <div class="card">
              <div class="card-content" style="text-align: center; padding: var(--spacing-xl);">
                No walks found for this pet yet.
              </div>
            </div>
          ) : (
            <>
              <div class="walks-list">
                {walks.map(walk => (
                  <div key={walk.id} class="walk-item">
                    <div class="walk-info">
                      <div class="walk-date">
                        {formatDate(walk.startedAt)}
                      </div>
                      <div class="walk-metrics">
                        <span class="metric">
                          <span class="metric-label">Distance:</span>
                          <span class="metric-value">{formatDistance(walk.distanciaM)}</span>
                        </span>
                        <span class="metric">
                          <span class="metric-label">Duration:</span>
                          <span class="metric-value">{formatDuration(walk.duracaoS)}</span>
                        </span>
                        <span class="metric">
                          <span class="metric-label">Pace:</span>
                          <span class="metric-value">{formatPace(walk.velMediaKmh)}</span>
                        </span>
                      </div>
                    </div>
                    <div class="walk-actions">
                      <button 
                        class="btn btn-sm btn-secondary"
                        onClick={() => handleViewWalkDetails(walk.id)}
                      >
                        View Route
                      </button>
                    </div>
                  </div>
                ))}
              </div>

              {totalPages > 1 && (
                <div class="pagination">
                  <button 
                    class="btn btn-sm btn-secondary"
                    disabled={currentPage === 0}
                    onClick={() => handlePageChange(currentPage - 1)}
                  >
                    Previous
                  </button>
                  <span class="pagination-info">
                    Page {currentPage + 1} of {totalPages} ({totalElements} total)
                  </span>
                  <button 
                    class="btn btn-sm btn-secondary"
                    disabled={currentPage === totalPages - 1}
                    onClick={() => handlePageChange(currentPage + 1)}
                  >
                    Next
                  </button>
                </div>
              )}
            </>
          )}
        </>
      )}
    </div>
  )
}
