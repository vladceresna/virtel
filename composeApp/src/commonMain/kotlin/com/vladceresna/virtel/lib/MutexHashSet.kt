package com.vladceresna.virtel.lib

import kotlinx.coroutines.sync.Mutex

/**
 * HashSet for Multithreading
 * {}<->{}<->{}
 * **/
class MutexHashSet<T> {
    var lock:Mutex = Mutex(false)
    var items = mutableListOf<T>()
    
    fun put(value:T):T{
        while(!lock.tryLock()){}

        items.remove(value)
        items.add(value)

        lock.unlock()
        return value
    }
    fun get(index:Int):T?{
        while(!lock.tryLock()){}

        var item = items.get(index)

        lock.unlock()
        return item
    }
    fun find(predicate: (T) -> Boolean):T?{
        while(!lock.tryLock()){}

        var item = items.find(predicate)

        lock.unlock()
        return item
    }
    fun remove(index:Int){
        while(!lock.tryLock()){}

        items.removeAt(index)

        lock.unlock()
    }
}