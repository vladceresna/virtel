package com.vladceresna.virtel.controllers

import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.SYSTEM

data object FileSystem {
    val srSystem = "/virtel/0"
    val srPrograms = "/programs"
    val srLog = "/log.txt"
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
    lateinit var systemLogPath: String

    fun scan(osHomeDir: String){
        this.osHomeDir = osHomeDir
        systemPath = "$osHomeDir$srSystem"
        programsPath = "$systemPath$srPrograms"
        launcherPath = "$programsPath$srLauncher"
        launcherCodePath = "$launcherPath$srCode"
        launcherStartPath = "$launcherCodePath$srStart"
        userFilesPath = "$systemPath$srUserFiles"
        systemConfigPath = "$systemPath$srConfig"
        systemLogPath = "$systemPath$srLog"
    }
    fun isInstalled():Boolean{
        return FileSystem.SYSTEM.exists(launcherStartPath.toPath())
    }
    fun install(){
        println(userFilesPath)
        FileSystem.SYSTEM.createDirectories(launcherCodePath.toPath())
        FileSystem.SYSTEM.createDirectories(userFilesPath.toPath())
        Launchers.loadLauncher(
            "https://drive.usercontent.google.com/uc?id=1JqI2WvSE738mA4YxRylNM0L3_fV-SqQl&export=download")
    }
    fun getListPaths(path: String): List<Path> {
        return okio.FileSystem.SYSTEM.listRecursively(path.toPath()).toList()
    }
}