/**
 * PatTrail PWA - GPS Recording Application
 * Implements GPS recording with batching and resilient delivery to backend
 */

class PatTrailApp {
    constructor() {
        this.map = null;
        this.currentWalkId = null;
        this.isRecording = false;
        this.geolocationId = null;
        this.buffer = [];
        this.flushTimer = null;
        this.recordingStartTime = null;
        this.recordingTimer = null;
        this.lastSentTimestamps = new Set();
        
        // Configuration
        this.bufferSize = 10;
        this.flushInterval = 5000; // 5 seconds
        this.geolocationOptions = {
            enableHighAccuracy: true,
            maximumAge: 2000,
            timeout: 10000
        };
        
        // Initialize components
        this.walkQueue = new WalkQueueDB();
        this.networking = new WalkNetworking();
        
        // Make available globally for debugging
        window.walkQueue = this.walkQueue;
        window.networking = this.networking;
        
        this.init();
    }

    async init() {
        try {
            // Initialize database
            await this.walkQueue.init();
            
            // Initialize map
            this.initMap();
            
            // Initialize UI
            this.initUI();
            
            // Initialize event listeners
            this.initEventListeners();
            
            // Update initial status
            await this.updateStatus();
            
            console.log('PatTrail app initialized successfully');
        } catch (error) {
            console.error('Failed to initialize app:', error);
            this.showToast('error', 'Initialization Error', 'Failed to initialize the application.');
        }
    }

    initMap() {
        // Create map instance centered on default coordinates
        this.map = L.map('map').setView([0, 0], 2);
        
        // Add OpenStreetMap tile layer with proper attribution
        const osmTiles = L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
            maxZoom: 19,
            subdomains: 'abc'
        });
        
        // Add the tile layer to the map
        osmTiles.addTo(this.map);
        
        // Add scale control
        L.control.scale().addTo(this.map);
        
        console.log('Leaflet map initialized with OpenStreetMap tiles');
    }

    initUI() {
        // Get UI elements
        this.startBtn = document.getElementById('start-btn');
        this.stopBtn = document.getElementById('stop-btn');
        this.statusText = document.getElementById('status-text');
        this.statusDetails = document.getElementById('status-details');
        this.pointsCount = document.getElementById('points-count');
        this.queuedBatches = document.getElementById('queued-batches');
        this.recordingTime = document.getElementById('recording-time');
        this.summaryModal = document.getElementById('summary-modal');
        this.summaryContent = document.getElementById('summary-content');
        
        // Initialize modal close functionality
        const closeBtn = this.summaryModal.querySelector('.close');
        closeBtn.onclick = () => this.hideSummaryModal();
        
        // Close modal when clicking outside
        window.onclick = (event) => {
            if (event.target === this.summaryModal) {
                this.hideSummaryModal();
            }
        };
    }

    initEventListeners() {
        // Start recording button
        this.startBtn.addEventListener('click', () => this.startRecording());
        
        // Stop recording button
        this.stopBtn.addEventListener('click', () => this.stopRecording());
        
        // Online/offline events
        window.addEventListener('online', () => this.handleOnline());
        window.addEventListener('offline', () => this.handleOffline());
        
        // Page visibility events
        document.addEventListener('visibilitychange', () => this.handleVisibilityChange());
        
        // Before unload - try to send remaining data
        window.addEventListener('beforeunload', (event) => this.handleBeforeUnload(event));
        
        // PWA installation events
        window.addEventListener('beforeinstallprompt', (e) => {
            e.preventDefault();
            window.deferredPrompt = e;
            console.log('PWA install prompt ready');
        });
        
        window.addEventListener('appinstalled', (evt) => {
            console.log('PWA installed successfully');
            window.deferredPrompt = null;
        });
        
        // Service worker message handling
        if ('serviceWorker' in navigator) {
            navigator.serviceWorker.addEventListener('message', (event) => {
                if (event.data && event.data.type === 'DRAIN_QUEUE') {
                    console.log('Background sync triggered queue drain');
                    this.networking.drainQueue();
                }
            });
        }
    }

    async startRecording() {
        try {
            this.startBtn.disabled = true;
            this.updateStatus('Starting walk...');
            
            // For demo purposes, use pet ID 1 (you might want to add a pet selection UI)
            const petId = 1;
            
            // Call backend to start walk
            const result = await this.networking.startWalk(petId);
            
            if (!result.success) {
                throw new Error(result.error?.message || 'Failed to start walk');
            }
            
            // Store walk ID and start time
            this.currentWalkId = result.data.walkId;
            this.recordingStartTime = new Date(result.data.startedAt);
            
            // Start GPS tracking
            await this.startGeolocationTracking();
            
            // Start flush timer
            this.startFlushTimer();
            
            // Start recording timer
            this.startRecordingTimer();
            
            // Register background sync for queue draining
            if ('serviceWorker' in navigator && 'sync' in window.ServiceWorkerRegistration.prototype) {
                navigator.serviceWorker.ready.then(registration => {
                    registration.sync.register('upload-points')
                        .then(() => console.log('Background sync registered'))
                        .catch(error => console.error('Background sync registration failed:', error));
                });
            }
            
            // Update UI
            this.isRecording = true;
            this.startBtn.disabled = true;
            this.stopBtn.disabled = false;
            this.updateStatus('Recording walk...');
            
            this.showToast('success', 'Walk Started', `Recording started for walk #${this.currentWalkId}`);
            
            console.log(`Walk ${this.currentWalkId} started successfully`);
            
        } catch (error) {
            console.error('Failed to start recording:', error);
            this.showToast('error', 'Start Failed', error.message);
            this.updateStatus('Failed to start recording');
            this.startBtn.disabled = false;
        }
    }

    async startGeolocationTracking() {
        return new Promise((resolve, reject) => {
            if (!navigator.geolocation) {
                reject(new Error('Geolocation not supported'));
                return;
            }
            
            this.geolocationId = navigator.geolocation.watchPosition(
                (position) => this.handleGeolocationSuccess(position),
                (error) => this.handleGeolocationError(error),
                this.geolocationOptions
            );
            
            // Resolve after a short delay to allow first position
            setTimeout(resolve, 1000);
        });
    }

    handleGeolocationSuccess(position) {
        if (!this.isRecording) return;
        
        const point = {
            lat: position.coords.latitude,
            lon: position.coords.longitude,
            ts: new Date(position.timestamp).toISOString(),
            elev: position.coords.altitude || undefined
        };
        
        // Validate coordinates (clip bogus values)
        if (point.lat < -90 || point.lat > 90 || point.lon < -180 || point.lon > 180) {
            console.warn('Invalid coordinates received:', point);
            return;
        }
        
        // Add to buffer
        this.buffer.push(point);
        
        // Update points count
        this.updatePointsCount();
        
        // Check if buffer is full
        if (this.buffer.length >= this.bufferSize) {
            this.flushBuffer();
        }
        
        // Update map position (center on current location)
        if (this.map) {
            this.map.setView([point.lat, point.lon], this.map.getZoom());
        }
        
        console.log('GPS point recorded:', point);
    }

    handleGeolocationError(error) {
        console.error('Geolocation error:', error);
        
        let message = 'GPS error occurred';
        switch (error.code) {
            case error.PERMISSION_DENIED:
                message = 'Location permission denied. Please enable location access.';
                break;
            case error.POSITION_UNAVAILABLE:
                message = 'Location information unavailable.';
                break;
            case error.TIMEOUT:
                message = 'Location request timed out.';
                break;
        }
        
        this.showToast('error', 'GPS Error', message);
        this.updateStatus('GPS error - check location permissions');
    }

    startFlushTimer() {
        this.flushTimer = setInterval(() => {
            if (this.buffer.length > 0) {
                this.flushBuffer();
            }
        }, this.flushInterval);
    }

    startRecordingTimer() {
        this.recordingTimer = setInterval(() => {
            this.updateRecordingTime();
        }, 1000);
    }

    async flushBuffer() {
        if (this.buffer.length === 0 || !this.currentWalkId) return;
        
        const points = [...this.buffer];
        this.buffer = [];
        
        // Update points count
        this.updatePointsCount();
        
        // Add to queue before attempting to send
        await this.walkQueue.addBatch({
            walkId: this.currentWalkId,
            points: points
        });
        
        // Update queue status
        await this.networking.updateQueueStatus();
        
        // Try to drain queue
        await this.networking.drainQueue();
        
        console.log(`Flushed ${points.length} points to queue`);
    }

    async stopRecording() {
        try {
            this.stopBtn.disabled = true;
            this.updateStatus('Stopping walk...');
            
            // Stop GPS tracking
            if (this.geolocationId) {
                navigator.geolocation.clearWatch(this.geolocationId);
                this.geolocationId = null;
            }
            
            // Stop timers
            if (this.flushTimer) {
                clearInterval(this.flushTimer);
                this.flushTimer = null;
            }
            
            if (this.recordingTimer) {
                clearInterval(this.recordingTimer);
                this.recordingTimer = null;
            }
            
            // Flush remaining buffer
            if (this.buffer.length > 0) {
                await this.flushBuffer();
            }
            
            // Wait for queue to drain
            await this.networking.drainQueue();
            
            // Call backend to stop walk
            const result = await this.networking.stopWalk(this.currentWalkId);
            
            if (!result.success) {
                throw new Error(result.error?.message || 'Failed to stop walk');
            }
            
            // Show summary
            this.showWalkSummary(result.data);
            
            // Reset state
            this.isRecording = false;
            this.currentWalkId = null;
            this.recordingStartTime = null;
            this.buffer = [];
            this.lastSentTimestamps.clear();
            
            // Update UI
            this.startBtn.disabled = false;
            this.stopBtn.disabled = true;
            this.updateStatus('Ready to record');
            this.updatePointsCount();
            this.updateRecordingTime();
            
            this.showToast('success', 'Walk Completed', 'Recording stopped successfully');
            
            console.log(`Walk ${this.currentWalkId} stopped successfully`);
            
        } catch (error) {
            console.error('Failed to stop recording:', error);
            this.showToast('error', 'Stop Failed', error.message);
            this.updateStatus('Failed to stop recording');
            this.stopBtn.disabled = false;
        }
    }

    showWalkSummary(data) {
        const pace = data.duracaoS > 0 && data.distanciaM > 0 
            ? ((data.duracaoS / 60) / (data.distanciaM / 1000)).toFixed(2)
            : '0.00';
        
        this.summaryContent.innerHTML = `
            <div class="summary-item">
                <span class="summary-label">Walk ID:</span>
                <span class="summary-value">#${data.walkId}</span>
            </div>
            <div class="summary-item">
                <span class="summary-label">Distance:</span>
                <span class="summary-value">${(data.distanciaM / 1000).toFixed(2)} km</span>
            </div>
            <div class="summary-item">
                <span class="summary-label">Duration:</span>
                <span class="summary-value">${Math.floor(data.duracaoS / 60)}:${(data.duracaoS % 60).toString().padStart(2, '0')}</span>
            </div>
            <div class="summary-item">
                <span class="summary-label">Average Speed:</span>
                <span class="summary-value">${data.velMediaKmh.toFixed(2)} km/h</span>
            </div>
            <div class="summary-item">
                <span class="summary-label">Pace:</span>
                <span class="summary-value">${pace} min/km</span>
            </div>
            <div class="summary-item">
                <span class="summary-label">Started:</span>
                <span class="summary-value">${new Date(data.startedAt).toLocaleString()}</span>
            </div>
            <div class="summary-item">
                <span class="summary-label">Finished:</span>
                <span class="summary-value">${new Date(data.finishedAt).toLocaleString()}</span>
            </div>
        `;
        
        this.summaryModal.style.display = 'block';
    }

    hideSummaryModal() {
        this.summaryModal.style.display = 'none';
    }

    updateStatus(message = null) {
        if (message) {
            this.statusText.textContent = message;
        } else if (this.isRecording) {
            this.statusText.textContent = 'Recording walk...';
            this.statusDetails.textContent = `Walk #${this.currentWalkId}`;
        } else {
            this.statusText.textContent = 'Ready to record';
            this.statusDetails.textContent = '';
        }
    }

    updatePointsCount() {
        if (this.pointsCount) {
            const valueElement = this.pointsCount.querySelector('.info-value');
            if (valueElement) {
                valueElement.textContent = this.buffer.length;
            }
        }
    }

    updateRecordingTime() {
        if (this.recordingTime && this.recordingStartTime) {
            const elapsed = Math.floor((Date.now() - this.recordingStartTime.getTime()) / 1000);
            const minutes = Math.floor(elapsed / 60);
            const seconds = elapsed % 60;
            const timeString = `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
            
            const valueElement = this.recordingTime.querySelector('.info-value');
            if (valueElement) {
                valueElement.textContent = timeString;
            }
        }
    }

    async updateStatus() {
        await this.networking.updateQueueStatus();
    }

    handleOnline() {
        console.log('App is online');
        this.updateStatus();
        this.networking.drainQueue();
        this.showToast('info', 'Connection Restored', 'Internet connection restored. Uploading queued data...');
    }

    handleOffline() {
        console.log('App is offline');
        this.updateStatus();
        this.showToast('warning', 'Connection Lost', 'Internet connection lost. Data will be queued for later upload.');
    }

    handleVisibilityChange() {
        if (document.hidden && this.isRecording && this.buffer.length > 0) {
            // Page is hidden, try to send remaining data
            this.flushBuffer();
        }
    }

    handleBeforeUnload(event) {
        if (this.isRecording && this.buffer.length > 0 && this.currentWalkId) {
            // Try to send remaining data using sendBeacon
            const success = this.networking.sendBeaconFallback(this.currentWalkId, this.buffer);
            if (success) {
                console.log('Remaining data sent via sendBeacon');
            }
        }
    }

    showToast(type, title, message) {
        const container = document.getElementById('toast-container');
        const toast = document.createElement('div');
        toast.className = `toast ${type}`;
        
        toast.innerHTML = `
            <div class="toast-header">
                <span class="toast-title">${title}</span>
                <button class="toast-close" onclick="this.parentElement.parentElement.remove()">&times;</button>
            </div>
            <div class="toast-message">${message}</div>
        `;
        
        container.appendChild(toast);
        
        // Auto-remove after 5 seconds
        setTimeout(() => {
            if (toast.parentElement) {
                toast.remove();
            }
        }, 5000);
    }
}

// Global toast function for use by other modules
window.showToast = function(type, title, message) {
    if (window.patTrailApp) {
        window.patTrailApp.showToast(type, title, message);
    }
};

// Initialize app when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    window.patTrailApp = new PatTrailApp();
});
