package com.vladceresna.virtel.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.LineHeightStyle
import com.vladceresna.virtel.controllers.DataStore
import com.vladceresna.virtel.controllers.DataType
import com.vladceresna.virtel.controllers.Log
import com.vladceresna.virtel.controllers.Programs
import com.vladceresna.virtel.controllers.ScreenModel
import com.vladceresna.virtel.controllers.VirtelSystem
import com.vladceresna.virtel.controllers.WidgetModel
import com.vladceresna.virtel.controllers.WidgetType
import com.vladceresna.virtel.controllers.log

@Composable
fun Widget(rd:Boolean, model: WidgetModel, modifier: Modifier){
    rd
    //log("Render Widget $model", Log.DEBUG)
    var arrangement = Arrangement.Center
    var alignmentVertical = Alignment.CenterVertically
    var alignmentHorizontal = Alignment.CenterHorizontally
    var alignment = Alignment.Center

    var modifierClickable = modifier.clickable {
        Programs.findProgram(model.appId).runFlow(model.onclick,model.onclick)
    }
    var onClick: () -> Unit = {
        Programs.findProgram(model.appId).runFlow(model.onclick,model.onclick)
    }
    when (model.widgetType) {
        WidgetType.TEXT -> Box(modifierClickable, contentAlignment = alignment){Text(model.title)}
        WidgetType.BUTTON -> Button(onClick = onClick, modifier = modifier){
            var data = DataStore.tryGet(VirtelSystem.getCurrentRunnedProgram().appId,
                DataType.VIEW, model.childs.get(0))
            if (data.returnType == DataType.VIEW) {
                var model = data.value as WidgetModel
                Widget(rd, model, Modifier.weight(model.weight))
            }
        }
        WidgetType.INPUT -> {
            rd
            var value by remember { mutableStateOf(model.value) }
            OutlinedTextField(
                label = { Text(model.title) },
                modifier = modifier,
                value = value,
                onValueChange = {
                    model.value = it
                    value = it
                    DataStore.put(model.appId,DataType.VIEW,model.name,model)
                }
            )
        }
        WidgetType.ROW -> Row(
            modifier = modifier,
            horizontalArrangement = arrangement,
            verticalAlignment = alignmentVertical
        ) {
            for (child in model.childs) {
                var data = DataStore.tryGet(VirtelSystem.getCurrentRunnedProgram().appId,
                    DataType.VIEW, child)
                if (data.returnType == DataType.VIEW) {
                    var model = data.value as WidgetModel
                    Widget(rd, model, Modifier.weight(model.weight))
                }
            }
        }
        WidgetType.COLUMN -> Column(
            modifier = modifier,
            verticalArrangement = arrangement,
            horizontalAlignment = alignmentHorizontal
        ) {
            for (child in model.childs) {
                var data = DataStore.tryGet(VirtelSystem.getCurrentRunnedProgram().appId,
                    DataType.VIEW, child)
                if (data.returnType == DataType.VIEW) {
                    var model = data.value as WidgetModel
                    Widget(rd, model, Modifier.weight(model.weight))
                }
            }
        }
        WidgetType.BOTTOM_BAR -> BottomAppBar (
            actions = {
                for (action in model.actions) {
                    var data = DataStore.tryGet(VirtelSystem.getCurrentRunnedProgram().appId,
                        DataType.VIEW, action)
                    if (data.returnType == DataType.VIEW) {
                        var model = data.value as WidgetModel
                        IconButton(onClick = {  }) {
                            Icon(IconRenderer(model.icon), contentDescription = "Localized description")
                        }
                    }
                }

            },
            floatingActionButton = {
                Column {
                    FloatingActionButton(
                        onClick = { /* do something */ },
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