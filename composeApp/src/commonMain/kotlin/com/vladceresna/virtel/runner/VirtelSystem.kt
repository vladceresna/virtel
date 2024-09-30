package com.vladceresna.virtel.runner

import com.vladceresna.virtel.getHomePath
import com.vladceresna.virtel.getPlatform
import kotlinx.coroutines.delay
import kotlinx.io.files.Path

data object VirtelSystem {
    lateinit var renderFunction: () -> Unit
    lateinit var currentRunnedProgram: Program

    var isLoading: Boolean = true
    var installPath = ""
    var programs: List<Program> = mutableListOf()


    suspend fun start() {
        delay(5000)
        if(!isInstalled()){
            println(getHomePath())
        }
        scanPrograms()
        isLoading = false
        renderFunction()
    }

    fun scanPrograms(): Unit {
        currentRunnedProgram = Program(
            "appId",
            Path("appId"),
            ProgramStatus.SCREEN
        )
    }

    fun isInstalled(): Boolean {
        return false // TODO
    }
    fun install(path:String){

    }
    fun getInstallPath() {

    }
    fun getApps(): List<Program> {
        return emptyList()
    }
    fun runProgram(appId:String) {

    }
}