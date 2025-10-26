import { openDB, DBSchema, IDBPDatabase } from 'idb'
import { WalkPointsBatchRequest } from '../api/walks'

interface QueueItem {
  id: string
  walkId: number
  batch: WalkPointsBatchRequest
  timestamp: number
  retryCount: number
}

interface PetTrailDB extends DBSchema {
  walkQueue: {
    key: string
    value: QueueItem
    indexes: { 'by-walkId': number }
  }
}

class IDBQueue {
  private db: IDBPDatabase<PetTrailDB> | null = null
  private readonly DB_NAME = 'PetTrailDB'
  private readonly DB_VERSION = 1

  async init(): Promise<void> {
    if (this.db) return

    this.db = await openDB<PetTrailDB>(this.DB_NAME, this.DB_VERSION, {
      upgrade(db) {
        const queueStore = db.createObjectStore('walkQueue', { keyPath: 'id' })
        queueStore.createIndex('by-walkId', 'walkId')
      }
    })
  }

  async enqueue(walkId: number, batch: WalkPointsBatchRequest): Promise<void> {
    await this.init()
    
    const item: QueueItem = {
      id: `${walkId}-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`,
      walkId,
      batch,
      timestamp: Date.now(),
      retryCount: 0
    }

    await this.db!.add('walkQueue', item)
  }

  async getQueuedBatches(walkId: number): Promise<QueueItem[]> {
    await this.init()
    
    const tx = this.db!.transaction('walkQueue', 'readonly')
    const index = tx.store.index('by-walkId')
    return await index.getAll(walkId)
  }

  async removeBatch(id: string): Promise<void> {
    await this.init()
    await this.db!.delete('walkQueue', id)
  }

  async incrementRetryCount(id: string): Promise<void> {
    await this.init()
    
    const tx = this.db!.transaction('walkQueue', 'readwrite')
    const item = await tx.store.get(id)
    
    if (item) {
      item.retryCount += 1
      await tx.store.put(item)
    }
  }

  async getStats(walkId: number): Promise<{ count: number; oldestTimestamp: number | null }> {
    await this.init()
    
    const batches = await this.getQueuedBatches(walkId)
    const oldestTimestamp = batches.length > 0 
      ? Math.min(...batches.map(b => b.timestamp))
      : null

    return {
      count: batches.length,
      oldestTimestamp
    }
  }

  async drain(walkId: number, sender: (batch: WalkPointsBatchRequest) => Promise<boolean>): Promise<{
    sent: number
    failed: number
    remaining: number
  }> {
    await this.init()
    
    const batches = await this.getQueuedBatches(walkId)
    let sent = 0
    let failed = 0

    for (const item of batches) {
      try {
        const success = await sender(item.batch)
        
        if (success) {
          await this.removeBatch(item.id)
          sent++
        } else {
          await this.incrementRetryCount(item.id)
          failed++
        }
      } catch (error) {
        console.error('Failed to send queued batch:', error)
        await this.incrementRetryCount(item.id)
        failed++
      }
    }

    const remaining = (await this.getQueuedBatches(walkId)).length

    return { sent, failed, remaining }
  }

  async clearWalk(walkId: number): Promise<void> {
    await this.init()
    
    const batches = await this.getQueuedBatches(walkId)
    const tx = this.db!.transaction('walkQueue', 'readwrite')
    
    for (const item of batches) {
      await tx.store.delete(item.id)
    }
  }
}

export const idbQueue = new IDBQueue()
