package com.vladceresna.virtel.controllers

import com.vladceresna.virtel.lib.MutexHashSet


class DataStore(
    var program: Program,
) {
    var data: MutexHashSet<Data> = MutexHashSet()

    fun get(name: String, type: DataType): Data? {
        return if(name == "u" && type == DataType.VAR) Data(DataType.VAR, name, "") // nullability in command var u
        else data.find {
            it.type == type && it.name == name
        }
    }
    fun unwrap(varName:String):String{
        return if (varName.startsWith("\"") && varName.endsWith("\""))
            varName.substring(1,varName.length-1)
        else varName
    }
    fun getVar(name:String, type: DataType):Any? {
        var unwrappedName = unwrap(name)
        return if(unwrappedName.equals(name)){
            get(unwrappedName, type)?.value
        } else unwrappedName // text
    }
    fun putVar(name: String, type: DataType, value: Any){
        var newData = Data(type,name,value)
        data.put(newData){t1,t2 -> t1.name.equals(t2.name) && t1.type.equals(t2.type) }
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