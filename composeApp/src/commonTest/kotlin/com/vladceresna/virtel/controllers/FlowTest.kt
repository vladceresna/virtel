package com.vladceresna.virtel.controllers

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class FlowTest {

    var flow = Flow("testAppId", "main")

    init {
        init()
    }
    fun init(){
        flow.stgPut(mutableListOf("\"testValue\"", "testVar"))
    }


    @Test
    fun testCslWrite(){
        assertFails { flow.cslWrite(mutableListOf("\"testValue")) }
        assertFails { flow.cslWrite(mutableListOf("testValue\"")) }
        flow.cslWrite(mutableListOf("\"testValue\""))
    }
    @Test
    fun testCslRead(){
        assertFails { flow.cslRead(mutableListOf("testVar\"")) }
        assertFails { flow.cslRead(mutableListOf("\"testVar")) }
        assertFails { flow.cslRead(mutableListOf("\"testVar\"")) }
    }
    @Test
    fun testStg(){
        flow.stgPut(mutableListOf("\"testValue\"", "testVar"))
        assertEquals(flow.stgGet(mutableListOf("testVar")).value.toString(), "testValue")
        flow.stgDel(mutableListOf("testVar"))
        assertFails { flow.stgGet(mutableListOf("testVar/")) }
        assertFails { flow.stgGet(mutableListOf("testVar")) }
        init()
    }
    @Test
    fun testMatPlus(){
        flow.matPlus(mutableListOf("\"5\"", "\"5\"", "testPlusVar"))
        assertEquals(flow.stgGet(mutableListOf("testPlusVar")).value.toString(), "10.0")
    }
    @Test
    fun testMatMinus(){
        flow.matMinus(mutableListOf("\"5\"", "\"5\"", "testMinusVar"))
        assertEquals(flow.stgGet(mutableListOf("testMinusVar")).value.toString(), "0.0")
    }
    @Test
    fun testMatMult(){
        flow.matMult(mutableListOf("\"5\"", "\"5\"", "testMultVar"))
        assertEquals(flow.stgGet(mutableListOf("testMultVar")).value.toString(), "25.0")
    }
    @Test
    fun testMatDiv(){
        flow.matDiv(mutableListOf("\"5\"", "\"5\"", "testDivVar"))
        assertEquals(flow.stgGet(mutableListOf("testDivVar")).value.toString(), "1.0")
    }



    @Test
    fun testRunStep(){
        assertFails { flow.runStep(Step(true)) }
        assertFails { flow.runStep(Step(true).also {
            it.mod = "mod"
            it.cmd = "cmd"
            it.args = mutableListOf("fie")
        }) }
    }


}