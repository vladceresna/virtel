package com.vladceresna.virtel

class JVMPlatform: Platform {
    override val name: String = "Java ${Processor.getProperty("java.version")}"
}

actual fun getPlatform(): Platform = JVMPlatform()