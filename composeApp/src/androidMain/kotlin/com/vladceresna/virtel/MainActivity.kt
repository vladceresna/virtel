package com.vladceresna.virtel

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vladceresna.virtel.controllers.VirtelSystem
import dev.icerock.moko.permissions.PermissionState
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory


class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        VirtelSystem.applicationContext = applicationContext
        setContent {
            VirtelApp()
        }

    }
}

@Composable
@org.jetbrains.compose.ui.tooling.preview.Preview
fun VirtelApp() {
    val factory = rememberPermissionsControllerFactory()
    val controller = remember(factory) {
        factory.createPermissionsController()
    }
    BindEffect(controller)
    val viewModel = viewModel {
        PermissionsViewModel(controller)
    }

    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (viewModel.recordAudioState == PermissionState.Granted &&
            viewModel.storageState == PermissionState.Granted) {
            App()
        } else if (viewModel.recordAudioState == PermissionState.DeniedAlways ||
            viewModel.storageState == PermissionState.DeniedAlways) {
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
            Text("Permission was permanently declined.")
            Button(onClick = {
                controller.openAppSettings()
            }) {
                Text("Open app settings")
            }
        } else {
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
            Button(
                onClick = {
                    viewModel.provideOrRequestPermissions()
                }
            ) {
                Text("Give permissions")
            }
        }
    }
}




@Preview
@Composable
fun AppAndroidPreview() {
    VirtelApp()
}