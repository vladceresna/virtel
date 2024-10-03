package com.vladceresna.virtel.controllers

import com.vladceresna.virtel.other.VirtelException

data object DataStore {
    var data: MutableSet<Data> = mutableSetOf()

    fun find(scopeAppId: String, type: DataType, name: String): Data {
        return data.find {
            it.type.equals(type)
            .and(it.scopeAppId.equals(scopeAppId))
            .and(it.name.equals(name))
        } ?: throw VirtelException()
    }
    fun tryGet(scopeAppId: String, type: DataType, name: String): Data {
        var name = name.replace("\"","")
        try {
            return find(scopeAppId, type, name)
        } catch (e:VirtelException){
            return Data(scopeAppId,type,name,name)
        }
    }
    fun put(scopeAppId: String, type: DataType, name: String, value: Any){
        data.add(Data(scopeAppId,type,name,value))
    }
}

data class Data(
    var scopeAppId: String,
    var type: DataType,
    var name: String,
    var value: Any
)

enum class DataType{
    FLOW,VAR,VIEW,SERVER,CLIENT
}