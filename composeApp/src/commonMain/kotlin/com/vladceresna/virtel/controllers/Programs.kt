package com.vladceresna.virtel.controllers

import com.vladceresna.virtel.other.VirtelException
import com.vladceresna.virtel.screens.model.ProgramViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okio.Path
import okio.Path.Companion.toPath
import okio.SYSTEM

data object Programs {
    var programs: MutableList<Program> = mutableListOf()
    val fileSystem = FileSystem


    fun scanPrograms(){
        log("Programs scanning",Log.INFO)
        var tempres:List<Path> = okio.FileSystem.SYSTEM.list(fileSystem.programsPath.toPath())
        programs = mutableListOf()
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
        throw VirtelException("Not found program: "+appId)//TODO:Fix
    }

    fun startProgram(appId: String){

        var program:Program = findProgram(appId)
        program.scan()



        var screenModel = VirtelSystem.screenModel
        var pageModel = screenModel.pageModels.get(screenModel.currentPageIndex.value)

        var programModel = ProgramViewModel(pageModel, program)

        pageModel.programViewModels.add(programModel)


        program.run()

    }
    fun runProgram(program: Program){
        program.run()

    }
}