package com.vladceresna.virtel.controllers

import androidx.compose.runtime.mutableStateMapOf

class DataStore(
    var flow:Flow,
) {
    var data = mutableStateMapOf<Pair<String,DataType>,Any>()
    fun get(name: String, type: DataType): Any? {
        return data[Pair(name,type)]
    }
    fun unwrap(varName:String):String{
        return if (varName.startsWith("\"") && varName.endsWith("\""))
            varName.substring(1,varName.length-1)
        else varName
    }
    fun getVar(name:String, type: DataType):Any? {
        var unwrappedName = unwrap(name)
        return if(unwrappedName.equals(name)){
            get(unwrappedName, type)
        } else unwrappedName // text
    }
    fun putVar(name: String, type: DataType, value: Any){
        data.put(Pair(name,type),value)
    }
}

enum class DataType{
    REF,VAR,LIST,VIEW,SERVER
}