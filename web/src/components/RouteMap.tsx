import { useEffect, useRef, useState } from 'preact/hooks'
import * as L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import { walksApi, type GeoFeature } from '../api/walks'

type Props = { walkId: string; height?: string }

export default function RouteMap({ walkId, height = '60vh' }: Props) {
  const mapEl = useRef<HTMLDivElement>(null)
  const mapRef = useRef<L.Map | null>(null)
  const layerRef = useRef<L.GeoJSON | null>(null)
  const [msg, setMsg] = useState<string>('Carregando rota...')

  // init map once
  useEffect(() => {
    if (!mapRef.current && mapEl.current) {
      mapRef.current = L.map(mapEl.current, { zoomControl: true })
      L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '&copy; OpenStreetMap contributors',
        maxZoom: 19
      }).addTo(mapRef.current)
      // sensible initial view
      mapRef.current.setView([0, 0], 2)
    }
  }, [])

  // load feature when walkId changes
  useEffect(() => {
    let cancelled = false
    ;(async () => {
      try {
        setMsg('Carregando rota...')
        const feature: GeoFeature = await walksApi.fetchWalkGeoJSON(walkId)

        const coords = feature?.geometry?.coordinates ?? []
        if (!Array.isArray(coords) || coords.length === 0) {
          setMsg('Sem dados suficientes')
          // clear previous layer if any
          if (layerRef.current) { layerRef.current.remove(); layerRef.current = null; }
          return
        }

        const map = mapRef.current!
        if (layerRef.current) layerRef.current.remove()
        layerRef.current = L.geoJSON(feature, { style: { weight: 5 } }).addTo(map)

        const bounds = layerRef.current.getBounds()
        if (bounds.isValid()) map.fitBounds(bounds, { padding: [24, 24] })

        if (!cancelled) setMsg('')
      } catch (e: any) {
        if (!cancelled) setMsg(e?.message || 'Erro inesperado ao carregar a rota')
      }
    })()
    return () => { cancelled = true; }
  }, [walkId])

  return (
    <div>
      <div class="status-msg">{msg}</div>
      <div ref={mapEl} class="map" style={{ height }} />
    </div>
  )
}
