package com.vladceresna.virtel.controllers

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okio.Path.Companion.toPath


data class Program(var path: String){
    var store:DataStore = DataStore(this)


    var config:HashMap<String, String> = hashMapOf()


    var debugMode:Boolean = true
    lateinit var status: ProgramStatus
    lateinit var appId: String
    lateinit var appName: String

    var flows:MutableMap<String, Flow> = mutableMapOf()



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
        CoroutineScope(Job()).launch {
            status = ProgramStatus.SCREEN
            flow.runFile(fileName)
            flows.remove(flowName)
            if (flows.isEmpty()) destroy()
        }
    }
    fun destroy(){
        status = ProgramStatus.DISABLED
        log("Program $appId destroyed", Log.SUCCESS)
    }
}

enum class ProgramStatus{
    SCREEN, BACKGROUND, DISABLED
}