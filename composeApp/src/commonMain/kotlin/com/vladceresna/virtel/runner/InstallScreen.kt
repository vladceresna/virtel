package com.vladceresna.virtel.runner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun InstallScreen(){
    var text by remember { mutableStateOf("") }
    var virtelSystem = VirtelSystem


    Column(
        Modifier.fillMaxSize(),
        Arrangement.Center,
        Alignment.CenterHorizontally
    ) {
        Text(
            text = "Virtel",
            fontSize = 100.sp,
            fontWeight = FontWeight.Black
        )
        Text(
            text = "Please install Virtel Platform for start working",
            fontSize = 25.sp,
            fontWeight = FontWeight.Black,
            color = Color.Green
        )
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Path") }
        )
        Button(
            onClick = {
                virtelSystem.install(text)
            }
        ){
            Text("Install")
        }
    }
}