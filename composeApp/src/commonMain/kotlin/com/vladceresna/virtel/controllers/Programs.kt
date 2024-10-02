package com.vladceresna.virtel.controllers

import com.vladceresna.virtel.models.Program
import com.vladceresna.virtel.models.ProgramStatus
import com.vladceresna.virtel.other.VirtelException
import com.vladceresna.virtel.runner.Flow
import okio.Path
import okio.Path.Companion.toPath
import okio.SYSTEM

data object Programs {
    lateinit var currentRunnedProgram: Program
    var programs: MutableList<Program> = mutableListOf()
    val fileSystem = FileSystem


    fun scanPrograms(){
        var tempres:List<Any> = okio.FileSystem.SYSTEM.list(fileSystem.programsPath.toPath())
        tempres.forEach {
            programs.add(Program((it as Path).name, it, ProgramStatus.DISABLED))
        }
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
        runProgram(program)
    }
    fun runProgram(program: Program){
        //TODO: getconfigs, caches, run new coroutine, thread, flow with code
    }
}