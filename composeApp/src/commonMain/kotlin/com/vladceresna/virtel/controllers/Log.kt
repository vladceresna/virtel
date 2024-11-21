package com.vladceresna.virtel.controllers

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import okio.Path.Companion.toPath
import okio.SYSTEM

data object Logger{
    var logs: MutableList<String> = mutableListOf()
}

fun log(message: String, type: Log){
    val currentMoment: Instant = Clock.System.now()
    val datetimeInSystemZone: LocalDateTime = currentMoment.toLocalDateTime(TimeZone.currentSystemDefault())
    val messageRes = when(type){
        Log.SUCCESS -> "\u001B[32m[SUCCESS] : ${datetimeInSystemZone} : $message\u001B[0m"
        Log.ERROR ->   "\u001B[31m[ERROR]   : ${datetimeInSystemZone} : $message\u001B[0m"
        Log.WARNING -> "\u001B[33m[WARNING] : ${datetimeInSystemZone} : $message\u001B[0m"
        Log.DEBUG ->    "\u001B[0m[DEBUG]   : ${datetimeInSystemZone} : $message"
        Log.INFO ->     "\u001B[0m[INFO]    : ${datetimeInSystemZone} : $message"
        Log.PROGRAM ->  "\u001B[0m[PROGRAM] : ${datetimeInSystemZone} : $message"
    }
    logAny(messageRes)
    Logger.logs.add(messageRes)
    try {
        okio.FileSystem.SYSTEM.write(FileSystem.systemLogPath.toPath()){
            writeUtf8(getLog()+messageRes+"\n")}
    } catch (e:Exception){}
}
fun getLog(): String {
    try {
        return okio.FileSystem.SYSTEM.read(FileSystem.systemLogPath.toPath()){readUtf8()}
    } catch (e:Exception) {
        return ""
    }
}
expect fun logAny(message: String)

enum class Log {
    ERROR, WARNING, SUCCESS, INFO, PROGRAM, DEBUG
}