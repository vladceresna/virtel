package com.vladceresna.virtel.lib

/**
 * FIFO Queue for Multithreading
 * null<-{}<-{}<-{lastItem}
 * **/
class MutexQueue<T> {
    var isLocked = false
    var lastItem: Item<T>? = null
    fun push(value:T){
        while(isLocked){}
        isLocked = true
        lastItem = Item(value,lastItem)
        isLocked = false
    }
    fun take():T?{
        while(isLocked){}
        isLocked = true
        var currentItem:Item<T> = lastItem ?: return null
        var newFirstItem:Item<T>? = null
        while(currentItem.parent != null){
            newFirstItem = currentItem
            currentItem = currentItem.parent!!
        }
        if(newFirstItem == null) lastItem = null
        else newFirstItem.parent = null
        isLocked = false
        return currentItem.value
    }
}
data class Item<T>(
    var value:T,
    var parent:Item<T>?
)