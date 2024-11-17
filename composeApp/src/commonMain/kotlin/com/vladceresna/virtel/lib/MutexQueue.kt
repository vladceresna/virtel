package com.vladceresna.virtel.lib

/**
 * FIFO Queue for Multithreading
 * null<-{}<-{}<-{lastItem}
 * **/
class MutexQueue<T> {
    var isLocked = false
    var lastQueueItem: QueueItem<T>? = null
    fun push(value:T){
        while(isLocked){}
        isLocked = true
        lastQueueItem = QueueItem(value,lastQueueItem)
        isLocked = false
    }
    fun take():T?{
        while(isLocked){}
        isLocked = true
        var currentQueueItem:QueueItem<T> = lastQueueItem ?: return null
        var newFirstQueueItem:QueueItem<T>? = null
        while(currentQueueItem.parent != null){
            newFirstQueueItem = currentQueueItem
            currentQueueItem = currentQueueItem.parent!!
        }
        if(newFirstQueueItem == null) lastQueueItem = null
        else newFirstQueueItem.parent = null
        isLocked = false
        return currentQueueItem.value
    }
}
data class QueueItem<T>(
    var value:T,
    var parent:QueueItem<T>?
)