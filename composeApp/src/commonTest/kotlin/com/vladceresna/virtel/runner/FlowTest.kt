package com.vladceresna.virtel.runner

import kotlin.test.Test
import kotlin.test.assertFails

class FlowTest {

    var flow = Flow("testAppId", "main")

    init {
        flow.stgPut(mutableListOf("\"testValue\"", "testVar"))
    }
    @Test
    fun testCslWrite(){
        assertFails { flow.cslWrite(mutableListOf("\"testValue")) }
        assertFails { flow.cslWrite(mutableListOf("testValue\"")) }
    }
    @Test
    fun testCslRead(){

    }

}