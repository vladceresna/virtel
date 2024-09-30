package com.vladceresna.virtel.runner

import kotlinx.io.files.Path

data class Program(
    var appId: String,
    var path: Path,
    var status: ProgramStatus
)

enum class ProgramStatus{
    SCREEN, BACKGROUND
}