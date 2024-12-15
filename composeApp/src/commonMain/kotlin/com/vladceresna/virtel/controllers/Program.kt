package com.vladceresna.virtel.controllers

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okio.Path.Companion.toPath


data class Program(var path: String){
    var store:DataStore = DataStore(this)



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
        }
    }
    fun destroy(){

    }
}

enum class ProgramStatus{
    SCREEN, BACKGROUND, DISABLED
}