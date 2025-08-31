import { useRouter } from '../router'

export const NavBar = () => {
  const { currentRoute, navigate } = useRouter()

  return (
    <nav class="nav-bar">
      <div class="nav-content">
        <div class="nav-title">🐾 PatTrail</div>
        <div class="nav-links">
          <a 
            class={`nav-link ${currentRoute === 'pets' ? 'active' : ''}`}
            onClick={() => navigate('pets')}
            href="#pets"
          >
            Pets
          </a>
          <a 
            class={`nav-link ${currentRoute === 'walks' ? 'active' : ''}`}
            onClick={() => navigate('walks')}
            href="#walks"
          >
            Walks
          </a>
          <a 
            class={`nav-link ${currentRoute === 'achievements' ? 'active' : ''}`}
            onClick={() => navigate('achievements')}
            href="#achievements"
          >
            Achievements
          </a>
          <a 
            class={`nav-link ${currentRoute === 'map' ? 'active' : ''}`}
            onClick={() => navigate('map')}
            href="#map"
          >
            Map & Walk
          </a>
        </div>
      </div>
    </nav>
  )
}
