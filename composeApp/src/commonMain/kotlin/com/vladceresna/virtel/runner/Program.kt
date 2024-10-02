package com.vladceresna.virtel.runner

import com.vladceresna.virtel.controllers.FileSystem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okio.Path
import okio.Path.Companion.toPath
import okio.SYSTEM


data class Program(var path: String){
    var debugMode:Boolean = true
    lateinit var status: ProgramStatus
    lateinit var appId: String

    var flows:MutableMap<String,Flow> = mutableMapOf()

    /*lateinit var config: String
    lateinit var cache: String*/

    val fileSystem = FileSystem

    fun scan(){
        appId = path.toPath().name
        status = ProgramStatus.DISABLED
        /*
        config = "$path${fileSystem.srConfig}"
        cache = "$path${fileSystem.srCache}"
        if (okio.FileSystem.SYSTEM.exists(config.toPath())){
            var result = okio.FileSystem.SYSTEM.read(config.toPath()) {
                readUtf8()
            }
        }*/
        //TODO:Config and cache analysis
    }
    fun run(){
        status = ProgramStatus.BACKGROUND
        runFlow("/start.steps", "main")

    }
    fun runFlow(fileName:String, flowName:String){
        var flow = Flow(appId, flowName)
        flows.put(flowName, flow)
        CoroutineScope(Job()).launch {
            flow.runFile(fileName)
        }
    }
    fun destroy(){

    }
}

enum class ProgramStatus{
    SCREEN, BACKGROUND, DISABLED
}