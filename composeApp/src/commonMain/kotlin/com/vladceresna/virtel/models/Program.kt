package com.vladceresna.virtel.models

import okio.Path


data class Program(
    var appId: String,
    var path: Path,
    var status: ProgramStatus
    //TODO:more configs and scans
)

enum class ProgramStatus{
    SCREEN, BACKGROUND, DISABLED
}