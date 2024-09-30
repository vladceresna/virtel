package com.vladceresna.virtel.runner

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import com.vladceresna.virtel.Platform
import com.vladceresna.virtel.getHomePath
import com.vladceresna.virtel.getPlatform

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VirtelScreen() {
    var text by remember { mutableStateOf("") }
    var virtelSystem = VirtelSystem


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(virtelSystem.currentRunnedProgram.appId) },
            )
        }

    ){
        Surface (
            modifier = Modifier.fillMaxSize().padding(0.dp,64.dp,0.dp,0.dp)
        ) {
            var dom = WidgetModel()
            Column {
                Text(text)
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("${getPlatform().name}${getHomePath()}") }
                )
                Button(
                    onClick = {
                        virtelSystem.isLoading = true
                        virtelSystem.renderFunction()

                    }
                ){
                    Text("Click")
                }
            }

        }
    }

}
