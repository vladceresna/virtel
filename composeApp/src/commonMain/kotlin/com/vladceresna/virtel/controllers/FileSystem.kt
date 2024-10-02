package com.vladceresna.virtel.controllers

import okio.FileSystem
import okio.Path.Companion.toPath
import okio.SYSTEM

data object FileSystem {
    lateinit var osHomeDir: String
    lateinit var systemPath: String
    lateinit var programsPath: String
    lateinit var launcherPath: String
    lateinit var launcherCodePath: String
    lateinit var launcherStartPath: String
    lateinit var userFilesPath: String
    lateinit var systemConfigPath: String

    fun scan(osHomeDir: String){
        this.osHomeDir = osHomeDir
        systemPath = "$osHomeDir/virtel/0"
        programsPath = "$systemPath/programs"
        launcherPath = "$programsPath/vladceresna.virtel.launcher"
        launcherCodePath = "$launcherPath/code"
        launcherStartPath = "$launcherCodePath/start.steps"
        userFilesPath = "$systemPath/files"
        systemConfigPath = "$systemPath/config"
    }
    fun isInstalled():Boolean{
        return FileSystem.SYSTEM.exists(launcherStartPath.toPath())
    }
    fun install(){
        FileSystem.SYSTEM.createDirectories(launcherCodePath.toPath())
        Launchers.loadLauncher(
            "https://drive.usercontent.google.com/u/0/uc?id=1xPqJuiJO1E9C7GP91E0c5cYPe5xnDCOb&export=download")
        //Launchers.loadLauncher("https://ktor.io/")

    }



}