package com.vladceresna.virtel.models

data object DataStore {
    var data:Map<DataType,List<Any>> = emptyMap()
}
enum class DataType{
    VAR,VIEW,SERVER,CLIENT
}