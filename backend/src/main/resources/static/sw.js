const CACHE_NAME = 'pattrail-map-v2';
const STATIC_CACHE = 'pattrail-static-v2';

// Files to cache (app shell only)
const STATIC_FILES = [
    '/',
    '/index.html',
    '/styles.css',
    '/app.js',
    '/db.js',
    '/net.js',
    '/manifest.webmanifest',
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
                        return caches.match('/index.html');
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

// Background sync for queue draining
self.addEventListener('sync', event => {
    console.log('Background sync triggered:', event.tag);
    
    if (event.tag === 'upload-points') {
        event.waitUntil(
            // Try to drain the queue when we come back online
            self.clients.matchAll().then(clients => {
                clients.forEach(client => {
                    client.postMessage({
                        type: 'DRAIN_QUEUE',
                        timestamp: Date.now()
                    });
                });
            })
        );
    }
});

// Handle messages from the main app
self.addEventListener('message', event => {
    console.log('Service Worker received message:', event.data);
    
    if (event.data && event.data.type === 'REGISTER_BACKGROUND_SYNC') {
        // Register background sync for queue draining
        if ('serviceWorker' in navigator && 'sync' in window.ServiceWorkerRegistration.prototype) {
            event.waitUntil(
                self.registration.sync.register('upload-points')
                    .then(() => {
                        console.log('Background sync registered for upload-points');
                    })
                    .catch(error => {
                        console.error('Background sync registration failed:', error);
                    })
            );
        }
    }
});

// Push notifications (if needed in future)
self.addEventListener('push', event => {
    console.log('Push notification received:', event);
    
    if (event.data) {
        const data = event.data.json();
        const options = {
            body: data.body || 'New notification from PatTrail',
            icon: '/icons/icon-192x192.png',
            badge: '/icons/icon-72x72.png',
            tag: 'pattrail-notification',
            data: data
        };
        
        event.waitUntil(
            self.registration.showNotification('PatTrail', options)
        );
    }
});

// Handle notification clicks
self.addEventListener('notificationclick', event => {
    console.log('Notification clicked:', event);
    
    event.notification.close();
    
    event.waitUntil(
        self.clients.matchAll({ type: 'window' }).then(clients => {
            // Focus existing window or open new one
            for (let client of clients) {
                if (client.url.includes('/') && 'focus' in client) {
                    return client.focus();
                }
            }
            if (self.clients.openWindow) {
                return self.clients.openWindow('/');
            }
        })
    );
});
