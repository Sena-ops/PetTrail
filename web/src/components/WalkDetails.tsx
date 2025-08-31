import { useRouter } from '../router'
import RouteMap from './RouteMap'

export default function WalkDetails() {
  const { getParams } = useRouter()
  const params = getParams()
  const id = params.get('id') || ''
  return (
    <main class="p-4">
      <h2 class="text-xl font-semibold mb-2">Detalhe da caminhada</h2>
      {id ? <RouteMap walkId={id} /> : <p>ID da caminhada não informado.</p>}
    </main>
  )
}
