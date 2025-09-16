package com.vladceresna.virtel

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import virtel.composeapp.generated.resources.Res
import virtel.composeapp.generated.resources.virtel_logo_png

@Composable
actual fun VirtelLogo() {
    var logo = painterResource(Res.drawable.virtel_logo_png)
    Icon(
        tint = MaterialTheme.colorScheme.onBackground,
        painter = logo,
        contentDescription = "Virtel Logo",
        modifier = Modifier
            .size(50.dp)
    )
}