package com.vladceresna.virtel.controllers

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.unit.dp
import com.vladceresna.virtel.ai.giveAnswer
import com.vladceresna.virtel.ai.toSpeech
import com.vladceresna.virtel.controllers.VirtelSystem.labs
import com.vladceresna.virtel.getHttpClient
import com.vladceresna.virtel.other.VirtelException
import com.vladceresna.virtel.screens.model.PageModel
import com.vladceresna.virtel.screens.model.ProgramViewModel
import com.vladceresna.virtel.screens.model.WidgetModel
import com.vladceresna.virtel.screens.model.WidgetType
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
import io.ktor.util.toMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okio.IOException
import okio.Path.Companion.toPath
import okio.SYSTEM
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.roundToInt


/**
 * In exectime:
 * - value/var (or variable with it): tryGet: any name
 * - varName (var need to write/save in): clear using: newVarName, newListName
 *
 * Flow params format:
 * app.id-flow-1-varname
 * **/
class Flow(
    var program: Program,
    var flowName: String,
    var currentStep: Step
) {


    /** sys apps (newListName)
     * */
    fun sysApps(args: MutableList<String>) {
        val list = okio.FileSystem.SYSTEM.list(FileSystem.programsPath.toPath())
        var programsIds: MutableList<String> = mutableListOf()
        list.forEach { programsIds.add(it.name) }
        nPutVar(args.get(0), DataType.LIST, programsIds)
    }

    /** sys files (newVarName)
     * */
    fun sysFiles(args: MutableList<String>) {
        nPutVar(args.get(0), DataType.VAR, FileSystem.userFilesPath)
    }

    /** sys flowname (newVarName)
     * */
    fun sysFlowname(args: MutableList<String>) {
        nPutVar(args.get(0), DataType.VAR, flowName)
    }


    /** sys log (newVarName)
     * */
    fun sysLog(args: MutableList<String>) {
        nPutVar(args.get(0), DataType.VAR, Logger.logs.joinToString("\n"))
    }

    /** sys start (appId)
     * */
    fun sysStart(args: MutableList<String>) {
        var value = nGetVar(args.get(0), DataType.VAR).toString()
        Programs.startProgram(value)
    }

    /** sys homedir (newVarName)
     * */
    fun sysHomedir(args: MutableList<String>) {
        nPutVar(args.get(0), DataType.VAR, FileSystem.systemPath)
    }

    /** sys install (appId) (contentVAR)
     * */
    fun sysInstall(args: MutableList<String>) {
        var installAppId = nGetVar(args.get(0), DataType.VAR).toString()
        var contentVAR = nGetVar(args.get(1), DataType.VAR).toString()
        VarInstaller.install(installAppId, contentVAR)
    }

    /** sys pack (appId) (contentVARNewVarName)
     * */
    fun sysPack(args: MutableList<String>) {
        var packAppId = nGetVar(args.get(0), DataType.VAR).toString()
        nPutVar(args.get(1), DataType.VAR, VarInstaller.toVar(packAppId))
    }


    /** csl error (text)
     * */
    fun cslError(args: MutableList<String>) {
        var value = nGetVar(args.get(0), DataType.VAR).toString()
        log(value, Log.ERROR)
        throw VirtelException("Error: "+value)
    }

    /** csl write (text)
     * */
    fun cslWrite(args: MutableList<String>) {
        var value = nGetVar(args.get(0), DataType.VAR).toString()
        log(value, Log.PROGRAM)
    }

    /** csl read (newVarName)
     * */
    fun cslRead(args: MutableList<String>) {
        try {
            nPutVar(args.get(0), DataType.VAR, readln())
        } catch (e: Exception) {
            Logger.readLine.value = true
            Logger.afterReadLine = { readedValue ->
                nPutVar(args.get(0), DataType.VAR, readedValue)
            }
            while (Logger.readLine.value){}
        }
    }


    //references
    /** ref set (varName) (newRefName)
     * */
    fun refSet(args: MutableList<String>) {
        nPutVar(args.get(1), DataType.REF, args.get(0))
    }

    /** ref name (valueVarName) (newRefName)
     * */
    fun refName(args: MutableList<String>) {
        var value = nGetVar(args.get(0), DataType.VAR).toString()
        nPutVar(args.get(1), DataType.REF, value)
    }

    /** ref get (newRefName)
     * */
    fun refGet(args: MutableList<String>): String {
        return nGetVar(args.get(0), DataType.REF).toString()
    }

    /** ref del (newRefName)
     * */
    fun refDel(args: MutableList<String>) {
        program.store.data.items.remove(
            Data(DataType.REF, args.get(0), nGetVar(args.get(0), DataType.REF)))
    }

    /** var set (value) (newVarName)
     * */
    fun varSet(args: MutableList<String>) {
        var value = nGetVar(args.get(0), DataType.VAR)
        nPutVar(args.get(1), DataType.VAR, value)
    }

    /** var get (valueVarName) (newVarName)
     * */
    fun varGet(args: MutableList<String>) {
        var name = nGetVar(args.get(0), DataType.VAR).toString()
        var value = nGetVar(name, DataType.VAR).toString()
        nPutVar(args.get(1), DataType.VAR,value)
    }



    /** var del (newVarName)
     * */
    fun varDel(args: MutableList<String>) {
        program.store.data.items.remove(
            Data(DataType.VAR, args.get(0), nGetVar(args.get(0), DataType.VAR)))
    }

    /** lst set (lstName) (index) (value)
     * */
    fun lstSet(args: MutableList<String>) {
        val list = try {
            nGetVar(args.get(0), DataType.LIST) as MutableList<String>
        } catch (e: VirtelException) { mutableListOf<String>() }
        var index = nGetVar(args.get(1), DataType.VAR).toString().toInt()
        var value = nGetVar(args.get(2), DataType.VAR).toString()
        list.add(index, value)
        nPutVar(args.get(0), DataType.LIST, list)
    }

    /** lst get (lstName) (index) (newVarName)
     * */
    fun lstGet(args: MutableList<String>) {
        var list = nGetVar(args.get(0), DataType.LIST) as MutableList<Any>
        var index = nGetVar(args.get(1), DataType.VAR).toString().toDouble()
        nPutVar(args.get(2), DataType.VAR, list.get(index.roundToInt()))
    }

    /** lst del (lstName) (index)
     * */
    fun lstDel(args: MutableList<String>) {
        var list = nGetVar(args.get(0), DataType.LIST) as MutableList<Any>
        var index = nGetVar(args.get(1), DataType.VAR).toString().toInt()
        program.store.data.items.remove(Data(DataType.LIST,args.get(0), list))
        list.remove(list.get(index))
        nPutVar(args.get(0), DataType.LIST, list)
    }

    /** lst len (lstName) (newVarName)
     * */
    fun lstLen(args: MutableList<String>) {
        var list = nGetVar(args.get(0), DataType.LIST) as MutableList<Any>
        if (list.isEmpty()) {
            nPutVar(args.get(1), DataType.VAR, 0)
        } else {
            nPutVar(args.get(1), DataType.VAR, list.lastIndex + 1)
        }
    }

    /** lst var (lstName) (newVarName)
     * */
    fun lstVar(args: MutableList<String>) {
        var list = nGetVar(args.get(0), DataType.LIST) as MutableList<Any>
        nPutVar(args.get(1), DataType.VAR, list.toString())
    }

    /** str cut (first) (last) (value) (newVarName)
     * */
    fun strCut(args: MutableList<String>) {
        var first = nGetVar(args.get(0), DataType.VAR).toString().toDouble().roundToInt()
        var second = nGetVar(args.get(1), DataType.VAR).toString().toDouble().roundToInt()
        var value = nGetVar(args.get(2), DataType.VAR).toString()
        nPutVar(args.get(3), DataType.VAR, value.substring(first, second))
    }

    /** str add (firstStr) (secondStr) (newVarName)
     * */
    fun strAdd(args: MutableList<String>) {
        var firstStr = nGetVar(args.get(0), DataType.VAR).toString()
        var secondStr = nGetVar(args.get(1), DataType.VAR).toString()
        nPutVar(args.get(2), DataType.VAR, firstStr + secondStr)
    }

    /** str len (value) (newVarName)
     * */
    fun strLen(args: MutableList<String>) {
        var value = nGetVar(args.get(0), DataType.VAR).toString().length.toString()
        nPutVar(args.get(1), DataType.VAR, value)
    }

    /** str get (index) (value) (newVarName)
     * */
    fun strGet(args: MutableList<String>) {
        var index = nGetVar(args.get(0), DataType.VAR).toString().toInt()
        var value = nGetVar(args.get(1), DataType.VAR).toString()
        nPutVar(args.get(2), DataType.VAR, value.get(index).toString())
    }

    /** str eqs (first) (second) (newVarName)
     * */
    fun strEqs(args: MutableList<String>) {
        var firstStr = nGetVar(args.get(0), DataType.VAR).toString()
        var secondStr = nGetVar(args.get(1), DataType.VAR).toString()
        nPutVar(args.get(2), DataType.VAR, firstStr.equals(secondStr).toString())
    }

    /** str split (regex) (value) (newVarListName)
     * */
    fun strSplit(args: MutableList<String>) {
        var regexStr = nGetVar(args.get(0), DataType.VAR).toString()
        var valueStr = nGetVar(args.get(1), DataType.VAR).toString()
        nPutVar(args.get(2), DataType.LIST, valueStr.split(regexStr).toMutableList())
    }

    /** mat plus (a) (b) (newVarName)
     * */
    fun matPlus(args: MutableList<String>) {
        var a = nGetVar(args.get(0), DataType.VAR).toString().toDouble()
        var b = nGetVar(args.get(1), DataType.VAR).toString().toDouble()
        nPutVar(args.get(2), DataType.VAR, (vnative.add(a.toUInt(),b.toUInt())).toString())
    }

    /** mat minus (a) (b) (newVarName)
     * */
    fun matMinus(args: MutableList<String>) {
        var a = nGetVar(args.get(0), DataType.VAR).toString().toDouble()
        var b = nGetVar(args.get(1), DataType.VAR).toString().toDouble()
        nPutVar(args.get(2), DataType.VAR, a - b)
    }

    /** mat mult (a) (b) (newVarName)
     * */
    fun matMult(args: MutableList<String>) {
        var a = nGetVar(args.get(0), DataType.VAR).toString().toDouble()
        var b = nGetVar(args.get(1), DataType.VAR).toString().toDouble()
        nPutVar(args.get(2), DataType.VAR, a * b)
    }

    /** mat div (a) (b) (newVarName)
     * */
    fun matDiv(args: MutableList<String>) {
        var a = nGetVar(args.get(0), DataType.VAR).toString().toDouble()
        var b = nGetVar(args.get(1), DataType.VAR).toString().toDouble()
        nPutVar(args.get(2), DataType.VAR, a / b)
    }

    /** mat eqs (a) (b) (newVarName)
     * */
    fun matEqs(args: MutableList<String>) {
        var a = nGetVar(args.get(0), DataType.VAR).toString().toDouble()
        var b = nGetVar(args.get(1), DataType.VAR).toString().toDouble()
        nPutVar(args.get(2), DataType.VAR, a.equals(b).toString())
    }

    /** mat grtr (a) (b) (newVarName)
     * */
    fun matGrtr(args: MutableList<String>) {
        var a = nGetVar(args.get(0), DataType.VAR).toString().toDouble()
        var b = nGetVar(args.get(1), DataType.VAR).toString().toDouble()
        nPutVar(args.get(2), DataType.VAR, (a > b).toString())
    }

    /** mat less (a) (b) (newVarName)
     * */
    fun matLess(args: MutableList<String>) {
        var a = nGetVar(args.get(0), DataType.VAR).toString().toDouble()
        var b = nGetVar(args.get(1), DataType.VAR).toString().toDouble()
        nPutVar(args.get(2), DataType.VAR, (a < b).toString())
    }

    /** mat grtreqs (a) (b) (newVarName)
     * */
    fun matGrtrEqs(args: MutableList<String>) {
        var a = nGetVar(args.get(0), DataType.VAR).toString().toDouble()
        var b = nGetVar(args.get(1), DataType.VAR).toString().toDouble()
        nPutVar(args.get(2), DataType.VAR, (a >= b).toString())
    }

    /** mat lesseqs (a) (b) (newVarName)
     * */
    fun matLessEqs(args: MutableList<String>) {
        var a = nGetVar(args.get(0), DataType.VAR).toString().toDouble()
        var b = nGetVar(args.get(1), DataType.VAR).toString().toDouble()
        nPutVar(args.get(2), DataType.VAR, (a <= b).toString())
    }

    /** mat random (min) (max) (newVarName)
     * */
    fun matRandom(args: MutableList<String>) {
        var min = nGetVar(args.get(0), DataType.VAR).toString().toDouble()
        var max = nGetVar(args.get(1), DataType.VAR).toString().toDouble()
        nPutVar(args.get(2), DataType.VAR, (ThreadLocalRandom.current().nextDouble(min, max)).toString())
    }
    /** mat int (numberDouble) (newVarName)
     * */
    fun matInt(args: MutableList<String>) {
        var numberDouble = nGetVar(args.get(0), DataType.VAR).toString().toDouble()
        nPutVar(args.get(1), DataType.VAR, (numberDouble.toInt()).toString())
    }



    /** bln and (a) (b) (newVarName)
     * */
    fun blnAnd(args: MutableList<String>) {
        var a = nGetVar(args.get(0), DataType.VAR).toString().toBoolean()
        var b = nGetVar(args.get(1), DataType.VAR).toString().toBoolean()
        nPutVar(args.get(2), DataType.VAR, a.and(b).toString())
    }

    /** bln or (a) (b) (newVarName)
     * */
    fun blnOr(args: MutableList<String>) {
        var a = nGetVar(args.get(0), DataType.VAR).toString().toBoolean()
        var b = nGetVar(args.get(1), DataType.VAR).toString().toBoolean()
        nPutVar(args.get(2), DataType.VAR, a.or(b).toString())
    }

    /** bln xor (a) (b) (newVarName)
     * */
    fun blnXor(args: MutableList<String>) {
        var a = nGetVar(args.get(0), DataType.VAR).toString().toBoolean()
        var b = nGetVar(args.get(1), DataType.VAR).toString().toBoolean()
        nPutVar(args.get(2), DataType.VAR, a.xor(b).toString())
    }

    /** bln not (a) (newVarName)
     * */
    fun blnNot(args: MutableList<String>) {
        var a = nGetVar(args.get(0), DataType.VAR).toString().toBoolean()
        nPutVar(args.get(1), DataType.VAR, a.not().toString())
    }

    /** fls read (path) (newVarName)
     * */
    fun flsRead(args: MutableList<String>) {
        var path = nGetVar(args.get(0), DataType.VAR).toString()
            .replace("$", FileSystem.systemPath)//$/path and pa/th
        var content = (okio.FileSystem.SYSTEM.read(path.toPath()) { readUtf8() }).toString()
        nPutVar(args.get(1), DataType.VAR, content)
    }

    /** fls write (path) (value)
     * */
    fun flsWrite(args: MutableList<String>) {
        var path = nGetVar(args.get(0), DataType.VAR).toString()
            .replace("$", FileSystem.systemPath)//$/path and pa/th
        var value = nGetVar(args.get(1), DataType.VAR).toString()

        var file = path.toPath()
        if (!okio.FileSystem.SYSTEM.exists(file.parent ?: path.toPath())) {
            okio.FileSystem.SYSTEM.createDirectories(file.parent ?: path.toPath())
        }
        okio.FileSystem.SYSTEM.write(path.toPath()) { writeUtf8(value) }
    }

    /** fls del (path)
     * */
    fun flsDel(args: MutableList<String>) {
        var path = nGetVar(args.get(0), DataType.VAR).toString()
            .replace("$", FileSystem.systemPath)//$/path and pa/th
        try {
            okio.FileSystem.SYSTEM.deleteRecursively(path.toPath())
        } catch (e: IOException) {
            runBlocking {
                delay(10)
                okio.FileSystem.SYSTEM.deleteRecursively(path.toPath())
            }
        }
    }

    /** fls dir (path)
     * */
    fun flsDir(args: MutableList<String>) {
        var path = nGetVar(args.get(0), DataType.VAR).toString()
            .replace("$", FileSystem.systemPath)//$/path and pa/th
        okio.FileSystem.SYSTEM.createDirectories(path.toPath())
    }

    /** fls list (path) (newListName)
     * */
    fun flsList(args: MutableList<String>) {
        var path = nGetVar(args.get(0), DataType.VAR).toString()
            .replace("$", FileSystem.systemPath)//$/path and pa/th
        nPutVar(
            args.get(1),
            DataType.LIST,
            okio.FileSystem.SYSTEM.list(path.toPath()).toMutableList()
        )
    }

    /** fls xst (path) (newVarName)
     * */
    fun flsXst(args: MutableList<String>) {
        var path = nGetVar(args.get(0), DataType.VAR).toString()
            .replace("$", FileSystem.systemPath)//$/path and pa/th
        nPutVar(
            args.get(1),
            DataType.VAR,
            okio.FileSystem.SYSTEM.exists(path.toPath())
        )
    }


    /** scr new (newVarName) (viewType) (weight) (size) (parent)
     *      (weight) = x>0 or 0 if without
     * */
    fun scrNew(args: MutableList<String>) {
        var programModel = nGetProgramModel()
        if (programModel == null) throw VirtelException("System Program View Model error")

        var parent = programModel.widgets.find {
            it.name == args.get(4)
        }
        if(parent == null) throw VirtelException("Parent widget not found by name: "+args.get(4))

        var newModel = WidgetModel(
            programModel,
            args.get(0),
            when(nGetVar(args.get(1), DataType.VAR)){
                "view" -> {WidgetType.VIEW}
                "text" -> {WidgetType.TEXT}
                "button" -> {WidgetType.BUTTON}
                "input" -> {WidgetType.INPUT}
                "column" -> {WidgetType.COLUMN}
                "adaptive" -> {WidgetType.ADAPTIVE}
                "row" -> {WidgetType.ROW}
                "card" -> {WidgetType.CARD}
                "topBar" -> {WidgetType.TOP_BAR}
                "bottomBar" -> {WidgetType.BOTTOM_BAR}
                else -> {throw VirtelException("Variable "+args.get(1)+" contains unexpected widget type")} },
            try {
                mutableStateOf(nGetVar(args.get(2), DataType.VAR).toString().toFloat())
            }catch(e:Exception){ mutableStateOf(1F)},
            size = try {
                mutableStateOf(nGetVar(args.get(3), DataType.VAR).toString())
            }catch(e:Exception){ mutableStateOf("min/min")}
        )

        when (newModel.widgetType) {
            WidgetType.CARD -> {
                var value = 20.dp
                newModel.paddingTop.value = value
                newModel.paddingRight.value = value
                newModel.paddingBottom.value = value
                newModel.paddingLeft.value = value
            }
            WidgetType.BUTTON -> {
                newModel.variant.value = "primary"
            }
            else -> {}
        }

        parent.childs.add(newModel)
        programModel.widgets.add(newModel)
    }

    /** scr del (newVarName)
     * */
    fun scrDel(args: MutableList<String>) {
        var programModel = nGetProgramModel()
        if (programModel == null) throw VirtelException("System Program View Model error")

        var widget = programModel.widgets.find {
            it.name == args.get(0)
        }
        if(widget == null) throw VirtelException("Widget not found by name: "+args.get(0))

        programModel.widgets.remove(widget)
        widget = null

    }

    /** scr set (newVarName) (property) (value)
     * (property) = (weight) (variant) (title) (value)
     *             (foreground) (background) (onClick) (parent)
     *             (paddingTop) (paddingRight) (paddingBottom) (paddingLeft)
     *             (marginTop) (marginRight) (marginBottom) (marginLeft)
     *             (scrollable) (size=[width]x[height]) (align) (justify)
     * */
    fun scrSet(args: MutableList<String>) {
        var programModel = nGetProgramModel()
        if (programModel == null) throw VirtelException("System Program View Model error")

        var widget = programModel.widgets.find {
            it.name == args.get(0)
        }
        if(widget == null) throw VirtelException("Widget not found by name: "+args.get(0))

        var property = nGetVar(args.get(1), DataType.VAR).toString()
        var value = nGetVar(args.get(2), DataType.VAR).toString()

        when (property) {
            "weight" -> widget.weight.value = value.toFloat()
            "variant" -> widget.variant.value = value
            "title" -> widget.title.value = value
            "value" -> widget.value.value = value
            "foreground" -> widget.foreground.value = value
            "background" -> widget.background.value = value
            "onClick" -> widget.onClick.value = value
            "parent" -> (programModel.widgets.find {
                it.name == args.get(2)
            } as WidgetModel).childs.add(widget)
            "paddingTop" ->    widget.paddingTop.value = value.toInt().dp
            "paddingRight" ->  widget.paddingRight.value = value.toInt().dp
            "paddingBottom" -> widget.paddingBottom.value = value.toInt().dp
            "paddingLeft" ->   widget.paddingLeft.value = value.toInt().dp
            "padding" ->       {
                widget.paddingTop.value = value.toInt().dp
                widget.paddingRight.value = value.toInt().dp
                widget.paddingBottom.value = value.toInt().dp
                widget.paddingLeft.value = value.toInt().dp
            }
            "marginTop" ->     widget.marginTop.value = value.toInt().dp
            "marginRight" ->   widget.marginRight.value = value.toInt().dp
            "marginBottom" ->  widget.marginBottom.value = value.toInt().dp
            "marginLeft" ->    widget.marginLeft.value = value.toInt().dp
            "margin" ->        {
                widget.marginTop.value = value.toInt().dp
                widget.marginRight.value = value.toInt().dp
                widget.marginBottom.value = value.toInt().dp
                widget.marginLeft.value = value.toInt().dp
            }
            "scrollable" ->    widget.scrollable.value = value.toBoolean()
            "size" -> widget.size.value = value
            "align" -> widget.align.value = value
            "justify" -> widget.justify.value = value

        }

    }

    /** scr get (newVarName) (viewVarName) (property)
     * */
    fun scrGet(args: MutableList<String>) {
        var property = nGetVar(args.get(2), DataType.VAR).toString()

        var programModel = nGetProgramModel()
        if (programModel == null) throw VirtelException("System Program View Model error")

        var widget = programModel.widgets.find {
            it.name == args.get(1)
        }
        if(widget == null) throw VirtelException("Widget not found by name: "+args.get(1))

        nPutVar( args.get(0), DataType.VAR, when (property) {
                "weight" -> widget.weight.value.toString()
                "variant" -> widget.variant.value
                "title" -> widget.title.value
                "value" -> widget.value.value
                "foreground" -> widget.foreground.value
                "background" -> widget.background.value
                "onClick" -> widget.onClick.value
                "parent" -> (programModel.widgets.find {
                    it.name == args.get(2)
                } as WidgetModel).name
                "paddingTop" ->    widget.paddingTop.value
                "paddingRight" ->  widget.paddingRight.value
                "paddingBottom" -> widget.paddingBottom.value
                "paddingLeft" ->   widget.paddingLeft.value
                "marginTop" ->     widget.marginTop.value
                "marginRight" ->   widget.marginRight.value
                "marginBottom" ->  widget.marginBottom.value
                "marginLeft" ->    widget.marginLeft.value
                "scrollable" ->    widget.scrollable.value.toString()
                "size" ->          widget.size.value
                "align" -> widget.align.value
                "justify" -> widget.justify.value
                else -> ""
            }
        )
    }

    /** scr nav (file) (navName)
     * */
    fun scrNav(args: MutableList<String>) {
        var programModel = nGetProgramModel()
        if (programModel == null) throw VirtelException("System Program View Model error")
        var file = nGetVar(args.get(0), DataType.VAR).toString()
        var navName = nGetVar(args.get(1), DataType.VAR).toString()
        when (navName) {
            "settings" -> programModel.pageModel.settingsClick.value = {
                CoroutineScope(Job()).launch {
                    runFile(file)
                }
            }
            "home" -> programModel.pageModel.homeClick.value = {
                CoroutineScope(Job()).launch {
                    runFile(file)
                }
            }
            "back" -> programModel.pageModel.backClick.value = {
                CoroutineScope(Job()).launch {
                    runFile(file)

                }
            }
            else -> throw VirtelException("This is not a system navigation button: "+navName)
        }
    }

    /** scr page ""
     * */
    fun scrPage(args: MutableList<String>){
        VirtelSystem.screenModel.pageModels.add(PageModel())
    }
    /** scr theme (themeName)
     * (themeName) = (dark) (light)
     * */
    fun scrTheme(args: MutableList<String>){
        var theme = nGetVar(args.get(0),DataType.VAR)
        when(theme){
            "light" -> VirtelSystem.darkTheme.value = false
            "dark" -> VirtelSystem.darkTheme.value = true
            else -> throw VirtelException("This is not a theme name: "+ theme+". In arg-var expression: "+args.get(0))
        }


    }
    


    /** run one (file)
     * */
    fun runOne(args: MutableList<String>) {
        var file = nGetVar(args.get(0), DataType.VAR).toString()
        runFile(file)
    }

    /** run if (boolValue) (file)
     * */
    fun runIf(args: MutableList<String>) {
        var expr = nGetVar(args.get(0), DataType.VAR).toString()
        var file = nGetVar(args.get(1), DataType.VAR).toString()
        if (expr.equals("true")) {
            runFile(file)
        }
    }

    /** run while (boolValue) (file)
     * */
    fun runWhile(args: MutableList<String>) {
        var expr = nGetVar(args.get(0), DataType.VAR).toString()
        var file = nGetVar(args.get(1), DataType.VAR).toString()
        while (expr.equals("true")) {
            runFile(file)
            expr = nGetVar(args.get(0), DataType.VAR).toString()
        }
    }

    /** run flow (file)
     * */
    fun runFlow(args: MutableList<String>) {
        var file = nGetVar(args.get(0), DataType.VAR).toString()
        var flowName = nGetNameOfNextFlow(file)
        program.runFlow(file,flowName)

        //CoroutineScope(Job()).launch { runOne(args) }
    }

    /** run pause (time)
     * */
    fun runPause(args: MutableList<String>) {
        var time = nGetVar(args.get(0), DataType.VAR).toString().toInt()
        runBlocking { delay(time.toLong()) }
    }

    /** srv new (port) (newVarName)
     * */
    fun srvNew(args: MutableList<String>) {
        var port = nGetVar(args.get(0), DataType.VAR).toString()
        nPutVar(args.get(1), DataType.SERVER, EmbeddedServer(port))
    }

    /** srv add (method) (route) (file) (resVarName) (newVarName)
     * */
    fun srvAdd(args: MutableList<String>) {
        var method = nGetVar(args.get(0), DataType.VAR).toString()
        var route = nGetVar(args.get(1), DataType.VAR).toString()
        var file = nGetVar(args.get(2), DataType.VAR).toString()
        var server = nGetVar(args.get(4), DataType.SERVER) as EmbeddedServer
        server.routes.add(Route(route, method, file, args.get(3)))
        nPutVar(args.get(4), DataType.SERVER, server)
    }

    /** srv run (newVarName)
     * */
    fun srvRun(args: MutableList<String>) {
        var server = nGetVar(args.get(0), DataType.SERVER) as EmbeddedServer

        embeddedServer(CIO, server.port.toInt()) {
            routing {
                server.routes.forEach {
                    when (it.method) {
                        "get" -> get(it.route) {
                            var flowName = nGetNameOfNextFlow(it.file)
                            call.parameters.toMap().forEach {
                                nPutVar(program.appId+"-"+flowName // TODO: Sometimes may be Critical Bug
                                        +"-parameters-"+it.key,DataType.VAR,it.value
                                            .toString().substring(1,it.value.toString().length-1)) // TODO: Sometimes may be Critical Bug
                            }

                            nPutVar(program.appId+"-"+flowName // TODO: Sometimes may be Critical Bug
                                    +"-server",DataType.SERVER,server) // TODO: Sometimes may be Critical Bug
                            nPutVar(program.appId+"-"+flowName // TODO: Sometimes may be Critical Bug
                                    +"-route",DataType.VAR,it.route) // TODO: Sometimes may be Critical Bug
                            nPutVar(program.appId+"-"+flowName // TODO: Sometimes may be Critical Bug
                                    +"-method",DataType.VAR,it.method) // TODO: Sometimes may be Critical Bug
                            runFlow(mutableListOf("\""+it.file+"\"")) // TODO: Sometimes may be Critical Bug
                            var response = nGetVar(it.resVar,DataType.VAR).toString()
                            call.respondText {
                                response
                            }
                        }

                        "post" -> post(it.route) {
                            var flowName = nGetNameOfNextFlow(it.file)
                            call.parameters.toMap().forEach {
                                nPutVar(program.appId+"-"+flowName // TODO: Sometimes may be Critical Bug
                                        +"-parameters-"+it.key,DataType.VAR,it.value
                                    .toString().substring(1,it.value.toString().length-1)) // TODO: Sometimes may be Critical Bug
                            }

                            nPutVar(program.appId+"-"+flowName // TODO: Sometimes may be Critical Bug
                                    +"-server",DataType.SERVER,server) // TODO: Sometimes may be Critical Bug
                            nPutVar(program.appId+"-"+flowName // TODO: Sometimes may be Critical Bug
                                    +"-route",DataType.VAR,it.route) // TODO: Sometimes may be Critical Bug
                            nPutVar(program.appId+"-"+flowName // TODO: Sometimes may be Critical Bug
                                    +"-method",DataType.VAR,it.method) // TODO: Sometimes may be Critical Bug
                            runFlow(mutableListOf("\""+it.file+"\"")) // TODO: Sometimes may be Critical Bug
                            var response = nGetVar(it.resVar,DataType.VAR).toString()
                            call.respondText {
                                response
                            }
                        }

                        "delete" -> delete(it.route) {
                            var flowName = nGetNameOfNextFlow(it.file)
                            call.parameters.toMap().forEach {
                                nPutVar(program.appId+"-"+flowName // TODO: Sometimes may be Critical Bug
                                        +"-parameters-"+it.key,DataType.VAR,it.value
                                    .toString().substring(1,it.value.toString().length-1)) // TODO: Sometimes may be Critical Bug
                            }

                            nPutVar(program.appId+"-"+flowName // TODO: Sometimes may be Critical Bug
                                    +"-server",DataType.SERVER,server) // TODO: Sometimes may be Critical Bug
                            nPutVar(program.appId+"-"+flowName // TODO: Sometimes may be Critical Bug
                                    +"-route",DataType.VAR,it.route) // TODO: Sometimes may be Critical Bug
                            nPutVar(program.appId+"-"+flowName // TODO: Sometimes may be Critical Bug
                                    +"-method",DataType.VAR,it.method) // TODO: Sometimes may be Critical Bug
                            runFlow(mutableListOf("\""+it.file+"\"")) // TODO: Sometimes may be Critical Bug
                            var response = nGetVar(it.resVar,DataType.VAR).toString()
                            call.respondText {
                                response
                            }
                        }
                    }
                }
            }
        }.start()
    }

    /** clt req (method) (url) (body) (newVarNameStatus) (newVarNameBody)
     * */
    fun cltReq(args: MutableList<String>) {
        var method = nGetVar(args.get(0), DataType.VAR).toString()
        var url = nGetVar(args.get(1), DataType.VAR).toString()
        var body = nGetVar(args.get(2), DataType.VAR).toString()
        runBlocking {
            var response = getHttpClient().request(url) {
                when (method) {
                    "post" -> HttpMethod.Post
                    "delete" -> HttpMethod.Delete
                    "put" -> HttpMethod.Put
                    else -> HttpMethod.Get
                }
                setBody(body)
            }
            nPutVar(args.get(4), DataType.VAR, response.bodyAsText())
            nPutVar(args.get(3), DataType.VAR, response.status.toString())
        }

    }

    /** tts say2 (text)
     * */
    fun ttsSay2(args: MutableList<String>) {
        var text = nGetVar(args.get(0), DataType.VAR).toString()
        var ttsFile = FileSystem.userFilesPath + "/virtel/tts-cache/$text.mp3"
        if (!okio.FileSystem.SYSTEM.exists(ttsFile.toPath())) {
            toSpeech(text, labs, ttsFile)
        }
        playMP3(ttsFile)
    }

    /** tts say (text) \language\
     * */
    fun ttsSay(args: MutableList<String>) {
        var text = nGetVar(args.get(0), DataType.VAR).toString()
        var ttsFile = FileSystem.userFilesPath + "/virtel/tts-cache/$text.mp3"
        if (!okio.FileSystem.SYSTEM.exists(ttsFile.toPath())) {
            if(args.size == 2) vnative.ttsSayLang(text, ttsFile, args.get(1))
            else vnative.ttsSay(text, ttsFile)
        } else vnative.playMp3(ttsFile)
    }


    /** stt start
     * */
    fun sttStart(args: MutableList<String>) {
        CoroutineScope(Job()).launch {
            runSTT()
        }
    }

    /** stt stop
     * */
    fun sttStop(args: MutableList<String>) {
        MediaSystem.isWorks = false
    }

    /** stt wake (text) (fileName)
     * */
    fun sttWake(args: MutableList<String>) {
        var text = nGetVar(args.get(0), DataType.VAR).toString()
        var fileName = nGetVar(args.get(0), DataType.VAR).toString()
        MediaSystem.wakes.put(text, Pair(fileName, program.appId))
    }

    /** stt unwake (text)
     * */
    fun sttUnwake(args: MutableList<String>) {
        var text = nGetVar(args.get(0), DataType.VAR).toString()
        MediaSystem.wakes.remove(text)
    }

    /** stt res (newVarName)
     * */
    fun sttRes(args: MutableList<String>) {
        nPutVar(args.get(0), DataType.VAR, MediaSystem.allResult)
    }

    /** stt lastres (newVarName)
     * */
    fun sttLastres(args: MutableList<String>) {
        nPutVar(args.get(0), DataType.VAR, MediaSystem.allResult)
    }

    /** spr play (type) (path)
     * */
    fun sprPlay(args: MutableList<String>) {
        var path = nGetVar(args.get(1), DataType.VAR).toString()
            .replace("$", FileSystem.systemPath)//$/path and pa/th
        var type = nGetVar(args.get(0), DataType.VAR).toString()
        when (type) {
            "mp3" -> playMP3(path)
            "wav" -> playWAV(path)
        }
    }

    /** llm ask (text) (newResVarName)
     * */
    fun llmAsk(args: MutableList<String>) {
        var text = nGetVar(args.get(0), DataType.VAR).toString()
        nPutVar(args.get(1), DataType.VAR, giveAnswer(text))
    }


    /**parser**/
    fun runStep(step: Step):Boolean {
        try {
            if (program.debugMode) {
                log("Executes ${step.mod} ${step.cmd} ${step.args}", Log.DEBUG)
            }

            var fromRefsArgs = mutableListOf<String>()
            step.args.forEach {
                fromRefsArgs.add(
                    try {
                        program.store.get(it, DataType.REF)!!.value.toString()
                    }catch(e:Exception){
                        it
                    }
                )
            }
            step.args = fromRefsArgs

            when (step.mod) {
                "sys" -> when (step.cmd) {
                    "apps" -> sysApps(step.args)
                    "files" -> sysFiles(step.args)
                    "flowname" -> sysFlowname(step.args)
                    "start" -> sysStart(step.args)
                    "homedir" -> sysHomedir(step.args)
                    "install" -> sysInstall(step.args)
                    "pack" -> sysPack(step.args)
                    "log" -> sysLog(step.args)
                }

                "csl" -> when (step.cmd) {
                    "error" -> cslError(step.args)
                    "write" -> cslWrite(step.args)
                    "read" -> cslRead(step.args)
                }

                "ref" -> when (step.cmd) {
                    "set" -> refSet(step.args)
                    "name" -> refName(step.args)
                    "get" -> refGet(step.args)
                    "del" -> refDel(step.args)
                }

                "var" -> when (step.cmd) {
                    "set" -> varSet(step.args)
                    "get" -> varGet(step.args)
                    "del" -> varDel(step.args)
                }

                "lst" -> when (step.cmd) {
                    "set" -> lstSet(step.args)
                    "get" -> lstGet(step.args)
                    "del" -> lstDel(step.args)
                    "len" -> lstLen(step.args)
                    "var" -> lstVar(step.args)
                }

                "str" -> when (step.cmd) {
                    "cut" -> strCut(step.args)
                    "add" -> strAdd(step.args)
                    "len" -> strLen(step.args)
                    "get" -> strGet(step.args)
                    "eqs" -> strEqs(step.args)
                    "split" -> strSplit(step.args)
                }

                "mat" -> when (step.cmd) {
                    "plus" -> matPlus(step.args)
                    "minus" -> matMinus(step.args)
                    "mult" -> matMult(step.args)
                    "div" -> matDiv(step.args)
                    "eqs" -> matEqs(step.args)
                    "grtr" -> matGrtr(step.args)
                    "less" -> matLess(step.args)
                    "grtreqs" -> matGrtrEqs(step.args)
                    "lesseqs" -> matLessEqs(step.args)
                    "random" -> matRandom(step.args)
                    "int" -> matInt(step.args)
                }

                "bln" -> when (step.cmd) {
                    "and" -> blnAnd(step.args)
                    "or" -> blnOr(step.args)
                    "xor" -> blnXor(step.args)
                    "not" -> blnNot(step.args)
                }

                "fls" -> when (step.cmd) {
                    "read" -> flsRead(step.args)
                    "write" -> flsWrite(step.args)
                    "del" -> flsDel(step.args)
                    "dir" -> flsDir(step.args)
                    "list" -> flsList(step.args)
                    "xst" -> flsXst(step.args)
                }

                "scr" -> when (step.cmd) {
                    "new" -> scrNew(step.args)
                    "del" -> scrDel(step.args)
                    "set" -> scrSet(step.args)
                    "get" -> scrGet(step.args)
                    "nav" -> scrNav(step.args)
                    "page" -> scrPage(step.args)
                    "theme" -> scrTheme(step.args)
                }

                "run" -> when (step.cmd) {
                    "one" -> runOne(step.args)
                    "if" -> runIf(step.args)
                    "while" -> runWhile(step.args)
                    "flow" -> runFlow(step.args)
                    "pause" -> runPause(step.args)
                }

                "srv" -> when (step.cmd) {
                    "new" -> srvNew(step.args)
                    "add" -> srvAdd(step.args)
                    "run" -> srvRun(step.args)
                }

                "clt" -> when (step.cmd) {
                    "req" -> cltReq(step.args)
                }

                "tts" -> when (step.cmd) {
                    "say" -> ttsSay(step.args)
                    "say2" -> ttsSay2(step.args)
                }

                "stt" -> when (step.cmd) {
                    "start" -> sttStart(step.args)
                    "stop" -> sttStop(step.args)
                    "wake" -> sttWake(step.args)
                    "unwake" -> sttUnwake(step.args)
                    "res" -> sttRes(step.args)
                    "lastres" -> sttLastres(step.args)
                }

                "spr" -> when (step.cmd) {
                    "play" -> sprPlay(step.args)
                }

                "llm" -> when (step.cmd) {
                    "ask" -> llmAsk(step.args)
                }
                else -> throw VirtelException("This is not a Virtel module: ${step.mod}")
            }
            return false // is not erroring
        } catch (e: IndexOutOfBoundsException) {
            if (program.debugMode) {
                e.printStackTrace()
            }
            try {
                (nGetProgramModel()!!).also {
                    it.errorMessage.value = "On line ${step.line} in file " + step.fileName +
                            " in application with appId: ${step.appId}\n\n" +
                            "Not all need arguments provided for this command: " +
                            step.mod + " " + step.cmd +
                            "\nIf you`re developer, please, check documentation for it"
                    it.isErrorHappened.value = true
                }
            } catch (k:Exception){k.printStackTrace()}
            return true // erroring
        } catch (e: VirtelException) {
            if (program.debugMode) {
                e.printStackTrace()
            }
            try {
                (nGetProgramModel() ?: ProgramViewModel(PageModel(), program)).also {
                    it.errorMessage.value = "On line ${step.line} in file " + step.fileName +
                            " in application with appId: ${step.appId}\n\n" + e.message.toString()
                    it.isErrorHappened.value = true
                }
            } catch (e:Exception){e.printStackTrace()}
            return true // erroring
        }
    }


    /**lexer**/
    fun runFile(fileName: String) {

        val code = okio.FileSystem.SYSTEM.read(
            "${FileSystem.programsPath}/${program.appId}${FileSystem.srCode}$fileName".toPath()
        )
        { readUtf8() }
        runCode(code,fileName)
    }
    fun runCode(code: String, fileName: String) {
        try {
            var lineNumber = 0
            var lastString = ""
            var step = Step(true)
            var word: Byte = 0
            var inValue = false
            var displayer = false
            //lexing
            for (i in code.indices) {
                when (val c = code[i]) {
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
                    }

                    '"' -> {
                        if (displayer) {
                            displayer = false
                        } else {
                            inValue = !inValue
                        }
                        lastString += '"'
                    }

                    ';' -> {
                        step.args.add(lastString)

                        step.appId = program.appId
                        step.fileName = fileName
                        step.line = lineNumber

                        if (runStep(step)) break // if erroring

                        lineNumber++
                        lastString = ""
                        step = Step(true)
                        word = 0
                        inValue = false
                        displayer = false
                    }

                    else -> {
                        lastString += c
                        displayer = false
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun nGetVar(name: String, type: DataType): Any {
        var gottenVar = program.store.getVar(name, type)
        if (gottenVar != null){
            return gottenVar
        }
        cslError(mutableListOf("\"Variable not found: $name\""))
        return ""
    }
    fun nPutVar(name: String, type: DataType, value: Any) = program.store.putVar(name, type, value)
    fun nGetProgramModel():ProgramViewModel? {
        var model:ProgramViewModel? = null
        for (pageModel in VirtelSystem.screenModel.pageModels) {
            for (programModel in pageModel.programViewModels) {
                if (programModel.program.appId.equals(program.appId)) {
                    model = programModel
                }
            }
        }
        return model
    }
    fun nGetNameOfNextFlow(file:String):String{
        var splitByDot = file.split(".").toMutableList()
        splitByDot.removeLast()
        return splitByDot.joinToString(".")+"-"+program.flows.size
    }
}

data class Step(
    var works: Boolean
) {
    lateinit var mod: String
    lateinit var cmd: String
    var args: MutableList<String> = mutableListOf()
    lateinit var appId: String
    lateinit var fileName: String
    var line: Int = 0
}