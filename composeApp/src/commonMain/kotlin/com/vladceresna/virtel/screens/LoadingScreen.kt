package com.vladceresna.virtel.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun LoadingScreen(){
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
            text = "by Vlad Ceresna",
            fontSize = 25.sp,
            fontWeight = FontWeight.Black,
            color = Color.Green
        )
    }
}