package com.vladceresna.virtel

import android.os.Build
import android.os.Bundle
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vladceresna.virtel.controllers.VirtelSystem
import dev.icerock.moko.permissions.PermissionState
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory


class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let {
                it.hide(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE)
            }
        } else {
            window.decorView.systemUiVisibility = (android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or android.view.View.SYSTEM_UI_FLAG_FULLSCREEN)
        }

        VirtelSystem.applicationContext = applicationContext
        VirtelContext.context = applicationContext
        setContent {
            VirtelApp()
        }

    }
    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let {
                it.hide(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE)
            }
        } else {
            window.decorView.systemUiVisibility = (android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or android.view.View.SYSTEM_UI_FLAG_FULLSCREEN)
        }
    }

    override fun onRestart() {
        super.onRestart()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let {
                it.hide(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE)
            }
        } else {
            window.decorView.systemUiVisibility = (android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or android.view.View.SYSTEM_UI_FLAG_FULLSCREEN)
        }
    }
}


@Composable
@Preview
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
        if ((viewModel.recordAudioState == PermissionState.Granted &&
            viewModel.storageState == PermissionState.Granted) ||
            android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
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
            Text("Permission was permanently declined.",Modifier.padding(0.dp,20.dp))
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



/*
@Preview
@Composable
fun SoraPreview(
    viewModel: SoraViewModel = viewModel()
){
    Column {
        CodeEditor(
            modifier = Modifier
                .fillMaxSize(),
            state = viewModel.editorState
        )
    }
}

class SoraViewModel : ViewModel() {
    val editorState by mutableStateOf(
        CodeEditorState()
    )
}*/
