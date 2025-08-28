# PatTrail Frontend

A modern Progressive Web App (PWA) for tracking pet walks with gamification features, built with Vite, Preact, and TypeScript.

## Features

- 🐾 **Pet Management**: Create, edit, and manage your pets
- 🗺️ **Live GPS Tracking**: Real-time location tracking with Leaflet maps
- 📱 **PWA Support**: Installable app with offline capabilities
- 🔄 **Offline Queue**: GPS points are queued when offline and synced when online
- 🏆 **Achievements**: Track and display pet achievements
- 📤 **Social Sharing**: Share walk results as PNG images
- 🎨 **Modern UI**: Responsive design with beautiful animations

## Tech Stack

- **Build Tool**: Vite
- **Framework**: Preact (React-like with smaller bundle size)
- **Language**: TypeScript
- **Maps**: Leaflet with OpenStreetMap tiles
- **PWA**: vite-plugin-pwa with Workbox
- **Storage**: IndexedDB for offline queue
- **Styling**: CSS with CSS Variables

## Prerequisites

- Node.js 18+ 
- npm or yarn
- Backend server running on http://localhost:8080

## Installation

1. Install dependencies:
```bash
npm install
```

2. Copy environment file:
```bash
cp env.example .env
```

3. Start development server:
```bash
npm run dev
```

The app will be available at http://localhost:5173

## Build for Production

```bash
npm run build
```

This creates static assets in the `dist/` directory ready for deployment to GitHub Pages, Netlify, or any static hosting service.

## Environment Variables

- `VITE_API_BASE`: API base URL (default: `/api`)
  - Development: proxied to http://localhost:8080
  - Production: relative `/api`

## Project Structure

```
web/
├── public/                 # Static assets
│   ├── favicon.svg        # App icon
│   ├── manifest.webmanifest # PWA manifest
│   └── robots.txt         # SEO
├── src/
│   ├── api/               # API clients
│   │   ├── http.ts        # Base HTTP client
│   │   ├── pets.ts        # Pet API
│   │   ├── walks.ts       # Walk API
│   │   └── achievements.ts # Achievements API
│   ├── components/        # React components
│   │   ├── NavBar.tsx     # Navigation
│   │   ├── PetsPage.tsx   # Pet management
│   │   ├── AchievementsPage.tsx # Achievements
│   │   ├── MapWalkPage.tsx # Main walk page
│   │   ├── MapView.tsx    # Leaflet map
│   │   └── WalkControls.tsx # Walk controls
│   ├── lib/               # Utilities
│   │   ├── geo.ts         # Geolocation service
│   │   ├── idbQueue.ts    # IndexedDB queue
│   │   └── sharePng.ts    # PNG sharing
│   ├── App.tsx            # Main app component
│   ├── main.tsx           # Entry point
│   ├── router.ts          # Hash router
│   └── styles.css         # Global styles
├── index.html             # HTML template
├── vite.config.ts         # Vite configuration
├── package.json           # Dependencies
└── tsconfig.json          # TypeScript config
```

## PWA Features

### Service Worker
- Auto-updates with `registerType: 'autoUpdate'`
- Runtime caching for API calls and OSM tiles
- Network-first strategy for API with 3s timeout
- Stale-while-revalidate for map tiles

### Offline Support
- GPS points are queued in IndexedDB when offline
- Automatic sync when connection is restored
- FIFO queue with retry logic
- Visual indicators for offline/syncing status

### Installable
- Web App Manifest with proper icons
- Standalone display mode
- Theme color: #ff7a00

## API Integration

The frontend integrates with the Spring Boot backend:

- **Pets**: CRUD operations for pet management
- **Walks**: Start/stop walks, send GPS points in batches
- **Achievements**: Display earned achievements per pet

### GPS Point Batching
- Points are batched every 10 points or 5 seconds
- Outlier filtering (>50 m/s speed)
- Automatic retry on network failures

## Deployment

### GitHub Pages
1. Build the project: `npm run build`
2. Push `dist/` contents to `gh-pages` branch
3. Enable GitHub Pages in repository settings

### Netlify
1. Connect repository to Netlify
2. Set build command: `npm run build`
3. Set publish directory: `dist`

### Environment Setup
For production, ensure your backend is accessible at the correct URL and update `VITE_API_BASE` if needed.

## Development

### Available Scripts
- `npm run dev` - Start development server
- `npm run build` - Build for production
- `npm run preview` - Preview production build
- `npm run lint` - Run ESLint

### Code Style
- TypeScript strict mode enabled
- ESLint with Preact rules
- Prettier formatting (recommended)

## Browser Support

- Modern browsers with ES2020 support
- Geolocation API required for GPS tracking
- Service Worker support for PWA features
- IndexedDB for offline storage

## Troubleshooting

### GPS Issues
- Ensure location permissions are granted
- Check browser console for geolocation errors
- Verify HTTPS in production (required for geolocation)

### Offline Queue Issues
- Check IndexedDB support in browser
- Verify service worker registration
- Monitor network status indicators

### Build Issues
- Clear node_modules and reinstall
- Check TypeScript compilation errors
- Verify all dependencies are installed

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## License

This project is licensed under the same license as the main PatTrail project.
