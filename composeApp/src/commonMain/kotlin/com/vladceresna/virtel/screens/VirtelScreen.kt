package com.vladceresna.virtel.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import com.vladceresna.virtel.controllers.VirtelSystem
import com.vladceresna.virtel.controllers.ScreenModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VirtelScreen() {
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
                BottomAppBar(
                    actions = {
                        IconButton(onClick = { /* do something */ }) {
                            Icon(Icons.Filled.Check, contentDescription = "Localized description")
                        }
                        IconButton(onClick = { /* do something */ }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Localized description")
                        }
                        IconButton(onClick = { /* do something */ }) {
                            Icon(
                                Icons.Filled.Settings,
                                contentDescription = "Localized description"
                            )
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
                )
            }
        ) {
            Surface(
                modifier = Modifier.fillMaxSize().padding(0.dp, 64.dp, 0.dp, 0.dp)
            ) {
                //todo:rendering
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
