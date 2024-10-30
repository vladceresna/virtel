package com.vladceresna.virtel.controllers

import com.vladceresna.virtel.other.VirtelException

data object DataStore {
    var data: MutableSet<Data> = mutableSetOf()

    fun find(scopeAppId: String, type: DataType, name: String): Data {
        return try {
            data.find {
                it.type.equals(type)
                    .and(it.scopeAppId.equals(scopeAppId))
                    .and(it.name.equals(name))
            }
        } catch (e:ConcurrentModificationException) {throw Exception()}?: throw VirtelException()
    }
    fun tryGet(scopeAppId: String, type: DataType, name: String): Data {
        //log("Data: $data \n $scopeAppId $type $name",Log.DEBUG)
        var name = name
        try {
            return (find(scopeAppId, type, name).also { try {
                it.returnType = it.type
            } catch (e:Throwable){
                e.printStackTrace()
            } })
        } catch (e:VirtelException){
            if (name.startsWith("\"").and(name.endsWith("\""))){
                name = name.replace("\"","")
                return Data(scopeAppId,type,name,name)
            }
            //throw VirtelException()
            return Data(scopeAppId,type,name,"Error")
        } catch (e:Exception){
            e.printStackTrace()
            return Data(scopeAppId,type,name,"Error")
        }
    }
    fun put(scopeAppId: String, type: DataType, name: String, value: Any){
        try {
            data.remove(find(scopeAppId, type, name))
        } catch (e: Exception){}
        data.add(Data(scopeAppId, type, name, value))
    }
}

data class Data(
    var scopeAppId: String,
    var type: DataType,
    var name: String,
    var value: Any,
    var returnType:DataType = DataType.VAR
){
    override fun equals(other: Any?): Boolean {
        var other = other as Data
        return scopeAppId.equals(other.scopeAppId)
            .and(type.equals(other.type))
            .and(name.equals(other.name))
    }
}

enum class DataType{
    REF,VAR,LIST,VIEW,SERVER
}