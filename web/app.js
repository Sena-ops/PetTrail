// Initialize the map when the DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    // Create map instance centered on default coordinates [0, 0] with zoom level 2
    const map = L.map('map').setView([0, 0], 2);
    
    // Add OpenStreetMap tile layer with proper attribution
    const osmTiles = L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
        maxZoom: 19,
        subdomains: 'abc'
    });
    
    // Add the tile layer to the map
    osmTiles.addTo(map);
    
    // Add a marker at the center point for reference
    const centerMarker = L.marker([0, 0]).addTo(map);
    centerMarker.bindPopup('<b>Center Point</b><br>Latitude: 0, Longitude: 0').openPopup();
    
    // Add scale control
    L.control.scale().addTo(map);
    
    // Add fullscreen control (if supported)
    if (map.toggleFullscreen) {
        L.control.fullscreen().addTo(map);
    }
    
    // Log map initialization
    console.log('Leaflet map initialized with OpenStreetMap tiles');
    console.log('Map center:', map.getCenter());
    console.log('Map zoom:', map.getZoom());
    
    // Handle map events for debugging
    map.on('load', function() {
        console.log('Map tiles loaded successfully');
    });
    
    map.on('tileloadstart', function(e) {
        console.log('Loading tile:', e.url);
    });
    
    map.on('tileerror', function(e) {
        console.error('Tile loading error:', e);
    });
});

// PWA installation handling
let deferredPrompt;

window.addEventListener('beforeinstallprompt', (e) => {
    // Prevent Chrome 67 and earlier from automatically showing the prompt
    e.preventDefault();
    // Stash the event so it can be triggered later
    deferredPrompt = e;
    console.log('PWA install prompt ready');
});

// Handle successful installation
window.addEventListener('appinstalled', (evt) => {
    console.log('PWA installed successfully');
    deferredPrompt = null;
});
