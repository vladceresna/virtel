package com.vladceresna.virtel.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.vladceresna.virtel.controllers.Log
import com.vladceresna.virtel.controllers.log
import com.vladceresna.virtel.other.makeError
import com.vladceresna.virtel.screens.model.WidgetModel
import com.vladceresna.virtel.screens.model.WidgetType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Widget(model: WidgetModel, modifier: Modifier){

    log("Render Widget $model", Log.DEBUG)
    var arrangementVertical = Arrangement.Center
    var arrangementHorizontal = Arrangement.Center
    var alignmentVertical = Alignment.CenterVertically
    var alignmentHorizontal = Alignment.CenterHorizontally


    var modifierClickable = modifier


    if(model.scrollable){
        var scrollState = rememberScrollState()
        when(model.widgetType){
            WidgetType.COLUMN -> modifierClickable = modifierClickable.verticalScroll(scrollState)
            WidgetType.ROW -> modifierClickable = modifierClickable.horizontalScroll(scrollState)
            else -> makeError(model.programViewModel,
                "This WidgetType doesnt support scroll: "+model.widgetType+
                        " in var: "+model.name)
        }
    }

    modifierClickable = modifierClickable.padding(
        model.paddingLeft,
        model.paddingTop,
        model.paddingRight,
        model.paddingBottom
    )



    var onClick: () -> Unit = {}
    if (!model.onClick.equals("")){
        modifierClickable = modifier.clickable {
            model.programViewModel.program.runFlow(model.onClick, model.onClick) }
        onClick = {
            model.programViewModel.program.runFlow(model.onClick, model.onClick) }
    }
    println("${model.widgetType} ${model.title}")
    when (model.widgetType) {
        WidgetType.TEXT -> Text(modifier = modifierClickable, text = model.title)
        WidgetType.BUTTON -> when(model.variant) {
            "primary" -> Button(onClick = onClick, modifier = modifier) {
                if(!model.childs.isEmpty()) {
                    var childModel = model.childs.get(0)
                    Widget(childModel, Modifier.weight(childModel.weight))
                }
            }
        }
        WidgetType.INPUT -> {
            var mutValue by remember { mutableStateOf(model.value) }
            OutlinedTextField(
                label = { Text(text = model.title) },
                modifier = modifier,
                value = mutValue,
                onValueChange = {
                    mutValue = it
                    model.value = it
                }
            )
        }
        WidgetType.ROW -> Row(
            modifier = modifier,
            horizontalArrangement = arrangementHorizontal,
            verticalAlignment = alignmentVertical
        ) {
            model.childs.forEach {
                when (it.weight) {
                    -1F -> {
                        Widget(it, Modifier.fillMaxHeight().wrapContentWidth())
                    }
                    1F -> {
                        Widget(it, Modifier.weight(it.weight,true).fillMaxHeight().fillMaxWidth())
                    }
                    else -> {
                        Widget(it, Modifier.weight(it.weight).fillMaxHeight().fillMaxWidth())
                    }
                }
            }
        }
        WidgetType.COLUMN -> Column(
            modifier = modifier,
            verticalArrangement = arrangementVertical,
            horizontalAlignment = alignmentHorizontal
        ) {
            model.childs.forEach {
                when (it.weight) {
                    -1F -> {
                        Widget(it, Modifier.fillMaxWidth().wrapContentHeight())
                    }
                    1F -> {
                        Widget(it, Modifier.weight(it.weight,true).fillMaxWidth().fillMaxHeight())
                    }
                    else -> {
                        Widget(it, Modifier.weight(it.weight).fillMaxWidth().fillMaxHeight())
                    }
                }
            }
        }
        WidgetType.ADAPTIVE -> {
            if(!model.childs.isEmpty()) {
                Adaptive(
                    left = {Widget(model.childs.get(0),
                        Modifier.fillMaxWidth())},
                    right = {Widget(model.childs.get(1),
                        Modifier.fillMaxWidth())},
                    leftModel = model.childs.get(0),
                    rightModel = model.childs.get(1)
                )
            }
        }
        WidgetType.CARD -> {
            Card(modifierClickable) {
                Box(modifierClickable){
                    model.childs.forEach {
                        Widget(it, Modifier)
                    }
                }
            }
        }
        WidgetType.TOP_BAR -> TopAppBar(
            title = {
                Text("I'm a TopAppBar")
            },
            navigationIcon = {
                IconButton(onClick = {/* Do Something*/ }) {
                    Icon(Icons.Filled.ArrowBack, null)
                }
            }, actions = {
                IconButton(onClick = {/* Do Something*/ }) {
                    Icon(Icons.Filled.Share, null)
                }
                IconButton(onClick = {/* Do Something*/ }) {
                    Icon(Icons.Filled.Settings, null)
                }
            }
        )
        WidgetType.BOTTOM_BAR -> BottomAppBar (
            actions = {
                model.childs.forEach {
                    IconButton(onClick = {  }) {
                        Icon(IconRenderer(model.foreground), contentDescription = "Localized description")
                    }
                }
            },
            floatingActionButton = {
                Column {
                    FloatingActionButton(
                        onClick = {  },
                        containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
                        elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation()
                    ) {
                        Icon(Icons.Filled.Menu, "Localized description")
                    }
                }
            }
        )
        else -> Box(modifier)
    }
}
@Composable
fun IconRenderer(name: String):ImageVector{
    return when(name){
        "check" -> Icons.Filled.Check
        else -> Icons.Filled.Menu
    }
}

@Composable
expect fun Adaptive(
    left: @Composable () -> Unit,
    right: @Composable () -> Unit,
    leftModel: WidgetModel,
    rightModel: WidgetModel
)