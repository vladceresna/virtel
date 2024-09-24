package com.vladceresna.virtel

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun Installer() {
    var installFolder by remember { mutableStateOf("") }
        Column (
            Modifier.fillMaxSize(),
            Arrangement.Center,
            Alignment.CenterHorizontally
        ) {
            Text(
                text = "Virtel",
                modifier = Modifier.padding(10.dp),
                fontWeight = FontWeight(900),
                fontSize = 75.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Choose installing folder",
                modifier = Modifier.padding(20.dp),
                fontWeight = FontWeight(500),
                color = Color(0,200,0,255),
                fontSize = 20.sp,
                textAlign = TextAlign.Center
            )
            TextField(
                value = installFolder,
                onValueChange = { installFolder = it },
                label = { Text("Label") }
            )
            Button(
                onClick = {}
            ){
                Text("Install")
            }
        }
}
