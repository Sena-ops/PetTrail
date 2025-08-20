/**
 * Networking helpers for API communication
 * Handles batching, retry logic, and queue management
 */

class WalkNetworking {
    constructor() {
        this.baseUrl = '/api';
        this.drainingQueue = false;
        this.maxRetries = 10;
        this.baseBackoffMs = 5000; // 5 seconds
        this.maxBackoffMs = 60000; // 60 seconds
    }

    /**
     * Send a batch of points to the server
     */
    async sendPointsBatch(walkId, points) {
        try {
            const response = await fetch(`${this.baseUrl}/walks/${walkId}/points`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(points)
            });

            if (response.ok) {
                const result = await response.json();
                console.log(`Batch sent successfully: ${result.accepted}/${result.received} points accepted`);
                return { success: true, response: result };
            } else {
                const errorData = await response.json().catch(() => ({}));
                console.error(`Batch send failed: ${response.status}`, errorData);
                
                // Handle specific error cases
                if (response.status === 409) {
                    // Walk already finished - stop retrying
                    return { success: false, stopRetrying: true, error: 'WALK_FINISHED' };
                } else if (response.status === 404) {
                    // Walk not found - stop retrying
                    return { success: false, stopRetrying: true, error: 'WALK_NOT_FOUND' };
                } else if (response.status === 400) {
                    // Validation error - drop batch
                    return { success: false, stopRetrying: true, error: 'VALIDATION_ERROR' };
                } else {
                    // Network/server error - retry
                    return { success: false, stopRetrying: false, error: 'NETWORK_ERROR' };
                }
            }
        } catch (error) {
            console.error('Network error sending batch:', error);
            return { success: false, stopRetrying: false, error: 'NETWORK_ERROR' };
        }
    }

    /**
     * Get all pets from the server
     */
    async getPets() {
        try {
            const response = await fetch(`${this.baseUrl}/pets`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                }
            });

            if (response.ok) {
                const pets = await response.json();
                console.log('Pets loaded:', pets);
                return { success: true, data: pets };
            } else {
                const errorData = await response.json().catch(() => ({}));
                console.error('Failed to get pets:', response.status, errorData);
                return { success: false, error: errorData };
            }
        } catch (error) {
            console.error('Network error getting pets:', error);
            return { success: false, error: { code: 'NETWORK_ERROR', message: error.message } };
        }
    }

    /**
     * Start a walk for a pet
     */
    async startWalk(petId) {
        try {
            const response = await fetch(`${this.baseUrl}/walks/start?petId=${petId}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                }
            });

            if (response.ok) {
                const result = await response.json();
                console.log('Walk started:', result);
                return { success: true, data: result };
            } else {
                const errorData = await response.json().catch(() => ({}));
                console.error('Failed to start walk:', response.status, errorData);
                return { success: false, error: errorData };
            }
        } catch (error) {
            console.error('Network error starting walk:', error);
            return { success: false, error: { code: 'NETWORK_ERROR', message: error.message } };
        }
    }

    /**
     * Stop a walk and get summary
     */
    async stopWalk(walkId) {
        try {
            const response = await fetch(`${this.baseUrl}/walks/${walkId}/stop`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                }
            });

            if (response.ok) {
                const result = await response.json();
                console.log('Walk stopped:', result);
                return { success: true, data: result };
            } else {
                const errorData = await response.json().catch(() => ({}));
                console.error('Failed to stop walk:', response.status, errorData);
                return { success: false, error: errorData };
            }
        } catch (error) {
            console.error('Network error stopping walk:', error);
            return { success: false, error: { code: 'NETWORK_ERROR', message: error.message } };
        }
    }

    /**
     * Calculate exponential backoff delay
     */
    calculateBackoff(retryCount) {
        const delay = Math.min(
            this.baseBackoffMs * Math.pow(2, retryCount),
            this.maxBackoffMs
        );
        return delay + Math.random() * 1000; // Add jitter
    }

    /**
     * Drain the queue of pending batches
     */
    async drainQueue() {
        if (this.drainingQueue || !navigator.onLine) {
            return;
        }

        this.drainingQueue = true;
        this.setSyncingIndicator(true);
        console.log('Starting queue drain...');

        try {
            while (true) {
                const batch = await window.walkQueue.getNextReadyBatch();
                if (!batch) {
                    break; // No more batches to process
                }

                console.log(`Processing batch ${batch.id} for walk ${batch.walkId} (${batch.points.length} points)`);
                
                const result = await this.sendPointsBatch(batch.walkId, batch.points);
                
                if (result.success) {
                    // Success - remove from queue
                    await window.walkQueue.removeBatch(batch.id);
                    console.log(`Batch ${batch.id} sent successfully`);
                    
                    // Update UI
                    this.updateQueueStatus();
                } else if (result.stopRetrying) {
                    // Stop retrying - remove from queue
                    await window.walkQueue.removeBatch(batch.id);
                    console.log(`Batch ${batch.id} removed (stop retrying: ${result.error})`);
                    
                    // Update UI
                    this.updateQueueStatus();
                    
                    // Show warning for certain errors
                    if (result.error === 'WALK_FINISHED' || result.error === 'WALK_NOT_FOUND') {
                        window.showToast('warning', 'Walk Error', 
                            result.error === 'WALK_FINISHED' 
                                ? 'Walk has already finished. Some points may not be saved.' 
                                : 'Walk not found. Recording stopped.');
                    }
                } else {
                    // Retry with backoff
                    const newRetryCount = batch.retryCount + 1;
                    if (newRetryCount > this.maxRetries) {
                        // Max retries reached - remove from queue
                        await window.walkQueue.removeBatch(batch.id);
                        console.log(`Batch ${batch.id} removed (max retries reached)`);
                        window.showToast('error', 'Upload Failed', 
                            'Some points could not be uploaded after multiple attempts.');
                    } else {
                        const nextAttemptAt = Date.now() + this.calculateBackoff(newRetryCount);
                        await window.walkQueue.updateBatchRetry(batch.id, newRetryCount, nextAttemptAt);
                        console.log(`Batch ${batch.id} scheduled for retry ${newRetryCount} at ${new Date(nextAttemptAt)}`);
                    }
                    
                    // Update UI
                    this.updateQueueStatus();
                    this.updateLastSyncedDisplay();
                }
            }
        } catch (error) {
            console.error('Error draining queue:', error);
        } finally {
            this.drainingQueue = false;
            this.setSyncingIndicator(false);
            console.log('Queue drain completed');
        }
    }

    /**
     * Update queue status in UI
     */
    async updateQueueStatus() {
        try {
            if (!window.walkQueue) {
                console.warn('WalkQueue not available yet');
                return;
            }
            const queueSize = await window.walkQueue.getQueueSize();
            const queuedElement = document.getElementById('queued-batches');
            if (queuedElement) {
                const valueElement = queuedElement.querySelector('.info-value');
                if (valueElement) {
                    valueElement.textContent = queueSize;
                }
            }
        } catch (error) {
            console.error('Error updating queue status:', error);
        }
    }

    /**
     * Update last synced display
     */
    async updateLastSyncedDisplay() {
        try {
            if (!window.walkQueue) {
                console.warn('WalkQueue not available yet');
                return;
            }
            const lastSynced = await window.walkQueue.getLastSynced();
            const lastSyncedElement = document.getElementById('last-synced');
            const lastSyncedTimeElement = document.getElementById('last-synced-time');
            
            if (lastSyncedElement && lastSyncedTimeElement) {
                if (lastSynced) {
                    const timeString = lastSynced.toLocaleTimeString();
                    lastSyncedTimeElement.textContent = timeString;
                    lastSyncedElement.style.display = 'block';
                } else {
                    lastSyncedElement.style.display = 'none';
                }
            }
        } catch (error) {
            console.error('Error updating last synced display:', error);
        }
    }

    /**
     * Set syncing indicator
     */
    setSyncingIndicator(show) {
        this.isSyncing = show;
        const syncingIndicator = document.getElementById('syncing-indicator');
        if (syncingIndicator) {
            syncingIndicator.style.display = show ? 'flex' : 'none';
        }
        this.updateStatusIndicators();
    }

    /**
     * Update all status indicators
     */
    updateStatusIndicators() {
        const recordingIndicator = document.getElementById('recording-indicator');
        const offlineIndicator = document.getElementById('offline-indicator');
        const syncingIndicator = document.getElementById('syncing-indicator');
        
        // Show recording indicator if app is recording
        if (recordingIndicator && window.patTrailApp) {
            recordingIndicator.style.display = window.patTrailApp.isRecording ? 'flex' : 'none';
        }
        
        // Show offline indicator if not online
        if (offlineIndicator) {
            offlineIndicator.style.display = !navigator.onLine ? 'flex' : 'none';
        }
        
        // Syncing indicator is managed by setSyncingIndicator
    }

    /**
     * Initialize status indicators on app load
     */
    async initStatusIndicators() {
        // Update last synced display
        await this.updateLastSyncedDisplay();
        
        // Update status indicators
        this.updateStatusIndicators();
        
        // Update queue status
        await this.updateQueueStatus();
    }

    /**
     * Send points using sendBeacon as fallback
     */
    sendBeaconFallback(walkId, points) {
        try {
            const url = `${this.baseUrl}/walks/${walkId}/points`;
            const data = JSON.stringify(points);
            const blob = new Blob([data], { type: 'application/json' });
            
            const success = navigator.sendBeacon(url, blob);
            console.log('SendBeacon fallback:', success ? 'sent' : 'failed');
            return success;
        } catch (error) {
            console.error('SendBeacon error:', error);
            return false;
        }
    }

    /**
     * Check if we're online
     */
    isOnline() {
        return navigator.onLine;
    }
}

// Export for use in other modules
window.WalkNetworking = WalkNetworking;
