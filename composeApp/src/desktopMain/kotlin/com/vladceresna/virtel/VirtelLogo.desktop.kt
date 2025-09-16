package com.vladceresna.virtel

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.vectorResource
import virtel.composeapp.generated.resources.Res
import virtel.composeapp.generated.resources.virtel_logo

@Composable
actual fun VirtelLogo() {

    val myIconPainter = painterResource(Res.drawable.virtel_logo)
    Icon(
        tint = MaterialTheme.colorScheme.onBackground,
        painter = myIconPainter,
        contentDescription = "Virtel Logo",
        modifier = Modifier
            .size(50.dp)
    )
}