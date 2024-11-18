package com.vladceresna.virtel.screens.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.vladceresna.virtel.controllers.DataStore
import com.vladceresna.virtel.controllers.DataType
import com.vladceresna.virtel.controllers.ScreenModel
import com.vladceresna.virtel.controllers.VirtelSystem
import com.vladceresna.virtel.controllers.WidgetModel
import com.vladceresna.virtel.screens.Widget
import com.vladceresna.virtel.screens.model.ProgramViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgramLayer(
    programViewModel:ProgramViewModel
){
    var virtelSystem = VirtelSystem

    Column {
        Scaffold(
            Modifier.weight(1f),
            topBar = {
                TopAppBar(
                    title = { Text(virtelSystem.getCurrentRunnedProgram().appId) },
                )
            },
            bottomBar = {
                var bottomData = programViewModel.bottomAppBar
                if(!bottomData.childs.isEmpty()){
                    var model = bottomData.value as WidgetModel
                    Widget(model, Modifier)
                }
            }
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(10.dp, 64.dp, 10.dp, if(!programViewModel.bottomAppBar.childs.isEmpty()) 90.dp else 10.dp)
            ) {
                var rootData = programViewModel.root

                if(!rootData.childs.isEmpty()){
                    var model = rootData.value as WidgetModel
                    Widget(model, Modifier.weight(model.weight))
                }
            }
        }

    }
}