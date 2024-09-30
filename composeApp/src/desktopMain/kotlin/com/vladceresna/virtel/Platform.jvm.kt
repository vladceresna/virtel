package com.vladceresna.virtel

class JVMPlatform: Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
}

actual fun getPlatform(): Platform = JVMPlatform()
actual fun getHomePath(): String? {
    return System.getProperty("user.home")
}