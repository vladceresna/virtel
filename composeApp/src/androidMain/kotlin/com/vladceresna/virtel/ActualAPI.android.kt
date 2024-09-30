package com.vladceresna.virtel

import java.io.File

actual fun isFileExists(path: String): Boolean = File(path).exists()