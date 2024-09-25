package com.vladceresna.virtel

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.ui.tooling.preview.Preview

enum class PageType {
    STARTUP,
    INSTALLING,

}


@Composable
@Preview
fun App() {
    MaterialTheme {
        var page by remember { mutableStateOf(PageType.STARTUP) }
        when(page){
            PageType.STARTUP -> {
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
                        text = "made by\nVlad Ceresna",
                        modifier = Modifier.padding(20.dp),
                        fontWeight = FontWeight(500),
                        color = Color(0,200,0,255),
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center
                    )
                    Button(onClick = {page = PageType.INSTALLING}){
                        Text("Install")
                    }
                }
            }
            PageType.INSTALLING -> {
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
                    OutlinedTextField(
                        value = installFolder,
                        onValueChange = { installFolder = it },
                        label = { Text("Label") }
                    )
                    Button(
                        onClick = {
                            
                        }
                    ){
                        Text("Install")
                    }
                }
            }
            else -> {}
        }

    }
}
