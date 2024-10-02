package com.vladceresna.virtel.controllers

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data object Logger{
    var logs: MutableList<String> = mutableListOf()
}

fun log(message: String, type: Log){
    val currentMoment: Instant = Clock.System.now()
    val datetimeInUtc: LocalDateTime = currentMoment.toLocalDateTime(TimeZone.UTC)
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
}
expect fun logAny(message: String)

enum class Log {
    ERROR, WARNING, SUCCESS, INFO, PROGRAM, DEBUG
}