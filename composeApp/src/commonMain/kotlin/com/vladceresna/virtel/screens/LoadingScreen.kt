package com.vladceresna.virtel.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.Image
import coil3.compose.AsyncImage
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import virtel.composeapp.generated.resources.Res
import virtel.composeapp.generated.resources.virtel_logo

@Composable
fun LoadingScreen(){
    Column(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(20.dp),
        Arrangement.Center,
        Alignment.CenterHorizontally
    ) {
        val myIconPainter = painterResource(Res.drawable.virtel_logo)

        Row (
            modifier = Modifier.padding(16.dp),
            Arrangement.Center,
            Alignment.CenterVertically
        ) {
            Icon(
                tint = MaterialTheme.colorScheme.onBackground,
                painter = myIconPainter,
                contentDescription = "Virtel Logo",
                modifier = Modifier.padding(end = 8.dp).size(50.dp)
            )
            Text(
                text = "Virtel",
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp
            )
        }
    }
}