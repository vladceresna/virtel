package com.vladceresna.virtel.lib

import kotlinx.coroutines.sync.Mutex

/**
 * FIFO Queue for Multithreading
 * null<-{}<-{}<-{lastItem}
 * **/
class MutexQueue<T> {
    var lock: Mutex = Mutex(false)
    var lastQueueItem: QueueItem<T>? = null
    fun push(value:T){
        while(!lock.tryLock()){}

        lastQueueItem = QueueItem(value,lastQueueItem)

        lock.unlock()
    }
    fun take():T?{
        while(!lock.tryLock()){}

        var currentQueueItem:QueueItem<T> = lastQueueItem ?: return null
        var newFirstQueueItem:QueueItem<T>? = null
        while(currentQueueItem.parent != null){
            newFirstQueueItem = currentQueueItem
            currentQueueItem = currentQueueItem.parent!!
        }
        if(newFirstQueueItem == null) lastQueueItem = null
        else newFirstQueueItem.parent = null

        lock.unlock()
        return currentQueueItem.value
    }
}
data class QueueItem<T>(
    var value:T,
    var parent:QueueItem<T>?
)