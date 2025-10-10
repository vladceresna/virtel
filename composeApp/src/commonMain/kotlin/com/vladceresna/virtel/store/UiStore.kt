package com.vladceresna.virtel.store

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vladceresna.virtel.icons.X

data object UiStore {
    val windows = mutableStateMapOf<String, Window>()

    init {
        val clickerWindow = Window("Clicker",Pair(1, 3), Pair(3, 5), "", true)

        val text = clickerWindow.createText(
            id = "text",
            initialText = "Click me"
        )

        val button = clickerWindow.createButton(
            id = "button",
            childWidgets = listOf(text),
            onClick = {
                val textWidget = clickerWindow.findWidgetById("text") as? Text
                val count = textWidget?.text?.split(" ")?.last()?.toIntOrNull() ?: 0
                textWidget?.text = "Clicked! ${count + 1}"
                VirtelStore.darkTheme.value = !VirtelStore.darkTheme.value
            }
        )

        clickerWindow.widgetsTree.add(button)

        windows.put("cw",clickerWindow)


        windows.put("hw",Window("Hello", Pair(1,1), Pair(2,2), "", true))

        var hw = windows.get("hw")!!

        val text2 = hw.createText(
            id = "text",
            initialText = "Hello"
        )
        hw.widgetsTree.add(text2)



    }
}
data class Window(
    var title: String,
    var start:Pair<Int, Int>,
    var end: Pair<Int, Int>,
    val appId:String,
    var visible: Boolean = false
) {
    private val widgetMap = mutableMapOf<String, Widget>()
    val widgetsTree = mutableStateListOf<Widget>()

    fun createText(id: String, initialText: String): Text {
        val widget = Text(id, initialText)
        registerWidget(id, widget)
        return widget
    }

    fun createButton(id: String, childWidgets: List<Widget>, onClick: () -> Unit): Button {
        val widget = Button(id, mutableStateListOf(*childWidgets.toTypedArray()), onClick)
        registerWidget(id, widget)
        return widget
    }

    private fun registerWidget(id: String, widget: Widget) {
        if (widgetMap.containsKey(id)) {
            throw IllegalStateException("Widget with ID '$id' already exists!")
        }
        widgetMap[id] = widget
    }

    fun destroyWidget(widget: Widget) {
        if (widget is ContainerWidget) {
            widget.childWidgets.toList().forEach { child ->
                destroyWidget(child)
            }
        }
        widgetMap.remove(widget.id)
        println("Destroyed widget: ${widget.id}")
    }

    fun findWidgetById(id: String): Widget? {
        return widgetMap[id]
    }

    @Composable
    fun render(key:String) {
        Column(
            modifier = Modifier.background(MaterialTheme.colorScheme.background)
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp, 20.dp, 0.dp,0.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            ) {
                Box(
                    modifier = Modifier
                        .padding(10.dp,10.dp)
                ) {
                    Text(this@Window.title,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface)
                }
                Box(Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .padding(10.dp,10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                            .clickable {
                                UiStore.windows.remove(key)
                            }
                            .padding(4.dp)
                    ) {
                        Icon(X, "Close",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
            Column{
                this@Window.widgetsTree.forEach { widget ->
                    widget.render()
                }
            }
            Row(
                modifier = Modifier
                    .height(20.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(0.dp, 0.dp, 20.dp,20.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            ) {
            }
        }
    }
}

interface Widget {
    var id: String
    @Composable
    fun render()
}
interface ContainerWidget: Widget {
    var childWidgets: MutableList<Widget>
}

class Button(
    override var id: String,
    override var childWidgets: MutableList<Widget>,
    var onClick: () -> Unit
): ContainerWidget {
    @Composable
    override fun render(){
        Button(onClick){
            childWidgets.forEach { widget ->
                widget.render()
            }
        }
    }
}
class Column(
    override var id: String,
    override var childWidgets: MutableList<Widget>
): ContainerWidget {
    @Composable
    override fun render(){
        Column {
            childWidgets.forEach { widget ->
                widget.render()
            }
        }
    }
}
class Row(
    override var id: String,
    override var childWidgets: MutableList<Widget>
): ContainerWidget {
    @Composable
    override fun render(){
        Row {
            childWidgets.forEach { widget ->
                widget.render()
            }
        }
    }
}

class Text(
    override var id: String,
    initialText: String
) : Widget {
    var text by mutableStateOf(initialText)

    @Composable
    override fun render() {
        Text(text = this.text)
    }
}

class Switch(
    override var id: String,
    var onCheck: (checked: Boolean) -> Unit
): Widget {
    var checked by mutableStateOf(false)

    @Composable
    override fun render(){
        Switch(
            checked,
            onCheck
        )
    }
}