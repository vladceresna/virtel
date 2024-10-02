package com.vladceresna.virtel.controllers

import okio.FileSystem
import okio.Path.Companion.toPath
import okio.SYSTEM

data object FileSystem {
    val srSystem = "/virtel/0"
    val srPrograms = "/programs"
    val srUserFiles = "/files"
    val srLauncher = "/vladceresna.virtel.launcher"
    val srCode = "/code"
    val srStart = "/start.steps"
    val srConfig = "/config.toml"
    val srCache = "/cache"

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
        systemPath = "$osHomeDir$srSystem"
        programsPath = "$systemPath$srPrograms"
        launcherPath = "$programsPath$srLauncher"
        launcherCodePath = "$launcherPath$srCode"
        launcherStartPath = "$launcherCodePath$srStart"
        userFilesPath = "$systemPath$srUserFiles"
        systemConfigPath = "$systemPath$srConfig"
    }
    fun isInstalled():Boolean{
        return FileSystem.SYSTEM.exists(launcherStartPath.toPath())
    }
    fun install(){
        FileSystem.SYSTEM.createDirectories(launcherCodePath.toPath())
        Launchers.loadLauncher(
            "https://drive.usercontent.google.com/u/0/uc?id=1xPqJuiJO1E9C7GP91E0c5cYPe5xnDCOb&export=download")
    }
}