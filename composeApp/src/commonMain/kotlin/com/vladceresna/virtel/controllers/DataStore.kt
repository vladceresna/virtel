package com.vladceresna.virtel.controllers


data object DataStore {
    var data: MutableSet<Data> = mutableSetOf()

    fun find(type: DataType, name: String): Data {

    }
    fun tryGet(type: DataType, name: String): Data? {

    }
    fun put(type: DataType, name: String, value: Any){

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