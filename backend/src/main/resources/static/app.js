/**
 * PatTrail PWA - GPS Recording Application
 * Implements GPS recording with batching and resilient delivery to backend
 * Enhanced with offline support and state persistence
 */

/**
 * LocationController - Handles real-time location tracking with marker and accuracy circle
 */
class LocationController {
    constructor(map) {
        this.map = map;
        this.isEnabled = false;
        this.followMe = false;
        this.watchId = null;
        this.marker = null;
        this.accuracyCircle = null;
        this.lastAccuracy = null;
        
        // UI elements
        this.locateBtn = null;
        this.followBtn = null;
        this.locationStatus = null;
        this.locationError = null;
        this.locationIndicator = null;
        
        // State persistence key
        this.stateKey = 'pattrail_location_state';
        
        this.init();
    }
    
    init() {
        // Create Leaflet elements once
        this.marker = L.marker([0, 0], { 
            keyboard: false,
            icon: L.divIcon({
                className: 'location-marker',
                html: '<div class="marker-pulse"></div>',
                iconSize: [20, 20],
                iconAnchor: [10, 10]
            })
        });
        
        this.accuracyCircle = L.circle([0, 0], { 
            radius: 0,
            color: '#2196F3',
            fillColor: '#2196F3',
            fillOpacity: 0.2,
            weight: 2
        });
        
        // Restore state
        this.restoreState();
    }
    
    setUIElements(locateBtn, followBtn, locationStatus, locationError, locationIndicator) {
        this.locateBtn = locateBtn;
        this.followBtn = followBtn;
        this.locationStatus = locationStatus;
        this.locationError = locationError;
        this.locationIndicator = locationIndicator;
        
        // Add event listeners
        this.locateBtn.addEventListener('click', () => this.toggleLocate());
        this.followBtn.addEventListener('click', () => this.toggleFollow());
        
        // Update UI based on current state
        this.updateUI();
    }
    
    async enableLocate() {
        if (this.isEnabled) return;
        
        if (!navigator.geolocation) {
            this.showError('Geolocation API not available in this browser.');
            return;
        }
        
        try {
            this.updateStatus('Locating...');
            this.showError(null);
            
            // Request permission and get initial position
            const position = await this.getCurrentPosition();
            
            // Start watching position
            this.watchId = navigator.geolocation.watchPosition(
                (pos) => this.handlePositionUpdate(pos),
                (error) => this.handlePositionError(error),
                {
                    enableHighAccuracy: true,
                    maximumAge: 2000,
                    timeout: 10000
                }
            );
            
            this.isEnabled = true;
            this.updateStatus('Live');
            this.saveState();
            this.updateUI();
            
            console.log('Location tracking enabled');
        } catch (error) {
            this.handlePositionError(error);
        }
    }
    
    disableLocate() {
        if (!this.isEnabled) return;
        
        if (this.watchId) {
            navigator.geolocation.clearWatch(this.watchId);
            this.watchId = null;
        }
        
        this.isEnabled = false;
        this.followMe = false;
        this.updateStatus('Off');
        this.saveState();
        this.updateUI();
        
        // Remove marker and circle from map
        if (this.marker) {
            this.map.removeLayer(this.marker);
        }
        if (this.accuracyCircle) {
            this.map.removeLayer(this.accuracyCircle);
        }
        
        console.log('Location tracking disabled');
    }
    
    setFollow(enabled) {
        this.followMe = enabled;
        this.saveState();
        this.updateUI();
    }
    
    toggleLocate() {
        if (this.isEnabled) {
            this.disableLocate();
        } else {
            this.enableLocate();
        }
    }
    
    toggleFollow() {
        if (!this.isEnabled) return;
        
        this.setFollow(!this.followMe);
    }
    
    getCurrentPosition() {
        return new Promise((resolve, reject) => {
            navigator.geolocation.getCurrentPosition(resolve, reject, {
                enableHighAccuracy: true,
                timeout: 10000,
                maximumAge: 0
            });
        });
    }
    
    handlePositionUpdate(position) {
        const lat = position.coords.latitude;
        const lon = position.coords.longitude;
        const accuracy = position.coords.accuracy;
        
        // De-jitter: ignore updates with accuracy > 100m unless no prior fix
        if (accuracy > 100 && this.lastAccuracy !== null) {
            console.log('Ignoring low accuracy position:', accuracy + 'm');
            return;
        }
        
        this.updateMarkerPosition(lat, lon, accuracy);
        
        if (this.followMe) {
            this.map.setView([lat, lon], this.map.getZoom());
        }
        
        // Notify app if recording
        if (window.patTrailApp && window.patTrailApp.isRecording) {
            this.onPositionUpdate(position);
        }
    }
    
    handlePositionError(error) {
        console.error('Geolocation error:', error);
        
        let errorMessage = 'Unknown location error';
        switch (error.code) {
            case error.PERMISSION_DENIED:
                errorMessage = 'Location permission denied. Please enable location access.';
                break;
            case error.POSITION_UNAVAILABLE:
                errorMessage = 'Location information unavailable.';
                break;
            case error.TIMEOUT:
                errorMessage = 'Location request timed out.';
                break;
        }
        
        this.showError(errorMessage);
        this.updateStatus('Error');
        this.updateUI();
    }
    
    updateMarkerPosition(lat, lon, accuracy) {
        const latLng = [lat, lon];
        
        // Update marker position
        this.marker.setLatLng(latLng);
        
        // Add to map if not already added
        if (!this.map.hasLayer(this.marker)) {
            this.marker.addTo(this.map);
        }
        
        // Update accuracy circle
        this.accuracyCircle.setLatLng(latLng).setRadius(accuracy);
        
        // Add to map if not already added
        if (!this.map.hasLayer(this.accuracyCircle)) {
            this.accuracyCircle.addTo(this.map);
        }
        
        this.lastAccuracy = accuracy;
    }
    
    updateStatus(status) {
        if (this.locationStatus) {
            this.locationStatus.textContent = status;
            this.locationStatus.className = 'location-status ' + status.toLowerCase();
        }
        
        if (this.locationIndicator) {
            this.locationIndicator.style.display = status === 'Live' ? 'flex' : 'none';
            if (status === 'Locating...') {
                this.locationIndicator.style.display = 'flex';
            }
        }
    }
    
    showError(message) {
        const errorElement = this.locationError;
        if (!errorElement) return;
        
        if (message) {
            errorElement.querySelector('.error-text').textContent = message;
            errorElement.style.display = 'flex';
        } else {
            errorElement.style.display = 'none';
        }
    }
    
    updateUI() {
        if (this.locateBtn) {
            this.locateBtn.classList.toggle('active', this.isEnabled);
            this.locateBtn.textContent = this.isEnabled ? 'Stop Locating' : 'Locate Me';
        }
        
        if (this.followBtn) {
            this.followBtn.disabled = !this.isEnabled;
            this.followBtn.classList.toggle('active', this.followMe);
        }
    }
    
    saveState() {
        try {
            const state = {
                isEnabled: this.isEnabled,
                followMe: this.followMe
            };
            localStorage.setItem(this.stateKey, JSON.stringify(state));
        } catch (error) {
            console.error('Error saving location state:', error);
        }
    }
    
    restoreState() {
        try {
            const saved = localStorage.getItem(this.stateKey);
            if (saved) {
                const state = JSON.parse(saved);
                this.isEnabled = state.isEnabled || false;
                this.followMe = state.followMe || false;
            }
        } catch (error) {
            console.error('Error restoring location state:', error);
        }
    }
    
    onRecordingStart() {
        // If location tracking is already enabled, reuse the watch
        if (this.isEnabled && this.watchId) {
            console.log('Reusing existing location watch for recording');
        }
    }
    
    onRecordingStop() {
        // Keep location tracking active if it was enabled by user
        console.log('Recording stopped, location tracking remains active');
    }
    
    onPositionUpdate(position) {
        // This method is called when recording is active
        // The position is already handled by handlePositionUpdate
    }
}

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
        
        // Location controller for real-time location tracking
        this.locationController = null;
        
        // Make available globally for debugging
        window.walkQueue = this.walkQueue;
        window.networking = this.networking;
        
        this.init();
    }

    async init() {
        try {
            console.log('Starting PatTrail app initialization...');
            
            // Initialize database first
            console.log('Initializing database...');
            await this.walkQueue.init();
            
            // Initialize map
            console.log('Initializing map...');
            this.initMap();
            
            // Initialize UI
            console.log('Initializing UI...');
            this.initUI();
            
            // Initialize event listeners
            console.log('Initializing event listeners...');
            this.initEventListeners();
            
            // Restore app state from IndexedDB
            console.log('Restoring app state...');
            await this.restoreAppState();
            
            // Initialize status indicators
            console.log('Initializing status indicators...');
            await this.networking.initStatusIndicators();
            
            // Load pets for selection
            console.log('Loading pets...');
            await this.loadPets();
            
            // Update initial status
            console.log('Updating initial status...');
            await this.updateStatus();
            
            console.log('PatTrail app initialized successfully');
        } catch (error) {
            console.error('Failed to initialize app:', error);
            // Use a simple alert instead of showToast to avoid initialization issues
            alert('Failed to initialize the application: ' + error.message);
        }
    }

    async restoreAppState() {
        try {
            const currentWalk = await this.walkQueue.getCurrentWalk();
            if (currentWalk) {
                console.log('Restoring app state from previous session:', currentWalk);
                
                this.currentWalkId = currentWalk.walkId;
                this.recordingStartTime = currentWalk.startedAt;
                this.isRecording = currentWalk.isRecording;
                
                if (this.isRecording) {
                    // Resume recording
                    await this.resumeRecording();
                }
            }
            
            // Restore location controller state if it was enabled
            if (this.locationController) {
                this.locationController.restoreState();
                if (this.locationController.isEnabled) {
                    console.log('Restoring location tracking state');
                    // Don't auto-enable location tracking on restore, let user control it
                }
            }
        } catch (error) {
            console.error('Error restoring app state:', error);
        }
    }

    async resumeRecording() {
        try {
            console.log('Resuming recording for walk:', this.currentWalkId);
            
            // Start GPS tracking
            await this.startGeolocationTracking();
            
            // Start flush timer
            this.startFlushTimer();
            
            // Start recording timer
            this.startRecordingTimer();
            
            // Update UI
            this.startBtn.disabled = true;
            this.stopBtn.disabled = false;
            this.petSelect.disabled = true;
            this.updateStatus('Recording walk...');
            
            // Update status indicators
            this.networking.updateStatusIndicators();
            
            // Try to drain any pending batches
            await this.networking.drainQueue();
            
            this.showToast('info', 'Recording Resumed', `Resumed recording for walk #${this.currentWalkId}`);
            
        } catch (error) {
            console.error('Failed to resume recording:', error);
            this.showToast('error', 'Resume Failed', 'Failed to resume recording. Please restart manually.');
            this.isRecording = false;
            this.currentWalkId = null;
        }
    }

    initMap() {
        try {
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
            
            // Initialize location controller
            this.locationController = new LocationController(this.map);
            
            console.log('Leaflet map initialized with OpenStreetMap tiles');
        } catch (error) {
            console.error('Error initializing map:', error);
        }
    }

    initUI() {
        try {
            console.log('Initializing UI...');
            
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
            this.petSelect = document.getElementById('pet-select');
            
            // Get location control elements
            const locateBtn = document.getElementById('locate-btn');
            const followBtn = document.getElementById('follow-btn');
            const locationStatus = document.getElementById('location-status');
            const locationError = document.getElementById('location-error');
            const locationIndicator = document.getElementById('location-indicator');
            
            console.log('Location elements found:', {
                locateBtn: !!locateBtn,
                followBtn: !!followBtn,
                locationStatus: !!locationStatus,
                locationError: !!locationError,
                locationIndicator: !!locationIndicator
            });
            
            // Set up location controller UI elements
            if (this.locationController) {
                console.log('Setting up location controller UI elements...');
                this.locationController.setUIElements(locateBtn, followBtn, locationStatus, locationError, locationIndicator);
            } else {
                console.error('Location controller not initialized!');
            }
            
            // Initialize modal close functionality
            const closeBtn = this.summaryModal.querySelector('.close');
            closeBtn.onclick = () => this.hideSummaryModal();
            
            // Close modal when clicking outside
            window.onclick = (event) => {
                if (event.target === this.summaryModal) {
                    this.hideSummaryModal();
                }
            };
            
            console.log('UI initialization complete');
        } catch (error) {
            console.error('Error initializing UI:', error);
        }
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

    async loadPets() {
        try {
            this.updateStatus('Loading pets...');
            
            const result = await this.networking.getPets();
            
            if (!result.success) {
                throw new Error(result.error?.message || 'Failed to load pets');
            }
            
            const pets = result.data;
            
            // Clear existing options
            this.petSelect.innerHTML = '';
            
            if (pets.length === 0) {
                // No pets available
                this.petSelect.innerHTML = '<option value="">No pets available</option>';
                this.petSelect.disabled = true;
                this.startBtn.disabled = true;
                this.showToast('warning', 'No Pets', 'No pets are available. Please add a pet first.');
            } else {
                // Add default option
                this.petSelect.innerHTML = '<option value="">Select a pet...</option>';
                
                // Add pet options
                pets.forEach(pet => {
                    const option = document.createElement('option');
                    option.value = pet.id;
                    option.textContent = `${pet.name} (${pet.species})`;
                    this.petSelect.appendChild(option);
                });
                
                this.petSelect.disabled = false;
                this.startBtn.disabled = false;
            }
            
            this.updateStatus('Ready to record');
            
        } catch (error) {
            console.error('Failed to load pets:', error);
            this.petSelect.innerHTML = '<option value="">Failed to load pets</option>';
            this.petSelect.disabled = true;
            this.startBtn.disabled = true;
            this.showToast('error', 'Load Failed', 'Failed to load pets. Please refresh the page.');
            this.updateStatus('Failed to load pets');
        }
    }

    async startRecording() {
        try {
            // Check if a pet is selected
            const selectedPetId = this.petSelect.value;
            if (!selectedPetId) {
                this.showToast('error', 'No Pet Selected', 'Please select a pet to start recording.');
                return;
            }
            
            this.startBtn.disabled = true;
            this.updateStatus('Starting walk...');
            
            // Call backend to start walk
            const result = await this.networking.startWalk(selectedPetId);
            
            if (!result.success) {
                throw new Error(result.error?.message || 'Failed to start walk');
            }
            
            // Store walk ID and start time
            this.currentWalkId = result.data.walkId;
            this.recordingStartTime = new Date(result.data.startedAt);
            
            // Persist walk state to IndexedDB
            await this.walkQueue.saveCurrentWalk(this.currentWalkId, this.recordingStartTime.toISOString());
            
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
            this.petSelect.disabled = true;
            this.updateStatus('Recording walk...');
            
            // Update status indicators
            this.networking.updateStatusIndicators();
            
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
            
            // Check if location controller is already watching
            if (this.locationController && this.locationController.isEnabled) {
                console.log('Reusing location controller watch for recording');
                this.locationController.onRecordingStart();
                setTimeout(resolve, 1000);
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
        
        // Update location controller if it's enabled
        if (this.locationController && this.locationController.isEnabled) {
            this.locationController.onPositionUpdate(position);
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
            
            // Stop GPS tracking (only if location controller is not enabled)
            if (this.geolocationId && (!this.locationController || !this.locationController.isEnabled)) {
                navigator.geolocation.clearWatch(this.geolocationId);
                this.geolocationId = null;
            }
            
            // Notify location controller
            if (this.locationController) {
                this.locationController.onRecordingStop();
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
            
            // Clear walk state from IndexedDB
            await this.walkQueue.clearCurrentWalk();
            
            // Reset state
            this.isRecording = false;
            this.currentWalkId = null;
            this.recordingStartTime = null;
            this.buffer = [];
            this.lastSentTimestamps.clear();
            
            // Update UI
            this.startBtn.disabled = false;
            this.stopBtn.disabled = true;
            this.petSelect.disabled = false;
            this.updateStatus('Ready to record');
            this.updatePointsCount();
            this.updateRecordingTime();
            
            // Update status indicators
            this.networking.updateStatusIndicators();
            
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
        this.networking.updateStatusIndicators();
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
        try {
            const container = document.getElementById('toast-container');
            if (!container) {
                console.warn('Toast container not found, using console instead');
                console.log(`[${type.toUpperCase()}] ${title}: ${message}`);
                return;
            }
            
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
        } catch (error) {
            console.error('Error showing toast:', error);
            console.log(`[${type.toUpperCase()}] ${title}: ${message}`);
        }
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
    console.log('DOM loaded, initializing PatTrail app...');
    try {
        window.patTrailApp = new PatTrailApp();
    } catch (error) {
        console.error('Failed to create PatTrail app:', error);
        alert('Failed to create application: ' + error.message);
    }
});
