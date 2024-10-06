package com.vladceresna.virtel.controllers

import kotlin.test.Test
import kotlin.test.assertEquals

class DataStoreTest {
    @Test
    fun testFindData(){
        var data: Data = Data("appId",DataType.VIEW,"view",WidgetModel(appId = "appId"))
        DataStore.data.add(data)

        assertEquals(data,
            DataStore.find("appId",DataType.VIEW,"view"))
    }
}