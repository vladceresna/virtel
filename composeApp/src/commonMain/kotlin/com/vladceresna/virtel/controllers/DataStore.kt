package com.vladceresna.virtel.controllers

import com.vladceresna.virtel.lib.MutexHashSet


class DataStore(
    var program: Program,
) {
    var data: MutexHashSet<Data> = MutexHashSet()

    fun get(name: String, type: DataType): Data? {
        return data.find {
            it.type == type && it.name == name
        }
    }
    fun unwrap(varName:String):String{
        return if (varName.startsWith("\"") && varName.endsWith("\""))
            varName.substring(1,varName.length-1)
        else varName
    }
    fun getVar(name:String, type: DataType):Any? {
        return get(unwrap(name), type)?.value
    }
    fun putVar(name: String, type: DataType, value: Any){
        var newData = Data(type,name,value)
        data.put(newData)
    }
}


data class Data(
    var type: DataType,
    var name: String, // appId.name as com.vladceresna.launcher.name
    var value: Any
)

enum class DataType{
    REF,VAR,LIST,VIEW,SERVER
}