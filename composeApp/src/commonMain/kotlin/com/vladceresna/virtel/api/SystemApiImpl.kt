package com.vladceresna.virtel.api

import com.vladceresna.virtel.getHomePath
import uniffi.vnative.SystemApi

class SystemApiImpl: SystemApi {
    override fun getOsHomeDir(): String {
        return getHomePath() ?: throw Exception("Your device doesn't support Virtel")
    }
}