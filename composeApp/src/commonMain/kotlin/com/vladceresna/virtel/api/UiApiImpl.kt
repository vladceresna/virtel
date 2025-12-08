package com.vladceresna.virtel.api

import com.vladceresna.virtel.store.UiStore
import com.vladceresna.virtel.store.UiStore.windows
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

    override fun createWindow(
        title: String,
        width: Long,
        height: Long
    ): String {
        var window = Window(title, Pair(1,1), Pair(2,2),"",true)
        windows[title] = window
        val text2 = window.createText(
            id = "text",
            initialText = title
        )
        window.widgetsTree.add(text2)
        return title
    }

    override fun putBox(node: String, id: String): String {
        TODO("Not yet implemented")
    }

}
