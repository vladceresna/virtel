package com.vladceresna.virtel

import kotlinx.io.files.Path

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
expect fun getHomePath(): String?