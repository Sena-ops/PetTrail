const CACHE_NAME = 'pattrail-map-v1';
const STATIC_CACHE = 'pattrail-static-v1';

// Files to cache (app shell only)
const STATIC_FILES = [
    '/web/',
    '/web/index.html',
    '/web/styles.css',
    '/web/app.js',
    '/web/manifest.webmanifest',
    'https://unpkg.com/leaflet@1.9.4/dist/leaflet.css',
    'https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'
];

// Install event - cache app shell
self.addEventListener('install', event => {
    console.log('Service Worker installing...');
    event.waitUntil(
        caches.open(STATIC_CACHE)
            .then(cache => {
                console.log('Caching app shell files');
                return cache.addAll(STATIC_FILES);
            })
            .catch(error => {
                console.error('Cache addAll failed:', error);
            })
    );
    self.skipWaiting();
});

// Activate event - clean up old caches
self.addEventListener('activate', event => {
    console.log('Service Worker activating...');
    event.waitUntil(
        caches.keys().then(cacheNames => {
            return Promise.all(
                cacheNames.map(cacheName => {
                    if (cacheName !== STATIC_CACHE && cacheName !== CACHE_NAME) {
                        console.log('Deleting old cache:', cacheName);
                        return caches.delete(cacheName);
                    }
                })
            );
        })
    );
    self.clients.claim();
});

// Fetch event - serve from cache, fallback to network
self.addEventListener('fetch', event => {
    const url = new URL(event.request.url);
    
    // Skip non-GET requests
    if (event.request.method !== 'GET') {
        return;
    }
    
    // Skip OpenStreetMap tile requests (don't cache them)
    if (url.hostname.includes('tile.openstreetmap.org')) {
        return;
    }
    
    // Handle app shell files
    if (STATIC_FILES.some(file => event.request.url.includes(file.replace('/web/', '')))) {
        event.respondWith(
            caches.match(event.request)
                .then(response => {
                    // Return cached version or fetch from network
                    return response || fetch(event.request)
                        .then(fetchResponse => {
                            // Cache successful responses for app shell files
                            if (fetchResponse && fetchResponse.status === 200) {
                                const responseToCache = fetchResponse.clone();
                                caches.open(STATIC_CACHE)
                                    .then(cache => {
                                        cache.put(event.request, responseToCache);
                                    });
                            }
                            return fetchResponse;
                        });
                })
                .catch(() => {
                    // Return offline page or fallback for HTML requests
                    if (event.request.destination === 'document') {
                        return caches.match('/web/index.html');
                    }
                })
        );
    }
    
    // For other requests, try network first, then cache
    event.respondWith(
        fetch(event.request)
            .then(response => {
                // Only cache successful responses for same-origin requests
                if (response && response.status === 200 && url.origin === location.origin) {
                    const responseToCache = response.clone();
                    caches.open(CACHE_NAME)
                        .then(cache => {
                            cache.put(event.request, responseToCache);
                        });
                }
                return response;
            })
            .catch(() => {
                // Fallback to cache for same-origin requests
                if (url.origin === location.origin) {
                    return caches.match(event.request);
                }
            })
    );
});

// Background sync (if supported)
self.addEventListener('sync', event => {
    console.log('Background sync triggered:', event.tag);
});

// Push notifications (if needed in future)
self.addEventListener('push', event => {
    console.log('Push notification received:', event);
});
