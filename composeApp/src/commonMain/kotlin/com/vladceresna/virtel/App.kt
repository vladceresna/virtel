package com.vladceresna.virtel

import androidx.compose.material3.*
import androidx.compose.runtime.*
import org.jetbrains.compose.ui.tooling.preview.Preview


@Composable
@Preview
fun App() {
    MaterialTheme (){
        Application("vladceresna.virtel.launcher")
    }
}


/*
var page by remember { mutableStateOf(PageType.STARTUP) }
when(pe){
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
    else -> {}
}
*/
