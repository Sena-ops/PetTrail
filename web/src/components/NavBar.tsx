import { useRouter } from '../router'
import { useAuth } from '../contexts/AuthContext'

export const NavBar = () => {
  const { currentRoute, navigate } = useRouter()
  const { user, logout } = useAuth()

  const handleLogout = () => {
    logout()
    window.location.reload()
  }

  return (
    <nav class="nav-bar">
      <div class="nav-content">
        <div class="nav-title">🐾 PetTrail</div>
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
        <div class="nav-user">
          <span class="nav-user-name">{user?.fullName}</span>
          <button 
            onClick={handleLogout}
            class="nav-logout"
          >
            Logout
          </button>
        </div>
      </div>
    </nav>
  )
}
