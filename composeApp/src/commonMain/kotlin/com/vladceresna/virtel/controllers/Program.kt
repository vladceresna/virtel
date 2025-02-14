package com.vladceresna.virtel.controllers

import androidx.compose.runtime.mutableStateMapOf
import okio.Path.Companion.toPath


data class Program(var path: String){



    var config:HashMap<String, String> = hashMapOf()


    var debugMode:Boolean = true
    lateinit var status: ProgramStatus
    lateinit var appId: String
    lateinit var appName: String

    var flows = mutableStateMapOf<String,Flow>()



    fun scan(){
        appId = path.toPath().name
        appName = appId.split(".").last()
        status = ProgramStatus.DISABLED
    }
    fun run(){
        status = ProgramStatus.BACKGROUND
        runFlow("/start.steps", "main")
    }
    fun runFlow(fileName:String, flowName:String){
        var flow = Flow(this, flowName,Step(false))
        flows.put(flowName, flow)

        Thread {
            status = ProgramStatus.SCREEN
            flow.runFile(fileName)
            flows.remove(flowName)
            if (flows.isEmpty()) destroy()
        }.start()
    }
    fun destroy(){
        status = ProgramStatus.DISABLED
        log("Program $appId destroyed", Log.SUCCESS)
    }
}

enum class ProgramStatus{
    SCREEN, BACKGROUND, DISABLED
}