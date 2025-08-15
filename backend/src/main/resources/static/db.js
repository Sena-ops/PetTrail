/**
 * IndexedDB wrapper for persistent queue storage
 * Stores batches keyed by auto-id with secondary index on nextAttemptAt
 */

class WalkQueueDB {
    constructor() {
        this.dbName = 'WalkQueueDB';
        this.version = 1;
        this.storeName = 'batches';
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
                const store = db.createObjectStore(this.storeName, {
                    keyPath: 'id',
                    autoIncrement: true
                });

                // Create indexes
                store.createIndex('walkId', 'walkId', { unique: false });
                store.createIndex('nextAttemptAt', 'nextAttemptAt', { unique: false });
                store.createIndex('retryCount', 'retryCount', { unique: false });
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
}

// Export for use in other modules
window.WalkQueueDB = WalkQueueDB;
