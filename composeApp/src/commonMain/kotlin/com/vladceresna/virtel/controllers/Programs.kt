package com.vladceresna.virtel.controllers

import com.vladceresna.virtel.other.VirtelException
import okio.Path
import okio.Path.Companion.toPath
import okio.SYSTEM

data object Programs {
    lateinit var currentRunnedProgram: Program
    var programs: MutableList<Program> = mutableListOf()
    val fileSystem = FileSystem


    fun scanPrograms(){
        log("Programs scanning",Log.INFO)
        var tempres:List<Path> = okio.FileSystem.SYSTEM.list(fileSystem.programsPath.toPath())
        tempres.forEach {
            programs.add(Program(it.toString()).also { it.scan() })
        }
        log("Programs scanned",Log.SUCCESS)
    }

    fun findProgram(appId:String): Program {
        for (program in programs) {
            if(program.appId.equals(appId)){
                return program
            }
        }
        throw VirtelException()//TODO:Fix
    }

    fun startProgram(appId: String){
        var program = findProgram(appId)
        program.scan()
        currentRunnedProgram = program
        program.run()
    }
    fun runProgram(program: Program){
        program.run()

    }
}