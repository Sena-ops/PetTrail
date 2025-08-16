/**
 * IndexedDB wrapper for persistent queue storage
 * Stores batches keyed by auto-id with secondary index on nextAttemptAt
 * Also stores metadata for app state persistence
 */

class WalkQueueDB {
    constructor() {
        this.dbName = 'WalkQueueDB';
        this.version = 2; // Increment version for metadata store
        this.batchesStoreName = 'batches';
        this.metadataStoreName = 'metadata';
        this.db = null;
    }

    async init() {
        return new Promise((resolve, reject) => {
            const request = indexedDB.open(this.dbName, this.version);

            request.onerror = () => reject(request.error);
            request.onsuccess = () => {
                this.db = request.result;
                resolve();
            };

            request.onupgradeneeded = (event) => {
                const db = event.target.result;
                
                // Create object store for batches
                if (!db.objectStoreNames.contains(this.batchesStoreName)) {
                    const batchesStore = db.createObjectStore(this.batchesStoreName, {
                        keyPath: 'id',
                        autoIncrement: true
                    });

                    // Create indexes for batches
                    batchesStore.createIndex('walkId', 'walkId', { unique: false });
                    batchesStore.createIndex('nextAttemptAt', 'nextAttemptAt', { unique: false });
                    batchesStore.createIndex('retryCount', 'retryCount', { unique: false });
                }

                // Create object store for metadata
                if (!db.objectStoreNames.contains(this.metadataStoreName)) {
                    const metadataStore = db.createObjectStore(this.metadataStoreName, {
                        keyPath: 'key'
                    });
                }
            };
        });
    }

    // Metadata methods
    async setMetadata(key, value) {
        if (!this.db) await this.init();
        
        return new Promise((resolve, reject) => {
            const transaction = this.db.transaction([this.metadataStoreName], 'readwrite');
            const store = transaction.objectStore(this.metadataStoreName);
            
            const request = store.put({ key, value, updatedAt: Date.now() });

            request.onsuccess = () => resolve();
            request.onerror = () => reject(request.error);
        });
    }

    async getMetadata(key) {
        if (!this.db) await this.init();
        
        return new Promise((resolve, reject) => {
            const transaction = this.db.transaction([this.metadataStoreName], 'readonly');
            const store = transaction.objectStore(this.metadataStoreName);
            
            const request = store.get(key);

            request.onsuccess = () => {
                const result = request.result;
                resolve(result ? result.value : null);
            };
            request.onerror = () => reject(request.error);
        });
    }

    async removeMetadata(key) {
        if (!this.db) await this.init();
        
        return new Promise((resolve, reject) => {
            const transaction = this.db.transaction([this.metadataStoreName], 'readwrite');
            const store = transaction.objectStore(this.metadataStoreName);
            
            const request = store.delete(key);

            request.onsuccess = () => resolve();
            request.onerror = () => reject(request.error);
        });
    }

    // App state persistence methods
    async saveCurrentWalk(walkId, startedAt) {
        await this.setMetadata('currentWalkId', walkId);
        await this.setMetadata('walkStartedAt', startedAt);
        await this.setMetadata('isRecording', true);
    }

    async getCurrentWalk() {
        const walkId = await this.getMetadata('currentWalkId');
        const startedAt = await this.getMetadata('walkStartedAt');
        const isRecording = await this.getMetadata('isRecording');
        
        if (walkId && startedAt && isRecording) {
            return {
                walkId: parseInt(walkId),
                startedAt: new Date(startedAt),
                isRecording: Boolean(isRecording)
            };
        }
        return null;
    }

    async clearCurrentWalk() {
        await this.removeMetadata('currentWalkId');
        await this.removeMetadata('walkStartedAt');
        await this.setMetadata('isRecording', false);
    }

    async updateLastSynced() {
        await this.setMetadata('lastSyncedAt', Date.now());
    }

    async getLastSynced() {
        const timestamp = await this.getMetadata('lastSyncedAt');
        return timestamp ? new Date(timestamp) : null;
    }

    // Batch methods (existing functionality)
    async addBatch(batch) {
        if (!this.db) await this.init();
        
        return new Promise((resolve, reject) => {
            const transaction = this.db.transaction([this.batchesStoreName], 'readwrite');
            const store = transaction.objectStore(this.batchesStoreName);
            
            const request = store.add({
                walkId: batch.walkId,
                points: batch.points,
                retryCount: 0,
                nextAttemptAt: Date.now(),
                createdAt: Date.now()
            });

            request.onsuccess = () => resolve(request.result);
            request.onerror = () => reject(request.error);
        });
    }

    async getNextReadyBatch() {
        if (!this.db) await this.init();
        
        return new Promise((resolve, reject) => {
            const transaction = this.db.transaction([this.batchesStoreName], 'readonly');
            const store = transaction.objectStore(this.batchesStoreName);
            const index = store.index('nextAttemptAt');
            
            const now = Date.now();
            const range = IDBKeyRange.upperBound(now);
            
            const request = index.openCursor(range);
            
            request.onsuccess = () => {
                const cursor = request.result;
                if (cursor) {
                    resolve({
                        id: cursor.key,
                        ...cursor.value
                    });
                } else {
                    resolve(null);
                }
            };
            
            request.onerror = () => reject(request.error);
        });
    }

    async removeBatch(id) {
        if (!this.db) await this.init();
        
        return new Promise((resolve, reject) => {
            const transaction = this.db.transaction([this.batchesStoreName], 'readwrite');
            const store = transaction.objectStore(this.batchesStoreName);
            
            const request = store.delete(id);
            
            request.onsuccess = () => resolve();
            request.onerror = () => reject(request.error);
        });
    }

    async updateBatchRetry(id, retryCount, nextAttemptAt) {
        if (!this.db) await this.init();
        
        return new Promise((resolve, reject) => {
            const transaction = this.db.transaction([this.batchesStoreName], 'readwrite');
            const store = transaction.objectStore(this.batchesStoreName);
            
            const getRequest = store.get(id);
            
            getRequest.onsuccess = () => {
                const batch = getRequest.result;
                if (batch) {
                    batch.retryCount = retryCount;
                    batch.nextAttemptAt = nextAttemptAt;
                    
                    const putRequest = store.put(batch);
                    putRequest.onsuccess = () => resolve();
                    putRequest.onerror = () => reject(putRequest.error);
                } else {
                    reject(new Error('Batch not found'));
                }
            };
            
            getRequest.onerror = () => reject(getRequest.error);
        });
    }

    async getBatchesByWalkId(walkId) {
        if (!this.db) await this.init();
        
        return new Promise((resolve, reject) => {
            const transaction = this.db.transaction([this.batchesStoreName], 'readonly');
            const store = transaction.objectStore(this.batchesStoreName);
            const index = store.index('walkId');
            
            const request = index.getAll(walkId);
            
            request.onsuccess = () => {
                const batches = request.result.map((batch, index) => ({
                    id: batch.id || index,
                    ...batch
                }));
                resolve(batches);
            };
            
            request.onerror = () => reject(request.error);
        });
    }

    async getQueueSize() {
        if (!this.db) await this.init();
        
        return new Promise((resolve, reject) => {
            const transaction = this.db.transaction([this.batchesStoreName], 'readonly');
            const store = transaction.objectStore(this.batchesStoreName);
            
            const request = store.count();
            
            request.onsuccess = () => resolve(request.result);
            request.onerror = () => reject(request.error);
        });
    }

    async clearAll() {
        if (!this.db) await this.init();
        
        return new Promise((resolve, reject) => {
            const transaction = this.db.transaction([this.batchesStoreName, this.metadataStoreName], 'readwrite');
            const batchesStore = transaction.objectStore(this.batchesStoreName);
            const metadataStore = transaction.objectStore(this.metadataStoreName);
            
            const batchesRequest = batchesStore.clear();
            const metadataRequest = metadataStore.clear();
            
            Promise.all([
                new Promise((res, rej) => {
                    batchesRequest.onsuccess = res;
                    batchesRequest.onerror = rej;
                }),
                new Promise((res, rej) => {
                    metadataRequest.onsuccess = res;
                    metadataRequest.onerror = rej;
                })
            ]).then(resolve).catch(reject);
        });
    }

    async removeBatchesForWalk(walkId) {
        if (!this.db) await this.init();
        
        return new Promise((resolve, reject) => {
            const transaction = this.db.transaction([this.batchesStoreName], 'readwrite');
            const store = transaction.objectStore(this.batchesStoreName);
            const index = store.index('walkId');
            
            const request = index.openCursor(walkId);
            
            request.onsuccess = () => {
                const cursor = request.result;
                if (cursor) {
                    cursor.delete();
                    cursor.continue();
                } else {
                    resolve();
                }
            };
            
            request.onerror = () => reject(request.error);
        });
    }
}

// Export for use in other modules
window.WalkQueueDB = WalkQueueDB;
