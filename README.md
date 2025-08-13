# PatTrail
Rastreie os passeios do seu pet e conquiste badges

## PWA Frontend

This app uses the OpenStreetMap tile API via Leaflet.

### Features

- **Interactive Map**: Full-screen Leaflet map using OpenStreetMap tiles
- **PWA Support**: Installable as a Progressive Web App
- **Offline Capability**: App shell cached for offline use
- **Responsive Design**: Works on desktop and mobile devices

### OpenStreetMap Usage

This application uses OpenStreetMap tiles via the official tile server at `tile.openstreetmap.org`. 

#### Tile Usage Policy

- **Be Considerate**: OpenStreetMap tile servers are community-funded and have usage limits
- **Production Usage**: For heavy or production use, consider:
  - Setting up your own tile server
  - Using a commercial tile provider (Mapbox, CartoDB, etc.)
  - Implementing tile caching strategies

#### Tile URL Template

```
https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png
```

#### Attribution

Proper OpenStreetMap attribution is included in the map:
```html
&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors
```

### Switching Tile Providers

To use a different tile provider:

1. **Edit the tile URL** in `web/app.js`:
   ```javascript
   const osmTiles = L.tileLayer('YOUR_TILE_URL_HERE', {
       attribution: 'YOUR_ATTRIBUTION_HERE',
       maxZoom: 19
   });
   ```

2. **Update attribution** to match your provider's requirements

3. **Popular alternatives**:
   - **CartoDB**: `https://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}{r}.png`
   - **Mapbox**: `https://api.mapbox.com/styles/v1/mapbox/streets-v11/tiles/{z}/{x}/{y}?access_token=YOUR_TOKEN`
   - **Thunderforest**: `https://{s}.tile.thunderforest.com/transport/{z}/{x}/{y}.png`

### Local Development

#### Quick Start

1. **Navigate to the web directory**:
   ```bash
   cd web
   ```

2. **Start a local server** (choose one):
   ```bash
   # Using Python 3
   python -m http.server 8000
   
   # Using Python 2
   python -m SimpleHTTPServer 8000
   
   # Using Node.js (http-server)
   npx http-server -p 8000
   
   # Using PHP
   php -S localhost:8000
   ```

3. **Open your browser** and navigate to:
   ```
   http://localhost:8000
   ```

#### HTTPS for PWA Testing

For full PWA functionality (installation, service worker), use HTTPS:

```bash
# Using http-server with HTTPS
npx http-server -p 8000 --ssl --cert cert.pem --key key.pem

# Or use a service like ngrok
npx ngrok http 8000
```

### File Structure

```
web/
├── index.html              # Main HTML file
├── styles.css              # CSS styles
├── app.js                  # Leaflet map initialization
├── manifest.webmanifest    # PWA manifest
├── sw.js                   # Service worker
└── icons/                  # PWA icons (see icons/README.md)
    └── README.md           # Icon requirements
```

### PWA Features

- **Installable**: Can be installed on desktop and mobile devices
- **Offline Support**: App shell cached for offline use
- **Service Worker**: Handles caching and offline functionality
- **Manifest**: Defines app appearance and behavior

### Browser Support

- Chrome/Edge: Full PWA support
- Firefox: Basic PWA support
- Safari: Limited PWA support
- Mobile browsers: Varies by platform

### Troubleshooting

1. **Map not loading**: Check browser console for CORS errors
2. **PWA not installable**: Ensure HTTPS is used and manifest is valid
3. **Service worker issues**: Clear browser cache and reload
4. **Tile loading errors**: Check network connectivity and OpenStreetMap server status

### Dependencies

- **Leaflet 1.9.4**: Loaded from CDN (unpkg.com)
- **No paid dependencies**: Uses only free, open-source libraries
- **No Google Maps**: Exclusively uses OpenStreetMap tiles
