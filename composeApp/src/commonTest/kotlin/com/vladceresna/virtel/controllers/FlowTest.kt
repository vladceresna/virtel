package com.vladceresna.virtel.controllers

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class FlowTest {

    var flow = Flow(Program(""), "main",Step(false))

    init {
        init()
    }
    fun init(){
        flow.varSet(mutableListOf("\"testValue\"", "testVar"))
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
        flow.varSet(mutableListOf("\"testValue\"", "testVar"))
        assertEquals(flow.nGetVar("testVar",DataType.VAR).toString(), "testValue")
        flow.varDel(mutableListOf("testVar"))
        init()
    }
    @Test
    fun testLst() {
        flow.lstSet(mutableListOf("newList","\"0\"","\"hello\""))
        flow.lstGet(mutableListOf("newList","\"0\"","newVarHello"))
        assertEquals("hello",flow.nGetVar("newVarHello",DataType.VAR).toString())
        flow.lstLen(mutableListOf("newList","newListLength"))
        assertEquals("1",flow.nGetVar("newListLength",DataType.VAR).toString())
        flow.lstDel(mutableListOf("newList","\"0\""))
        flow.lstLen(mutableListOf("newList","newListLength2"))
        assertEquals("0",flow.nGetVar("newListLength2",DataType.VAR).toString())
        assertFails { flow.lstGet(mutableListOf("newList","\"0\"","newVarHello")) }
    }
    @Test
    fun testStr(){
        flow.strAdd(mutableListOf("\"first\"","\"second\"","fsresult"))
        flow.strLen(mutableListOf("fsresult","lenresult"))
        assertEquals("11",flow.nGetVar("lenresult",DataType.VAR).toString())
        flow.strCut(mutableListOf("\"0\"","\"5\"","fsresult","first"))
        assertEquals("first", flow.nGetVar("first",DataType.VAR).toString())
        flow.strGet(mutableListOf("\"5\"","fsresult","five"))
        assertEquals("s", flow.nGetVar("five",DataType.VAR).toString())
    }
    @Test
    fun testMatPlus(){
        flow.varSet(mutableListOf("\"5\"", "number"))
        flow.matPlus(mutableListOf("\"5\"", "number", "testPlusVar"))
        assertEquals(flow.nGetVar("testPlusVar",DataType.VAR).toString(), "10.0")
    }
    @Test
    fun testMatMinus(){
        flow.matMinus(mutableListOf("\"5\"", "\"5\"", "testMinusVar"))
        assertEquals(flow.nGetVar("testMinusVar",DataType.VAR).toString(), "0.0")
    }
    @Test
    fun testMatMult(){
        flow.matMult(mutableListOf("\"5\"", "\"5\"", "testMultVar"))
        assertEquals(flow.nGetVar("testMultVar",DataType.VAR).toString(), "25.0")
    }
    @Test
    fun testMatDiv(){
        flow.matDiv(mutableListOf("\"5\"", "\"5\"", "testDivVar"))
        assertEquals(flow.nGetVar("testDivVar",DataType.VAR).toString(), "1.0")
    }


    @Test
    fun testStrAdd(){
        flow.varSet(mutableListOf("\"res\"","result"))
        flow.strAdd(mutableListOf("result","\"0\"","result"))
        assertEquals("res0",flow.nGetVar("result",DataType.VAR).toString())
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