package com.vladceresna.virtel.runner

import com.vladceresna.virtel.controllers.Data
import com.vladceresna.virtel.controllers.DataStore
import com.vladceresna.virtel.controllers.DataType
import com.vladceresna.virtel.controllers.FileSystem
import com.vladceresna.virtel.controllers.Log
import com.vladceresna.virtel.controllers.Programs
import com.vladceresna.virtel.controllers.log
import okio.Path.Companion.toPath
import okio.SYSTEM

class Flow (
    var appId: String,
    var flowName: String
) {
    fun cmdWrite(args: MutableList<String>){
        var value = DataStore.find(appId, DataType.VAR, args.get(0)).value
        log(value.toString(),Log.PROGRAM)
    }
    fun cmdRead(args: MutableList<String>){
        var resVarName = DataStore.find(appId, DataType.VAR, args.get(0)).value//check if value is clear, pure startly
        DataStore.data.add(Data(appId, DataType.VAR, resVarName as String, readln()!!))

    }
    fun stgPut(args: MutableList<String>){

    }
    fun stgGet(args: MutableList<String>) {

    }
    fun stgDel(args: MutableList<String>) {

    }



    fun runStep(step: Step){
        if(Programs.findProgram(appId).debugMode) {
            log("Executes ${step.mod} ${step.cmd} ${step.args}", Log.WARNING)
        }
        when(step.mod){
            "cmd" -> when(step.cmd){
                "write" -> cmdWrite(step.args)
                "read" -> cmdRead(step.args)
            }
            "stg" -> when(step.cmd){
                "put" -> stgPut(step.args)
                "get" -> stgGet(step.args)
                "del" -> stgDel(step.args)
            }
            "mat" -> when(step.cmd){
                "plus" -> cmdWrite(step.args)
                "minus" -> cmdRead(step.args)
                "mult" -> cmdRead(step.args)
                "div" -> cmdRead(step.args)
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