package com.vladceresna.virtel.models

data object DataStore {
    var data:MutableMap<String, MutableMap<DataType,MutableList<Any>>> = mutableMapOf()
}
enum class DataType{
    FLOW,VAR,VIEW,SERVER,CLIENT
}