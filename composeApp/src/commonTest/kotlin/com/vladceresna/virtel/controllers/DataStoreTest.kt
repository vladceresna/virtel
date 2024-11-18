package com.vladceresna.virtel.controllers

import kotlin.test.Test
import kotlin.test.assertEquals

class DataStoreTest {
    @Test
    fun testFindData(){
        var store = DataStore(Program(""))
        store.putVar("var", DataType.VAR,"val")
        store.putVar("var", DataType.VAR,"value")
        assertEquals("value", store.getVar("var",DataType.VAR))
    }
}