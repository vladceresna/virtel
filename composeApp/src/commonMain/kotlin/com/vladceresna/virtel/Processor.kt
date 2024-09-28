package com.vladceresna.virtel

object Processor {
    var installPath = ""

    var programs: List<Program> = mutableListOf()

    fun start() {
        scanPrograms()
    }

    fun scanPrograms(): Unit {

    }

    fun isInstalled(): Boolean {
        return false // TODO
    }
    fun install(path: String){

    }
    fun getInstallPath() {

    }
    fun getApps(): List<Program> {
        return emptyList()
    }
    fun runProgram(appId:String) {

    }


}