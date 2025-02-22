package com.vladceresna.virtel.controllers

import androidx.compose.runtime.mutableStateMapOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okio.Path.Companion.toPath
import vnative.Storage


data class Program(var path: String){



    var config:HashMap<String, String> = hashMapOf()


    var debugMode:Boolean = true
    lateinit var status: ProgramStatus
    lateinit var appId: String
    lateinit var appName: String

    var flows = mutableStateMapOf<String,Flow>()

    lateinit var storage:Storage



    fun scan(){
        appId = path.toPath().name
        appName = appId.split(".").last()
        status = ProgramStatus.DISABLED
    }
    fun run(){
        status = ProgramStatus.BACKGROUND

        setStorage()

        var (flowName, thr) = runFlow("/start.steps")
        CoroutineScope(Job()).launch {
            status = ProgramStatus.SCREEN
            thr.start()
        }
    }
    fun setStorage(){
        storage = Storage(FileSystem.programsPath +
                "/" +appId+
                FileSystem.srData+
                FileSystem.srStorageFile
        )
    }

    fun runFlow(fileName:String): Pair<String,Thread> {
        var flowName:String
        var flow:Flow
        synchronized(flows){
            flowName = "flow-${flows.size}"
            flow = Flow(this, flowName,Step(false))
            flows.put(flowName, flow)
        }

        return Pair(
            flowName,
            Thread {
                status = ProgramStatus.SCREEN
                flow.runFile(fileName)
            }
        )
    }
    fun destroy(){
        status = ProgramStatus.DISABLED
        log("Program $appId destroyed", Log.SUCCESS)
    }
}

enum class ProgramStatus{
    SCREEN, BACKGROUND, DISABLED
}