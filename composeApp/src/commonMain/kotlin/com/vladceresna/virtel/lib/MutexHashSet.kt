package com.vladceresna.virtel.lib

import io.ktor.util.Hash

/**
 * HashSet for Multithreading
 * {firstItem}<->{}<->{}<->null
 * **/
class MutexHashSet<T> {
    var isLocked = false
    var firstHashSetItem: HashSetItem<T>? = null
    
    fun push(value:T){
        while(isLocked){}
        isLocked = true

        firstHashSetItem = HashSetItem(value, value)

        isLocked = false
    }
    fun take():T?{
        while(isLocked){}
        isLocked = true

        var currentHashSetItem:HashSetItem<T> = firstHashSetItem ?: return null
        var newFirstHashSetItem:HashSetItem<T>? = null
        while(currentHashSetItem.parent != null){
            newFirstHashSetItem = currentHashSetItem
            currentHashSetItem = currentHashSetItem.parent!!
        }
        if(newFirstHashSetItem == null) firstHashSetItem = null
        else newFirstHashSetItem.parent = null

        isLocked = false
        return currentHashSetItem.value
    }
}
data class HashSetItem<T>(
    var value:T,
    var left:HashSetItem<T>?,
    var right:HashSetItem<T>?,
    var hash:String
)