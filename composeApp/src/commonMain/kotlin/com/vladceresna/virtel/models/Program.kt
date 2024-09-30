package com.vladceresna.virtel.models

import kotlinx.io.files.Path

data class Program(
    var appId: String,
    var path: Path,
    var status: ProgramStatus
)

enum class ProgramStatus{
    SCREEN, BACKGROUND, DISABLED
}