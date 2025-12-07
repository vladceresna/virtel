package com.vladceresna.virtel.api

import com.vladceresna.virtel.store.UiStore
import com.vladceresna.virtel.store.VirtelStore
import com.vladceresna.virtel.store.Window
import uniffi.vnative.UiApi
import uniffi.vnative.VirtelCenter

class UiApiImpl: UiApi {
//    override fun createWindow(id: String, title: String, appId:String): String {
//        UiStore.windows.put(id, Window("Hello",
//            Pair(1,1), Pair(2,2),appId, true))
//        return id
//    }
//    override fun changeTheme() {
//        VirtelStore.darkTheme.value = !VirtelStore.darkTheme.value
//    }
    override fun createWindow(title: String): String {
        TODO("Not yet implemented")
    }

}
