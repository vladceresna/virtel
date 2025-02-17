package com.vladceresna.virtel.lib

import androidx.compose.runtime.mutableStateListOf
import kotlinx.coroutines.sync.Mutex

/**
 * HashSet for Multithreading
 * {}<->{}<->{}
 * **/
class MutexHashSet<T> {
    var items = mutableStateListOf<T>()
    
    fun put(value:T,equals: (T,T) -> Boolean):T{
        var i = -1
        items.forEachIndexed { index, t -> if(equals(t,value)){ i = index} }
        if(i != -1) items.set(i,value)
        else items.add(value)
        return value
    }
    fun get(index:Int):T?{
        var item = items.get(index)
        return item
    }
    fun find(predicate: (T) -> Boolean):T?{
        var item = items.find(predicate)
        return item
    }
    fun remove(index:Int){
        items.removeAt(index)
    }
}