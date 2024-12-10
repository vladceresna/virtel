package com.vladceresna.virtel.lib

import kotlinx.coroutines.sync.Mutex

/**
 * HashSet for Multithreading
 * {}<->{}<->{}
 * **/
class MutexHashSet<T> {
    var lock:Mutex = Mutex(false)
    var items = mutableListOf<T>()
    
    fun put(value:T,equals: (T,T) -> Boolean):T{
        while(!lock.tryLock()){}

        var i = -1
        items.forEachIndexed { index, t -> if(equals(t,value)){ i = index} }
        if(i != -1) items.set(i,value)
        else items.add(value)

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