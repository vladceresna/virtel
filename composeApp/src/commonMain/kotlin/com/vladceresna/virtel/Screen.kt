package com.vladceresna.virtel

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Screen(appId: String, darkTheme: Boolean) {
    var text by remember { mutableStateOf("") }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(appId) },
            )
        }
    ){
        Surface (
            modifier = Modifier.fillMaxSize().padding(0.dp,64.dp,0.dp,0.dp)
        ) {
            var dom = ScreenOperator
            Column {
                Text(text)
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Label") }
                )
            }

        }
    }

}
