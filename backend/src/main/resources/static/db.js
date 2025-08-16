/**
 * IndexedDB wrapper for persistent queue storage
 * Stores batches keyed by auto-id with secondary index on nextAttemptAt
 */

class WalkQueueDB {
    constructor() {
        this.dbName = 'WalkQueueDB';
        this.version = 2;
        this.storeName = 'batches';
        this.db = null;
    }

    async init() {
        return new Promise((resolve, reject) => {
            const request = indexedDB.open(this.dbName, this.version);

            request.onerror = () => {
                console.error('Failed to open database:', request.error);
                // Try to delete the database and recreate it
                this.deleteDatabase().then(() => {
                    console.log('Database deleted, retrying initialization...');
                    this.init().then(resolve).catch(reject);
                }).catch(reject);
            };
            
            request.onsuccess = () => {
                this.db = request.result;
                console.log('Database initialized successfully');
                resolve();
            };

            request.onupgradeneeded = (event) => {
                const db = event.target.result;
                const oldVersion = event.oldVersion;
                
                console.log(`Upgrading database from version ${oldVersion} to ${this.version}`);
                
                // Create object store for batches if it doesn't exist
                if (!db.objectStoreNames.contains(this.storeName)) {
                    const store = db.createObjectStore(this.storeName, {
                        keyPath: 'id',
                        autoIncrement: true
                    });

                    // Create indexes
                    store.createIndex('walkId', 'walkId', { unique: false });
                    store.createIndex('nextAttemptAt', 'nextAttemptAt', { unique: false });
                    store.createIndex('retryCount', 'retryCount', { unique: false });
                }
                
                // Create metadata store if it doesn't exist
                if (!db.objectStoreNames.contains('metadata')) {
                    const metadataStore = db.createObjectStore('metadata', {
                        keyPath: 'key'
                    });
                }
            };
        });
    }

    async addBatch(batch) {
        if (!this.db) await this.init();
        
        return new Promise((resolve, reject) => {
            const transaction = this.db.transaction([this.storeName], 'readwrite');
            const store = transaction.objectStore(this.storeName);
            
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
            const transaction = this.db.transaction([this.storeName], 'readonly');
            const store = transaction.objectStore(this.storeName);
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
            const transaction = this.db.transaction([this.storeName], 'readwrite');
            const store = transaction.objectStore(this.storeName);
            
            const request = store.delete(id);
            
            request.onsuccess = () => resolve();
            request.onerror = () => reject(request.error);
        });
    }

    async updateBatchRetry(id, retryCount, nextAttemptAt) {
        if (!this.db) await this.init();
        
        return new Promise((resolve, reject) => {
            const transaction = this.db.transaction([this.storeName], 'readwrite');
            const store = transaction.objectStore(this.storeName);
            
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
            const transaction = this.db.transaction([this.storeName], 'readonly');
            const store = transaction.objectStore(this.storeName);
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
            const transaction = this.db.transaction([this.storeName], 'readonly');
            const store = transaction.objectStore(this.storeName);
            
            const request = store.count();
            
            request.onsuccess = () => resolve(request.result);
            request.onerror = () => reject(request.error);
        });
    }

    async clearAll() {
        if (!this.db) await this.init();
        
        return new Promise((resolve, reject) => {
            const transaction = this.db.transaction([this.storeName], 'readwrite');
            const store = transaction.objectStore(this.storeName);
            
            const request = store.clear();
            
            request.onsuccess = () => resolve();
            request.onerror = () => reject(request.error);
        });
    }

    async removeBatchesForWalk(walkId) {
        if (!this.db) await this.init();
        
        return new Promise((resolve, reject) => {
            const transaction = this.db.transaction([this.storeName], 'readwrite');
            const store = transaction.objectStore(this.storeName);
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

    // Metadata storage methods
    async setMetadata(key, value) {
        if (!this.db) await this.init();
        
        return new Promise((resolve, reject) => {
            const transaction = this.db.transaction(['metadata'], 'readwrite');
            const store = transaction.objectStore('metadata');
            
            const request = store.put({ key, value });
            
            request.onsuccess = () => resolve();
            request.onerror = () => reject(request.error);
        });
    }

    async getMetadata(key) {
        if (!this.db) await this.init();
        
        return new Promise((resolve, reject) => {
            const transaction = this.db.transaction(['metadata'], 'readonly');
            const store = transaction.objectStore('metadata');
            
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
            const transaction = this.db.transaction(['metadata'], 'readwrite');
            const store = transaction.objectStore('metadata');
            
            const request = store.delete(key);
            
            request.onsuccess = () => resolve();
            request.onerror = () => reject(request.error);
        });
    }

    // App state persistence methods
    async saveCurrentWalk(walkData) {
        await this.setMetadata('currentWalkId', walkData.walkId);
        await this.setMetadata('walkPetId', walkData.petId);
        await this.setMetadata('walkStartedAt', walkData.startedAt);
        await this.setMetadata('isRecording', walkData.isRecording);
    }

    async getCurrentWalk() {
        const walkId = await this.getMetadata('currentWalkId');
        const petId = await this.getMetadata('walkPetId');
        const startedAt = await this.getMetadata('walkStartedAt');
        const isRecording = await this.getMetadata('isRecording');
        
        if (walkId && startedAt && isRecording) {
            return {
                walkId: parseInt(walkId),
                petId: petId ? parseInt(petId) : null,
                startedAt: new Date(startedAt),
                isRecording: Boolean(isRecording)
            };
        }
        return null;
    }

    async clearCurrentWalk() {
        await this.removeMetadata('currentWalkId');
        await this.removeMetadata('walkPetId');
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

    async deleteDatabase() {
        return new Promise((resolve, reject) => {
            if (this.db) {
                this.db.close();
                this.db = null;
            }
            
            const request = indexedDB.deleteDatabase(this.dbName);
            
            request.onerror = () => reject(request.error);
            request.onsuccess = () => {
                console.log('Database deleted successfully');
                resolve();
            };
        });
    }
}

// Export for use in other modules
window.WalkQueueDB = WalkQueueDB;
