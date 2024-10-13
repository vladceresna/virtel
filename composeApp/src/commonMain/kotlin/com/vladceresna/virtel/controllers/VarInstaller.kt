package com.vladceresna.virtel.controllers

import okio.Path.Companion.toPath
import okio.SYSTEM

object VarInstaller {
    fun install(appId: String, varContent: String){
        var appCodePath = FileSystem.programsPath+"/"+appId+"/code"

        var fileName = ""
        var fileContent = ""
        var readFileName = false
        var readFileContent = false
        var readed = false
        for (char in varContent) when(char){
            '[' -> {
                if (readed == false) readed = true
                else {
                    okio.FileSystem.SYSTEM.write(
                        (appCodePath+fileName).toPath()) {writeUtf8(fileContent)}
                }
                readFileName = true
                readFileContent = false
                fileName = ""
            }
            ']' -> {
                readFileName = false
                readFileContent = true
                fileContent = ""
            }
            else -> {
                if (readFileName) fileName+=char
                else if (readFileContent) fileContent+=char
            }
        }
        Programs.scanPrograms()
    }
    fun toVar(appId:String):String{
        var varContent = ""
        FileSystem.getListPaths("${FileSystem.programsPath}/$appId${FileSystem.srCode}/").forEach {
            varContent+="\n[${it.toString().split("$appId/code").get(1)}]\n"
            varContent+= okio.FileSystem.SYSTEM.read(it){readUtf8()}
        }
        varContent+="\n[end]"
        return varContent
    }
}