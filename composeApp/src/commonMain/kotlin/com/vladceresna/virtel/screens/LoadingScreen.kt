package com.vladceresna.virtel.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.vladceresna.virtel.controllers.VirtelSystem

@Composable
fun LoadingScreen(){
    Column(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        Arrangement.Center,
        Alignment.CenterHorizontally
    ) {
        Text(
            text = "Virtel",
            fontSize = 100.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "By Vlad Ĉereŝna",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.hsv(135f, 0.8f,0.3f)
        )
    }
}