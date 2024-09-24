package com.vladceresna.virtel

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform