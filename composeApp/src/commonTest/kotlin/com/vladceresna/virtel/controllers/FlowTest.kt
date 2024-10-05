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
        //assertFails { flow.cslWrite(mutableListOf("\"testValue")) }
        //assertFails { flow.cslWrite(mutableListOf("testValue\"")) }
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
        init()
    }
    @Test
    fun testStr(){
        flow.strAdd(mutableListOf("\"first\"","\"second\"","fsresult"))
        flow.strLen(mutableListOf("fsresult","lenresult"))
        assertEquals("11",flow.stgGet(mutableListOf("lenresult")).value.toString())
        flow.strCut(mutableListOf("\"0\"","\"5\"","fsresult","first"))
        assertEquals("first", flow.stgGet(mutableListOf("first")).value.toString())
        flow.strGet(mutableListOf("\"5\"","fsresult","five"))
        assertEquals("s", flow.stgGet(mutableListOf("five")).value.toString())
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
    fun testScr(){
        flow.scrNew(mutableListOf("\"text\"", "view"))
        flow.scrSet(mutableListOf("\"true\"","\"root\"","view"))
        assertEquals(ScreenModel.root,"view")
    }

    @Test
    fun testSrvNew(){
        flow.srvNew(mutableListOf("\"8080\"","server"))
        assertEquals(DataStore.find("testAppId",DataType.SERVER,"server"),
            Data("testAppId",DataType.SERVER,"server",EmbeddedServer("8080"),DataType.SERVER))
    }
    @Test
    fun testSrvAdd(){
        flow.srvNew(mutableListOf("\"8080\"","server2"))
        flow.srvAdd(mutableListOf("\"get\"","\"/ping\"","\"/ping.steps\"","resVar","server2"))//?
        assertEquals(DataStore.find("testAppId",DataType.SERVER,"server2"),
            Data("testAppId",DataType.SERVER,"server2",
                EmbeddedServer("8080", mutableSetOf(
                    Route("/ping","get","/ping.steps","resVar")
                )),DataType.SERVER))
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