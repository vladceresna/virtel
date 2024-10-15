package com.vladceresna.virtel.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import com.example.compose.errorContainerDark
import com.vladceresna.virtel.controllers.DataStore
import com.vladceresna.virtel.controllers.DataType
import com.vladceresna.virtel.controllers.Log
import com.vladceresna.virtel.controllers.Programs
import com.vladceresna.virtel.controllers.ScreenModel
import com.vladceresna.virtel.controllers.VirtelSystem
import com.vladceresna.virtel.controllers.WidgetModel
import com.vladceresna.virtel.controllers.WidgetType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Widget(rd:Boolean, model: WidgetModel, modifier: Modifier){
    rd
    //log("Render Widget $model", Log.DEBUG)
    var arrangement = Arrangement.Center
    var alignmentVertical = Alignment.CenterVertically
    var alignmentHorizontal = Alignment.CenterHorizontally
    var alignment = Alignment.Center

    var modifierClickable = modifier
    var onClick: () -> Unit = {}
    if (!model.onclick.equals("noclick.steps")){
        modifierClickable = modifier.clickable {
            Programs.findProgram(model.appId).runFlow(model.onclick, model.onclick) }
        onClick = {
            Programs.findProgram(model.appId).runFlow(model.onclick, model.onclick) }
    }
    when (model.widgetType) {
        WidgetType.TEXT -> Box(modifierClickable){
            Text(modifier = Modifier, text = model.title)}
        WidgetType.BUTTON -> when(model.variant) {
            0 -> Row {
                Button(onClick = onClick, modifier = modifier.wrapContentSize()) {
                    var data = DataStore.tryGet(
                        VirtelSystem.getCurrentRunnedProgram().appId,
                        DataType.VIEW, model.childs.get(0)
                    )
                    if (data.returnType == DataType.VIEW) {
                        var model = data.value as WidgetModel
                        Widget(rd, model, Modifier.weight(model.weight))
                    }
                }
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
                    model.value = value
                    value = model.value
                    DataStore.put(model.appId,DataType.VIEW,model.name,model)
                }
            )
            LaunchedEffect(Unit){
                CoroutineScope(Job()).launch{
                    while(true){
                        var data = DataStore.tryGet(
                            VirtelSystem.getCurrentRunnedProgram().appId,
                            DataType.VIEW, model.name
                        )
                        if (data.returnType == DataType.VIEW) {
                            var model = data.value as WidgetModel
                            delay(10)
                            if (model.value != value) {
                                value = model.value
                                model.value = value
                                DataStore.put(model.appId, DataType.VIEW, model.name, model)
                            }
                        }
                    }
                }
            }
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
                    Widget(rd, model, Modifier.weight(model.weight).fillMaxHeight())
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
                    var inmodifier = Modifier.weight(model.weight).fillMaxWidth()
                    when(inmodifier){

                    }
                    Widget(rd, model, inmodifier)
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
