package com.vladceresna.virtel.controllers

import com.vladceresna.virtel.getHttpClient
import com.vladceresna.virtel.other.VirtelException
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpMethod
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.response.respondText
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okio.IOException
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
    /** var set (value) (newVarName)
     * */
    fun varSet(args: MutableList<String>){
        var value = DataStore.tryGet(appId, DataType.VAR, args.get(0)).value
        DataStore.put(appId,DataType.VAR, args.get(1), value)
    }
    /** var get (newVarName)
     * */
    fun varGet(args: MutableList<String>): Data{
        return DataStore.tryGet(appId,DataType.VAR, args.get(0))
    }
    /** var del (newVarName)
     * */
    fun varDel(args: MutableList<String>) {
        DataStore.data.remove(DataStore.tryGet(appId, DataType.VAR, args.get(0)))
    }
    /** lst set (lstName) (index) (value)
     * */
    fun lstSet(args: MutableList<String>){
        var data:Data
        try{
            data = DataStore.find(appId,DataType.LIST,args.get(0))
        } catch (e:VirtelException){
            DataStore.put(appId,DataType.LIST,args.get(0), mutableListOf<Any>())
            data = DataStore.find(appId,DataType.LIST,args.get(0))
        }
        var list = data.value as MutableList<Any>
        var index = DataStore.tryGet(appId, DataType.VAR, args.get(1)).value.toString().toInt()
        var value = DataStore.tryGet(appId, DataType.VAR, args.get(2)).value.toString()
        list.add(index, value)
        DataStore.put(appId,DataType.LIST, args.get(0), list)
    }
    /** lst get (lstName) (index) (newVarName)
     * */
    fun lstGet(args: MutableList<String>) {
        var list = DataStore.tryGet(appId,DataType.LIST,args.get(0)).value as MutableList<Any>
        var index = DataStore.tryGet(appId, DataType.VAR, args.get(1)).value.toString().toInt()
        DataStore.put(appId,DataType.VAR, args.get(2), list.get(index))
    }
    /** lst del (lstName) (index)
     * */
    fun lstDel(args: MutableList<String>) {
        var list = DataStore.tryGet(appId,DataType.LIST,args.get(0)).value as MutableList<Any>
        var index = DataStore.tryGet(appId, DataType.VAR, args.get(1)).value.toString().toInt()
        DataStore.data.remove(Data(appId,DataType.LIST,args.get(0),list))
        list.remove(list.get(index))
        DataStore.put(appId,DataType.LIST, args.get(0), list)
    }
    /** lst len (lstName) (newVarName)
     * */
    fun lstLen(args: MutableList<String>) {
        var list = DataStore.tryGet(appId,DataType.LIST,args.get(0)).value as MutableList<Any>
        if (list.isEmpty()){
            DataStore.put(appId, DataType.VAR, args.get(1), 0)
        } else {
            DataStore.put(appId, DataType.VAR, args.get(1), list.lastIndex + 1)
        }
    }

    /** str cut (first) (last) (value) (newVarName)
     * */
    fun strCut(args: MutableList<String>){
        var first = DataStore.tryGet(appId, DataType.VAR, args.get(0)).value.toString().toInt()
        var second = DataStore.tryGet(appId, DataType.VAR, args.get(1)).value.toString().toInt()
        var value = DataStore.tryGet(appId, DataType.VAR, args.get(2)).value.toString()
        DataStore.put(appId,DataType.VAR, args.get(3), value.substring(first,second))
    }
    /** str add (firstStr) (secondStr) (newVarName)
     * */
    fun strAdd(args: MutableList<String>){
        var firstStr = DataStore.tryGet(appId, DataType.VAR, args.get(0)).value.toString()
        var secondStr = DataStore.tryGet(appId, DataType.VAR, args.get(1)).value.toString()
        DataStore.put(appId,DataType.VAR, args.get(2), firstStr+secondStr)
    }
    /** str len (value) (newVarName)
     * */
    fun strLen(args: MutableList<String>){
        var value = DataStore.tryGet(appId, DataType.VAR, args.get(0)).value.toString().length.toString()
        DataStore.put(appId,DataType.VAR, args.get(1), value)
    }
    /** str get (index) (value) (newVarName)
     * */
    fun strGet(args: MutableList<String>){
        var index = DataStore.tryGet(appId, DataType.VAR, args.get(0)).value.toString().toInt()
        var value = DataStore.tryGet(appId, DataType.VAR, args.get(1)).value.toString()
        DataStore.put(appId,DataType.VAR, args.get(2), value.get(index).toString())
    }
    /** str eqs (first) (second) (newVarName)
     * */
    fun strEqs(args: MutableList<String>){
        var firstStr = DataStore.tryGet(appId, DataType.VAR, args.get(0)).value.toString()
        var secondStr = DataStore.tryGet(appId, DataType.VAR, args.get(1)).value.toString()
        DataStore.put(appId,DataType.VAR, args.get(2), firstStr.equals(secondStr).toString())
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
    /** mat eqs (a) (b) (newVarName)
     * */
    fun matEqs(args: MutableList<String>) {
        var a = DataStore.tryGet(appId, DataType.VAR, args.get(0)).value.toString().toDouble()
        var b = DataStore.tryGet(appId, DataType.VAR, args.get(1)).value.toString().toDouble()
        DataStore.put(appId,DataType.VAR, args.get(2), a.equals(b).toString())
    }
    /** mat grtr (a) (b) (newVarName)
     * */
    fun matGrtr(args: MutableList<String>) {
        var a = DataStore.tryGet(appId, DataType.VAR, args.get(0)).value.toString().toDouble()
        var b = DataStore.tryGet(appId, DataType.VAR, args.get(1)).value.toString().toDouble()
        DataStore.put(appId,DataType.VAR, args.get(2), (a>b).toString())
    }
    /** mat less (a) (b) (newVarName)
     * */
    fun matLess(args: MutableList<String>) {
        var a = DataStore.tryGet(appId, DataType.VAR, args.get(0)).value.toString().toDouble()
        var b = DataStore.tryGet(appId, DataType.VAR, args.get(1)).value.toString().toDouble()
        DataStore.put(appId,DataType.VAR, args.get(2), (a<b).toString())
    }


    /** bln and (a) (b) (newVarName)
     * */
    fun blnAnd(args: MutableList<String>) {
        var a = DataStore.tryGet(appId, DataType.VAR, args.get(0)).value.toString().toBoolean()
        var b = DataStore.tryGet(appId, DataType.VAR, args.get(1)).value.toString().toBoolean()
        DataStore.put(appId,DataType.VAR, args.get(2), a.and(b).toString())
    }
    /** bln or (a) (b) (newVarName)
     * */
    fun blnOr(args: MutableList<String>) {
        var a = DataStore.tryGet(appId, DataType.VAR, args.get(0)).value.toString().toBoolean()
        var b = DataStore.tryGet(appId, DataType.VAR, args.get(1)).value.toString().toBoolean()
        DataStore.put(appId,DataType.VAR, args.get(2), a.or(b).toString())
    }
    /** bln xor (a) (b) (newVarName)
     * */
    fun blnXor(args: MutableList<String>) {
        var a = DataStore.tryGet(appId, DataType.VAR, args.get(0)).value.toString().toBoolean()
        var b = DataStore.tryGet(appId, DataType.VAR, args.get(1)).value.toString().toBoolean()
        DataStore.put(appId,DataType.VAR, args.get(2), a.xor(b).toString())
    }
    /** bln not (a) (newVarName)
     * */
    fun blnNot(args: MutableList<String>) {
        var a = DataStore.tryGet(appId, DataType.VAR, args.get(0)).value.toString().toBoolean()
        DataStore.put(appId,DataType.VAR, args.get(1), a.not().toString())
    }

    /** fls read (path) (newVarName)
     * */
    fun flsRead(args: MutableList<String>){
        var path = DataStore.tryGet(appId, DataType.VAR, args.get(0)).value.toString()
            .replace("$",FileSystem.systemPath)//$/path and pa/th
        var content = (okio.FileSystem.SYSTEM.read(path.toPath()){readUtf8()}).toString()
        DataStore.put(appId,DataType.VAR, args.get(1), content)
    }
    /** fls write (path) (value)
     * */
    fun flsWrite(args: MutableList<String>){
        var path = DataStore.tryGet(appId, DataType.VAR, args.get(0)).value.toString()
            .replace("$",FileSystem.systemPath)//$/path and pa/th
        var value = DataStore.tryGet(appId, DataType.VAR, args.get(1)).value.toString()
        okio.FileSystem.SYSTEM.write(path.toPath()){writeUtf8(value)}
    }
    /** fls del (path)
     * */
    fun flsDel(args: MutableList<String>){
        var path = DataStore.tryGet(appId, DataType.VAR, args.get(0)).value.toString()
            .replace("$",FileSystem.systemPath)//$/path and pa/th
        try {
            okio.FileSystem.SYSTEM.deleteRecursively(path.toPath())
        } catch(e:IOException){
            runBlocking {
                delay(10)
                okio.FileSystem.SYSTEM.deleteRecursively(path.toPath())
            }
        }
    }
    /** fls dirs (path)
     * */
    fun flsDir(args: MutableList<String>){
        var path = DataStore.tryGet(appId, DataType.VAR, args.get(0)).value.toString()
            .replace("$",FileSystem.systemPath)//$/path and pa/th
        okio.FileSystem.SYSTEM.createDirectories(path.toPath())
    }
    /** fls list (path) (newVarName)
     * */
    fun flsList(args: MutableList<String>){
        var path = DataStore.tryGet(appId, DataType.VAR, args.get(0)).value.toString()
            .replace("$",FileSystem.systemPath)//$/path and pa/th
        DataStore.put(appId, DataType.VAR,args.get(1),okio.FileSystem.SYSTEM.list(path.toPath()).toString())
    }
    /** fls xst (path) (newVarName)
     * */
    fun flsXst(args: MutableList<String>){
        var path = DataStore.tryGet(appId, DataType.VAR, args.get(0)).value.toString()
            .replace("$",FileSystem.systemPath)//$/path and pa/th
        DataStore.put(appId, DataType.VAR,args.get(1),okio.FileSystem.SYSTEM.exists(path.toPath()))
    }





    /** scr new (viewType) (newVarName)
     * */
    fun scrNew(args: MutableList<String>) {
        var cleartype = DataStore.tryGet(appId, DataType.VAR, args.get(0)).value.toString()
        var widgetType = when(cleartype){
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
        var property = DataStore.tryGet(appId, DataType.VAR, args.get(1)).value.toString()
        var value = DataStore.tryGet(appId, DataType.VAR, args.get(0)).value.toString()
        /*try {

        } catch (e:Exception){ }*/
        var widget = DataStore.tryGet(appId, DataType.VIEW, args.get(2)).value as WidgetModel
        println("$value,$property,$widget")
        when(property){
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
            "weight" -> widget.weight = value.toFloat()
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
            "child" -> widget.childs.add(value)
            "root" -> ScreenModel.root = when(value){
                "true" -> args.get(2)
                "false" -> ""
                else -> throw VirtelException()
            }
        }
        DataStore.data.remove(DataStore.tryGet(appId, DataType.VIEW, args.get(2)))
        DataStore.put(appId,DataType.VIEW,args.get(2),widget)
    }
    /** scr nav (file) (navName)
     * */
    fun scrNav(args: MutableList<String>) {
        var file = DataStore.tryGet(appId, DataType.VAR, args.get(0)).value.toString()
        var navName = DataStore.tryGet(appId, DataType.VAR, args.get(1)).value.toString()
        when(navName){
            "settings" -> ScreenModel.settingsClick = { CoroutineScope(Job()).launch { runFile(file) }}
            "home" -> ScreenModel.homeClick = { CoroutineScope(Job()).launch { runFile(file) }}
            "back" -> ScreenModel.backClick = { CoroutineScope(Job()).launch { runFile(file) }}
            else -> throw VirtelException()
        }
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
    /** srv new (port) (newVarName)
     * */
    fun srvNew(args: MutableList<String>){
        var port = DataStore.tryGet(appId,DataType.VAR,args.get(0)).value.toString()
        DataStore.put(appId,DataType.SERVER,args.get(1),EmbeddedServer(port))
    }
    /** srv add (method) (route) (file) (resVarName) (newVarName)
     * */
    fun srvAdd(args: MutableList<String>){
        var method = DataStore.tryGet(appId,DataType.VAR,args.get(0)).value.toString()
        var route = DataStore.tryGet(appId,DataType.VAR,args.get(1)).value.toString()
        var file = DataStore.tryGet(appId,DataType.VAR,args.get(2)).value.toString()
        var server = DataStore.tryGet(appId,DataType.SERVER,args.get(4)).value as EmbeddedServer
        server.routes.add(Route(route, method, file,args.get(3)))
        DataStore.put(appId,DataType.SERVER,args.get(4),server)
    }
    /** srv run (newVarName)
     * */
    fun srvRun(args: MutableList<String>){
        var server = DataStore.tryGet(appId,DataType.SERVER,args.get(0)).value as EmbeddedServer

        embeddedServer(CIO, server.port.toInt()) {
            routing {
                server.routes.forEach {
                    when(it.method){
                        "get" -> get(it.route) {
                            runFile(it.file)
                            var response = varGet(mutableListOf(it.resVar)).value.toString()
                            call.respondText {
                                response
                            }
                        }
                        "post" -> post(it.route) {
                            runFile(it.file)
                            var response = varGet(mutableListOf(it.resVar)).value.toString()
                            call.respondText {
                                response
                            }
                        }
                        "delete" -> delete(it.route) {
                            runFile(it.file)
                            var response = varGet(mutableListOf(it.resVar)).value.toString()
                            call.respondText {
                                response
                            }
                        }
                    }
                }
            }
        }.start()
    }

    /** clt req (method) (url) (body) (newVarName) (newVarName)
     * */
    fun cltReq(args: MutableList<String>){
        var method = DataStore.tryGet(appId,DataType.VAR,args.get(0)).value.toString()
        var url = DataStore.tryGet(appId,DataType.VAR,args.get(1)).value.toString()
        var body = DataStore.tryGet(appId,DataType.VAR,args.get(2)).value.toString()
        runBlocking {
            var response = getHttpClient().request(url) {
                when(method){
                    "post" -> HttpMethod.Post
                    "delete" -> HttpMethod.Delete
                    "put" -> HttpMethod.Put
                    else -> HttpMethod.Get
                }
                setBody(body)
            }
            DataStore.put(appId,DataType.VAR,args.get(4),response.bodyAsText())
            DataStore.put(appId,DataType.VAR,args.get(3),response.status.toString())
        }

    }




    /**parser**/
    fun runStep(step: Step){
        if(Programs.findProgram(appId).debugMode) {
            log("Executes ${step.mod} ${step.cmd} ${step.args}", Log.WARNING)
        }
        when(step.mod){
            "csl" -> when(step.cmd){
                "write" -> cslWrite(step.args)
                "read" -> cslRead(step.args)
            }
            "var" -> when(step.cmd){
                "set" -> varSet(step.args)
                "get" -> varGet(step.args)
                "del" -> varDel(step.args)
            }
            "lst" -> when(step.cmd){
                "set" -> lstSet(step.args)
                "get" -> lstGet(step.args)
                "del" -> lstDel(step.args)
                "len" -> lstLen(step.args)
            }
            "str" -> when(step.cmd){
                "cut" -> strCut(step.args)
                "add" -> strAdd(step.args)
                "len" -> strLen(step.args)
                "get" -> strGet(step.args)
                "eqs" -> strEqs(step.args)
            }
            "mat" -> when(step.cmd){
                "plus" -> matPlus(step.args)
                "minus" -> matMinus(step.args)
                "mult" -> matMult(step.args)
                "div" -> matDiv(step.args)
                "eqs" -> matEqs(step.args)
                "grtr" -> matGrtr(step.args)
                "less" -> matLess(step.args)
            }
            "bln" -> when(step.cmd){
                "and" -> blnAnd(step.args)
                "or"  -> blnOr(step.args)
                "xor" -> blnXor(step.args)
                "not" -> blnNot(step.args)
            }
            "fls" -> when(step.cmd){
                "read" -> flsRead(step.args)
                "write" -> flsWrite(step.args)
                "del" -> flsDel(step.args)
                "dir" -> flsDir(step.args)
                "list" -> flsList(step.args)
                "xst" -> flsXst(step.args)
            }
            "scr" -> {
                when (step.cmd) {
                    "new" -> scrNew(step.args)
                    "del" -> scrDel(step.args)
                    "set" -> scrSet(step.args)
                    "nav" -> scrNav(step.args)
                }
                VirtelSystem.renderFunction()
            }
            "run" -> when(step.cmd){
                "one" -> runOne(step.args)
                "if" -> runIf(step.args)
                "while" -> runWhile(step.args)
                "flow" -> runFlow(step.args)
            }
            "srv" -> when(step.cmd){
                "new" -> srvNew(step.args)
                "add" -> srvAdd(step.args)
                "run" -> srvRun(step.args)
            }
            "clt" -> when(step.cmd){
                "req" -> cltReq(step.args)
            }
        }
    }
    /**lexer**/
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


