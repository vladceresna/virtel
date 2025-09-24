package com.vladceresna.virtel.store

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import uniffi.vnative.VirtelCenter

data object UiStore {
    var windows = mutableStateListOf<Window>(
        Window(
            title = "Clicker",
            widgetsTree = mutableStateListOf(
                Button(
                    id = "button",
                    onClick = {

                    },
                    childWidgets = mutableStateListOf(
                        Text(
                            id = "text",
                            text = "Click me"
                        )
                    )
                )
            )
        )
    )

}
data class Window(
    var title: String,
    var widgetsTree: MutableList<Widget> = mutableStateListOf(),
    var widgets: MutableList<Widget> = mutableStateListOf()
)
interface Widget {
    var id: String
    @Composable
    fun render()
}

class Button(
    override var id: String,
    var onClick: () -> Unit,
    var childWidgets: MutableList<Widget> = mutableStateListOf()
): Widget {

    @Composable
    override fun render(){
        Button(
            onClick
        ){
            childWidgets.forEach { widget ->
                widget.render()
            }
        }
    }
}

class Text(
    override var id: String,
    var text: String
): Widget {

    @Composable
    override fun render(){
        Text(
            text
        )
    }
}