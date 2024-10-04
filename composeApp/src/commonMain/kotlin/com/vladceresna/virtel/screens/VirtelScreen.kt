package com.vladceresna.virtel.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.*
import androidx.compose.runtime.key
import androidx.lifecycle.ViewModel
import com.vladceresna.virtel.controllers.DataStore
import com.vladceresna.virtel.controllers.DataType
import com.vladceresna.virtel.controllers.Log
import com.vladceresna.virtel.controllers.VirtelSystem
import com.vladceresna.virtel.controllers.ScreenModel
import com.vladceresna.virtel.controllers.WidgetModel
import com.vladceresna.virtel.controllers.WidgetType
import com.vladceresna.virtel.controllers.log
import io.ktor.util.reflect.instanceOf
import kotlinx.coroutines.delay
import kotlin.reflect.KClass

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VirtelScreen(rd:Boolean) {
    var text by remember { mutableStateOf("") }
    var virtelSystem = VirtelSystem

    var screen = ScreenModel


    Column {
        Scaffold(
            Modifier.weight(1f),
            topBar = {
                TopAppBar(
                    title = { Text(virtelSystem.getCurrentRunnedProgram().appId) },
                )
            },
            bottomBar = {
                /*BottomAppBar(
                    actions = {
                        IconButton(onClick = { /* do something */ }) {
                            Icon(Icons.Filled.Check, contentDescription = "Localized description")
                        }
                        IconButton(onClick = { /* do something */ }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Localized description")
                        }
                        IconButton(onClick = { /* do something */ }) {
                            Icon(Icons.Filled.Add, contentDescription = "Localized description")
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
                )*/
            }
        ) {
            Surface(
                modifier = Modifier.fillMaxSize().padding(0.dp, 64.dp, 0.dp, 0.dp)
            ) {
                rd
                //todo:rendering
                var rootData = DataStore.tryGet(virtelSystem.getCurrentRunnedProgram().appId,DataType.VIEW, "${ScreenModel.root}")
                var root = rootData.value
                var isView = false
                when (rootData.returnType){
                    DataType.VIEW -> isView = true
                    else -> {}
                }
                println("$rootData $root $isView")
                log("Enrend", Log.DEBUG)
                if (isView) {
                    root = root as WidgetModel
                    log("Render", Log.DEBUG)
                    var modifier = Modifier.weight(root.weight).fillMaxSize()
                    when (root.widgetType) {
                        WidgetType.TEXT -> Text(root.title, modifier)
                        else -> Box(modifier)
                    }
                }

            }
        }
        Row {
            Text("",Modifier.weight(1f))
            IconButton(onClick = { screen.settingsClick() },Modifier.weight(1f)) {
                Icon(Icons.Filled.Settings, contentDescription = "Localized description")
            }
            IconButton(onClick = { screen.homeClick() },Modifier.weight(1f)) {
                Icon(Icons.Filled.Home, contentDescription = "Localized description")
            }
            IconButton(onClick = { screen.backClick() },Modifier.weight(1f)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Localized description")
            }
            Text("",Modifier.weight(1f))
        }
    }

}
