package com.vladceresna.virtel.runner

import com.vladceresna.virtel.controllers.FileSystem
import com.vladceresna.virtel.getHomePath
import com.vladceresna.virtel.models.Program
import com.vladceresna.virtel.models.ProgramStatus
import com.vladceresna.virtel.other.VirtelException
import kotlinx.coroutines.delay
import kotlinx.io.files.Path

data object VirtelSystem {
    var isLoading: Boolean = true
    lateinit var renderFunction: () -> Unit
    var currentRunnedProgram: Program = Program("s", Path(""),ProgramStatus.SCREEN)//TODO:VisualModule

    var fileSystem = FileSystem

    suspend fun start() {
        delay(1000)
        val homePath = getHomePath()
        if(homePath == null) throw VirtelException()//TODO:Fix iOS and JVM
        else fileSystem.scan(homePath)
        if(fileSystem.isInstalled().not()){
            install()
        }
        //TODO:running

        isLoading = false
        renderFunction()
    }

    fun install(){
        fileSystem.install()
    }

}