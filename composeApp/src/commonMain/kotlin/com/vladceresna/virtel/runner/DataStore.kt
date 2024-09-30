package com.vladceresna.virtel.runner

data object DataStore {
    var data:Map<DataType,List<Any>> = emptyMap()
}
enum class DataType{
    VAR,VIEW,SERVER,CLIENT
}