package com.vladceresna.virtel.api

import com.vladceresna.virtel.store.VirtelStore
import uniffi.vnative.UiApi
import uniffi.vnative.VirtelCenter

class UiApiImpl: UiApi {
    override fun createWindow(title: String): String {
        TODO("Not yet implemented")
    }
    fun changeTheme() {
        VirtelStore.darkTheme.value = !VirtelStore.darkTheme.value
    }
}