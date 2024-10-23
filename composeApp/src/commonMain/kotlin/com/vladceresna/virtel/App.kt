package com.vladceresna.virtel

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.compose.AppTheme
import com.vladceresna.virtel.screens.LoadingScreen
import com.vladceresna.virtel.screens.VirtelScreen
import com.vladceresna.virtel.controllers.VirtelSystem
import dev.icerock.moko.permissions.PermissionState
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App(theme: Boolean = isSystemInDarkTheme()) {
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
            VirtelApp(theme = theme)
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

@Composable
fun VirtelApp(theme: Boolean){
    var darkTheme by remember { mutableStateOf(theme) }
    var rd by remember { mutableStateOf(true) }
    val virtelSystem = VirtelSystem
    virtelSystem.renderFunction = { rd = !rd }
    LaunchedEffect(Unit){
        virtelSystem.renderFunction()
        //after first recomposition only
        CoroutineScope(Job()).launch {
            virtelSystem.start()
            virtelSystem.renderFunction()
        }
    }
    LaunchedEffect(rd) {
        virtelSystem.renderFunction()
        //after recomposition
    }
    AppTheme(rd){
        rd
        if (virtelSystem.isLoading) {
            LoadingScreen(rd)
        } else {
            VirtelScreen(rd)
        }
    }
}