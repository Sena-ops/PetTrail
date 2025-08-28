import html2canvas from 'html2canvas'

export interface ShareMetrics {
  distance: number
  duration: number
  averagePace: number
  elevation?: number
}

export interface ShareOptions {
  mapContainer: HTMLElement
  metrics: ShareMetrics
  badges?: string[]
  petName?: string
  walkDate?: string
}

export async function composeAndShare(options: ShareOptions): Promise<void> {
  try {
    // Create a canvas with the map and metrics overlay
    const canvas = await createShareImage(options)
    
    // Convert canvas to blob
    const blob = await new Promise<Blob>((resolve) => {
      canvas.toBlob((blob) => {
        resolve(blob!)
      }, 'image/png')
    })

    // Create file for sharing
    const file = new File([blob], 'pattrail-walk.png', { type: 'image/png' })

    // Try Web Share API first
    if (navigator.share && navigator.canShare && navigator.canShare({ files: [file] })) {
      await navigator.share({
        title: 'My PatTrail Walk',
        text: `Check out my walk with ${options.petName || 'my pet'}!`,
        files: [file]
      })
    } else {
      // Fallback to download
      downloadImage(canvas, 'pattrail-walk.png')
    }
  } catch (error) {
    console.error('Failed to share image:', error)
    throw error
  }
}

async function createShareImage(options: ShareOptions): Promise<HTMLCanvasElement> {
  const { mapContainer, metrics, badges, petName, walkDate } = options
  
  // Create a container for the share image
  const container = document.createElement('div')
  container.style.position = 'absolute'
  container.style.left = '-9999px'
  container.style.top = '0'
  container.style.width = '800px'
  container.style.height = '600px'
  container.style.backgroundColor = '#ffffff'
  container.style.padding = '20px'
  container.style.boxSizing = 'border-box'
  container.style.fontFamily = '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif'
  
  document.body.appendChild(container)

  try {
    // Clone the map container
    const mapClone = mapContainer.cloneNode(true) as HTMLElement
    mapClone.style.width = '100%'
    mapClone.style.height = '400px'
    mapClone.style.marginBottom = '20px'
    mapClone.style.borderRadius = '8px'
    mapClone.style.overflow = 'hidden'
    
    // Remove any interactive elements from the map clone
    const interactiveElements = mapClone.querySelectorAll('.leaflet-control, .leaflet-popup, .leaflet-tooltip')
    interactiveElements.forEach(el => el.remove())
    
    container.appendChild(mapClone)

    // Create metrics overlay
    const metricsDiv = document.createElement('div')
    metricsDiv.style.display = 'flex'
    metricsDiv.style.justifyContent = 'space-between'
    metricsDiv.style.alignItems = 'center'
    metricsDiv.style.padding = '20px'
    metricsDiv.style.backgroundColor = '#f8f9fa'
    metricsDiv.style.borderRadius = '8px'
    metricsDiv.style.marginBottom = '20px'

    // Header info
    const headerDiv = document.createElement('div')
    headerDiv.innerHTML = `
      <div style="font-size: 24px; font-weight: bold; color: #ff7a00; margin-bottom: 8px;">
        ${petName || 'Pet'} Walk
      </div>
      <div style="font-size: 14px; color: #666;">
        ${walkDate || new Date().toLocaleDateString()}
      </div>
    `
    metricsDiv.appendChild(headerDiv)

    // Metrics
    const metricsInfoDiv = document.createElement('div')
    metricsInfoDiv.style.display = 'flex'
    metricsInfoDiv.style.gap = '30px'
    metricsInfoDiv.style.textAlign = 'center'

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

    metricsInfoDiv.innerHTML = `
      <div>
        <div style="font-size: 20px; font-weight: bold; color: #333;">${formatDistance(metrics.distance)}</div>
        <div style="font-size: 12px; color: #666; text-transform: uppercase;">Distance</div>
      </div>
      <div>
        <div style="font-size: 20px; font-weight: bold; color: #333;">${formatDuration(metrics.duration)}</div>
        <div style="font-size: 12px; color: #666; text-transform: uppercase;">Duration</div>
      </div>
      <div>
        <div style="font-size: 20px; font-weight: bold; color: #333;">${formatPace(metrics.averagePace)}</div>
        <div style="font-size: 12px; color: #666; text-transform: uppercase;">Pace</div>
      </div>
    `
    metricsDiv.appendChild(metricsInfoDiv)

    container.appendChild(metricsDiv)

    // Badges section
    if (badges && badges.length > 0) {
      const badgesDiv = document.createElement('div')
      badgesDiv.style.marginTop = '20px'
      badgesDiv.innerHTML = `
        <div style="font-size: 16px; font-weight: bold; color: #333; margin-bottom: 10px;">
          🏆 Achievements Earned
        </div>
        <div style="display: flex; gap: 10px; flex-wrap: wrap;">
          ${badges.map(badge => `
            <div style="background: linear-gradient(135deg, #ff7a00, #ff9500); color: white; padding: 8px 12px; border-radius: 20px; font-size: 12px; font-weight: 500;">
              ${badge}
            </div>
          `).join('')}
        </div>
      `
      container.appendChild(badgesDiv)
    }

    // Footer
    const footerDiv = document.createElement('div')
    footerDiv.style.marginTop = '20px'
    footerDiv.style.textAlign = 'center'
    footerDiv.style.fontSize = '12px'
    footerDiv.style.color = '#999'
    footerDiv.innerHTML = `
      <div style="font-weight: bold; color: #ff7a00;">PatTrail</div>
      <div>Track your pet's adventures</div>
    `
    container.appendChild(footerDiv)

    // Render to canvas
    const canvas = await html2canvas(container, {
      width: 800,
      height: 600,
      backgroundColor: '#ffffff',
      scale: 2, // Higher resolution
      useCORS: true,
      allowTaint: true
    })

    return canvas
  } finally {
    // Clean up
    document.body.removeChild(container)
  }
}

function downloadImage(canvas: HTMLCanvasElement, filename: string): void {
  const link = document.createElement('a')
  link.download = filename
  link.href = canvas.toDataURL('image/png')
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}
