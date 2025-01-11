package com.vladceresna.virtel.controllers

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import okio.Path.Companion.toPath

data object Logger{
    var logs: MutableList<String> = mutableStateListOf()
    var readLine = mutableStateOf(false)
    var afterReadLine: (readedValue: String) -> Unit = {}
}

fun log(message: String, type: Log){
    val currentMoment: Instant = Clock.System.now()
    val datetimeInSystemZone: LocalDateTime = currentMoment.toLocalDateTime(TimeZone.currentSystemDefault())
    val messageRes = when(type){
        Log.SUCCESS -> "\u001B[32m[SUCCESS] : ${datetimeInSystemZone} :\n $message\u001B[0m"
        Log.ERROR ->   "\u001B[31m[ERROR]   : ${datetimeInSystemZone} :\n $message\u001B[0m"
        Log.WARNING -> "\u001B[33m[WARNING] : ${datetimeInSystemZone} :\n $message\u001B[0m"
        Log.DEBUG ->    "\u001B[0m[DEBUG]   : ${datetimeInSystemZone} :\n $message"
        Log.INFO ->     "\u001B[0m[INFO]    : ${datetimeInSystemZone} :\n $message"
        Log.PROGRAM ->  "\u001B[0m[PROGRAM] : ${datetimeInSystemZone} :\n $message"
    }
    logAny(messageRes)
    Logger.logs.add(messageRes
        .replace("\u001B[32m","")
        .replace("\u001B[31m","")
        .replace("\u001B[33m","")
        .replace("\u001B[0m",""))
    try {
        okio.FileSystem.SYSTEM.write(FileSystem.systemLogPath.toPath()){
            writeUtf8(Logger.logs.joinToString("\n"))}
    } catch (e:Exception){}
}
expect fun logAny(message: String)

enum class Log {
    ERROR, WARNING, SUCCESS, INFO, PROGRAM, DEBUG
}