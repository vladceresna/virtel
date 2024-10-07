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
        flow.varSet(mutableListOf("\"testValue\"", "\"testVar\""))
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
    fun testVar(){
        flow.varSet(mutableListOf("\"testValue\"", "\"testVar\""))
        assertEquals(flow.varGet(mutableListOf("\"testVar\"")).value.toString(), "testValue")
        flow.varDel(mutableListOf("\"testVar\""))
        init()
    }
    @Test
    fun testLst() {
        flow.lstSet(mutableListOf("\"newList\"","\"0\"","\"hello\""))
        flow.lstGet(mutableListOf("\"newList\"","\"0\"","\"newVarHello\""))
        assertEquals("hello",flow.varGet(mutableListOf("\"newVarHello\"")).value.toString())
        flow.lstLen(mutableListOf("\"newList\"","\"newListLength\""))
        assertEquals("1",flow.varGet(mutableListOf("\"newListLength\"")).value.toString())
        flow.lstDel(mutableListOf("\"newList\"","\"0\""))
        flow.lstLen(mutableListOf("\"newList\"","\"newListLength2\""))
        assertEquals("0",flow.varGet(mutableListOf("\"newListLength2\"")).value.toString())
        assertFails { flow.lstGet(mutableListOf("\"newList\"","\"0\"","\"newVarHello\"")) }
    }
    @Test
    fun testStr(){
        flow.strAdd(mutableListOf("\"first\"","\"second\"","\"fsresult\""))
        flow.strLen(mutableListOf("\"fsresult\"","\"lenresult\""))
        assertEquals("11",flow.varGet(mutableListOf("\"lenresult\"")).value.toString())
        flow.strCut(mutableListOf("\"0\"","\"5\"","\"fsresult\"","\"first\""))
        assertEquals("first", flow.varGet(mutableListOf("\"first\"")).value.toString())
        flow.strGet(mutableListOf("\"5\"","\"fsresult\"","\"five\""))
        assertEquals("s", flow.varGet(mutableListOf("\"five\"")).value.toString())
    }
    @Test
    fun testMatPlus(){
        flow.matPlus(mutableListOf("\"5\"", "\"5\"", "\"testPlusVar\""))
        assertEquals(flow.varGet(mutableListOf("\"testPlusVar\"")).value.toString(), "10.0")
    }
    @Test
    fun testMatMinus(){
        flow.matMinus(mutableListOf("\"5\"", "\"5\"", "\"testMinusVar\""))
        assertEquals(flow.varGet(mutableListOf("\"testMinusVar\"")).value.toString(), "0.0")
    }
    @Test
    fun testMatMult(){
        flow.matMult(mutableListOf("\"5\"", "\"5\"", "\"testMultVar\""))
        assertEquals(flow.varGet(mutableListOf("\"testMultVar\"")).value.toString(), "25.0")
    }
    @Test
    fun testMatDiv(){
        flow.matDiv(mutableListOf("\"5\"", "\"5\"", "\"testDivVar\""))
        assertEquals(flow.varGet(mutableListOf("\"testDivVar\"")).value.toString(), "1.0")
    }

    @Test
    fun testScr(){
        flow.scrNew(mutableListOf("\"text\"", "\"view\""))
        flow.scrSet(mutableListOf("\"true\"","\"root\"","\"view\""))
        assertEquals(ScreenModel.root,"\"view\"")
    }

    @Test
    fun testSrvNew(){
        flow.srvNew(mutableListOf("\"8080\"","\"server\""))
        assertEquals(DataStore.find("\"testAppId\"",DataType.SERVER,"\"server\""),
            Data("testAppId",DataType.SERVER,"server",EmbeddedServer("8080"),DataType.SERVER))
    }
    @Test
    fun testSrvAdd(){
        flow.srvNew(mutableListOf("\"8080\"","\"server2\""))
        flow.srvAdd(mutableListOf("\"get\"","\"/ping\"","\"/ping.steps\"","\"resVar\"","\"server2\""))//?
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