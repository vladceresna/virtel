package com.vladceresna.virtel.runner

import com.vladceresna.virtel.controllers.Data
import com.vladceresna.virtel.controllers.DataStore
import com.vladceresna.virtel.controllers.DataType
import com.vladceresna.virtel.controllers.FileSystem
import com.vladceresna.virtel.controllers.Log
import com.vladceresna.virtel.controllers.Programs
import com.vladceresna.virtel.controllers.VirtelSystem
import com.vladceresna.virtel.controllers.WidgetModel
import com.vladceresna.virtel.controllers.WidgetType
import com.vladceresna.virtel.controllers.log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okio.Path.Companion.toPath
import okio.SYSTEM


/**
 * In exectime:
 * - value/var (or variable with it): tryGet: any name
 * - varName (var need to write/save in): clear using: newVarName
 * **/
class Flow (
    var appId: String,
    var flowName: String
) {
    /** csl write (text)
     * */
    fun cslWrite(args: MutableList<String>){
        var value = DataStore.tryGet(appId, DataType.VAR, args.get(0)).value
        log(value.toString(),Log.PROGRAM)
    }
    /** csl read (newVarName)
     * */
    fun cslRead(args: MutableList<String>){
        DataStore.put(appId, DataType.VAR, args.get(0), readln()!!)
    }
    /** stg put (value) (newVarName)
     * */
    fun stgPut(args: MutableList<String>){
        var value = DataStore.tryGet(appId, DataType.VAR, args.get(0)).value
        DataStore.put(appId,DataType.VAR, args.get(1), value)
    }
    /** stg get (newVarName)
     * */
    fun stgGet(args: MutableList<String>): Data{
        return DataStore.tryGet(appId,DataType.VAR, args.get(0))
    }
    /** stg del (newVarName)
     * */
    fun stgDel(args: MutableList<String>) {
        DataStore.data.remove(DataStore.tryGet(appId, DataType.VAR, args.get(0)))
    }
    /** mat plus (a) (b) (newVarName)
     * */
    fun matPlus(args: MutableList<String>) {
        var a = DataStore.tryGet(appId, DataType.VAR, args.get(0)).value.toString().toDouble()
        var b = DataStore.tryGet(appId, DataType.VAR, args.get(1)).value.toString().toDouble()
        DataStore.put(appId,DataType.VAR, args.get(2), a+b)
    }
    /** mat minus (a) (b) (newVarName)
     * */
    fun matMinus(args: MutableList<String>) {
        var a = DataStore.tryGet(appId, DataType.VAR, args.get(0)).value.toString().toDouble()
        var b = DataStore.tryGet(appId, DataType.VAR, args.get(1)).value.toString().toDouble()
        DataStore.put(appId,DataType.VAR, args.get(2), a-b)
    }
    /** mat mult (a) (b) (newVarName)
     * */
    fun matMult(args: MutableList<String>) {
        var a = DataStore.tryGet(appId, DataType.VAR, args.get(0)).value.toString().toDouble()
        var b = DataStore.tryGet(appId, DataType.VAR, args.get(1)).value.toString().toDouble()
        DataStore.put(appId,DataType.VAR, args.get(2), a*b)
    }
    /** mat div (a) (b) (newVarName)
     * */
    fun matDiv(args: MutableList<String>) {
        var a = DataStore.tryGet(appId, DataType.VAR, args.get(0)).value.toString().toDouble()
        var b = DataStore.tryGet(appId, DataType.VAR, args.get(1)).value.toString().toDouble()
        DataStore.put(appId,DataType.VAR, args.get(2), a/b)
    }

    /** scr new (viewType) (newVarName)
     * */
    fun scrNew(args: MutableList<String>) {
        var widgetType = when(args.get(0)){
            "view" -> WidgetType.VIEW
            "text" -> WidgetType.TEXT
            "button" -> WidgetType.BUTTON
            "input" -> WidgetType.INPUT
            "column" -> WidgetType.COLUMN
            "row" -> WidgetType.ROW
            "topBar" -> WidgetType.TOP_BAR
            "bottomBar" -> WidgetType.BOTTOM_BAR
            else -> WidgetType.VIEW
        }
        DataStore.put(appId,DataType.VIEW, args.get(1), WidgetModel(widgetType = widgetType))
    }
    /** scr del (newVarName)
     * */
    fun scrDel(args: MutableList<String>) {
        DataStore.data.remove(DataStore.tryGet(appId, DataType.VIEW, args.get(0)))
    }

    /** scr set (value) (property) (newVarName)
     * */
    fun scrSet(args: MutableList<String>) {
        var value = ""
        try {
            value = DataStore.tryGet(appId, DataType.VAR, args.get(0)).value.toString()
        } catch (e:Exception){ }
        var widget = DataStore.tryGet(appId, DataType.VIEW, args.get(2)).value as WidgetModel
        when(args.get(1)){
            "width" -> widget.width = when(value){
                "wrap" -> -1
                "fill" -> -2
                else -> value.toInt()
            }
            "height" -> widget.height = when(value){
                "wrap" -> -1
                "fill" -> -2
                else -> value.toInt()
            }
            "variant" -> widget.variant = when(value){
                "primary" -> 0
                "secondary" -> 1
                "tertiary" -> 2
                "destructive" -> 3
                else -> value.toInt()
            }
            "title" -> widget.title = value
            "value" -> widget.value = value
            "onclick" -> widget.onclick = value
            "child" -> widget.childs.add(args.get(0))
        }
        scrDel(mutableListOf(args.get(2)))
        DataStore.put(appId,DataType.VIEW,args.get(2),widget)
    }

    /** run one (file)
     * */
    fun runOne(args: MutableList<String>){
        var file = DataStore.tryGet(appId,DataType.VAR, args.get(0)).value.toString()
        runFile(file)
    }
    /** run if (boolValue) (file)
     * */
    fun runIf(args: MutableList<String>){
        var expr = DataStore.tryGet(appId,DataType.VAR, args.get(0)).value.toString()
        var file = DataStore.tryGet(appId,DataType.VAR, args.get(1)).value.toString()
        if(expr.equals("true")) { runFile(file) }
    }
    /** run while (boolValue) (file)
     * */
    fun runWhile(args: MutableList<String>){
        var expr = DataStore.tryGet(appId,DataType.VAR, args.get(0)).value.toString()
        var file = DataStore.tryGet(appId,DataType.VAR, args.get(1)).value.toString()
        while(expr.equals("true")) {
            runFile(file)
            expr = DataStore.tryGet(appId,DataType.VAR, args.get(0)).value.toString()
        }
    }
    /** run flow (file)
     * */
    fun runFlow(args: MutableList<String>){
        CoroutineScope(Job()).launch { runOne(args) }
    }






    fun runStep(step: Step){
        if(Programs.findProgram(appId).debugMode) {
            log("Executes ${step.mod} ${step.cmd} ${step.args}", Log.WARNING)
        }
        when(step.mod){
            "csl" -> when(step.cmd){
                "write" -> cslWrite(step.args)
                "read" -> cslRead(step.args)
            }
            "stg" -> when(step.cmd){
                "put" -> stgPut(step.args)
                "get" -> stgGet(step.args)
                "del" -> stgDel(step.args)
            }
            "mat" -> when(step.cmd){
                "plus" -> matPlus(step.args)
                "minus" -> matMinus(step.args)
                "mult" -> matMult(step.args)
                "div" -> matDiv(step.args)
            }
            "scr" -> {
                when (step.cmd) {
                    "new" -> scrNew(step.args)
                    "del" -> scrDel(step.args)
                    "set" -> scrSet(step.args)
                }
                VirtelSystem.renderFunction()
            }
            "run" -> when(step.cmd){
                "one" -> runOne(step.args)
                "if" -> runIf(step.args)
                "while" -> runWhile(step.args)
                "flow" -> runFlow(step.args)
            }
        }
    }

    fun runFile(fileName:String){
        val code = okio.FileSystem.SYSTEM.read(
            "${FileSystem.programsPath}/${appId}${FileSystem.srCode}$fileName".toPath())
        { readUtf8() }

        try {
            var lineNumber = 0
            var lastString = ""
            var step = Step(true)
            var word: Byte = 0
            var inValue = false
            var displayer = false
            //lexing
            for (i in code.indices) {
                val c = code[i]
                when (c) {
                    ' ' -> {
                        if (inValue) {
                            lastString += ' '
                        } else {
                            when (word) {
                                0.toByte() -> step.mod = lastString.trim()
                                1.toByte() -> step.cmd = lastString
                                else -> step.args.add(lastString)
                            }
                            lastString = ""
                            word++
                        }
                        if (displayer) displayer = false
                    }
                    '\\' -> {
                        displayer = !displayer
                        lastString += '\\'
                    }
                    '"' -> {
                        if (displayer) {
                            displayer = false
                        } else {
                            inValue = !inValue
                            lastString += '"'
                        }
                    }
                    ';' -> {
                        step.args.add(lastString)
                        try {
                            runStep(step)
                        } catch (e: Exception) {
                            println("Error on line $lineNumber: ${e.message}")
                            e.printStackTrace()
                            break
                        }
                        lineNumber++
                        lastString = ""
                        step = Step(true)
                        word = 0
                        inValue = false
                        if (displayer) displayer = false
                    }
                    else -> {
                        lastString += c
                        if (displayer) displayer = false
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

data class Step(
    var works:Boolean
) {
    lateinit var mod: String
    lateinit var cmd: String
    var args: MutableList<String> = mutableListOf()
}


