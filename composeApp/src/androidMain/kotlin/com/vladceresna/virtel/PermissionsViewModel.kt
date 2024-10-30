package com.vladceresna.virtel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.icerock.moko.permissions.DeniedAlwaysException
import dev.icerock.moko.permissions.DeniedException
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionState
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.RequestCanceledException
import kotlinx.coroutines.launch


class PermissionsViewModel(
    private val controller: PermissionsController
): ViewModel() {

    var recordAudioState by mutableStateOf(PermissionState.NotDetermined)
        private set
    var storageState by mutableStateOf(PermissionState.NotDetermined)
        private set

    init {
        viewModelScope.launch {
            recordAudioState = controller.getPermissionState(Permission.RECORD_AUDIO)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                storageState = PermissionState.Granted
            } else {
                storageState = controller.getPermissionState(Permission.WRITE_STORAGE)
            }
        }
    }

    fun provideOrRequestPermissions() {
        viewModelScope.launch {
            try {
                controller.providePermission(Permission.RECORD_AUDIO)
                recordAudioState = PermissionState.Granted
            } catch(e: DeniedAlwaysException) {
                recordAudioState = PermissionState.DeniedAlways
            } catch(e: DeniedException) {
                recordAudioState = PermissionState.Denied
            } catch(e: RequestCanceledException) {
                e.printStackTrace()
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q){
                storageState = PermissionState.Granted
            } else{
                try {
                    controller.providePermission(Permission.WRITE_STORAGE)
                    storageState = PermissionState.Granted
                } catch(e: DeniedAlwaysException) {
                    storageState = PermissionState.DeniedAlways
                } catch(e: DeniedException) {
                    storageState = PermissionState.Denied
                } catch(e: RequestCanceledException) {
                    e.printStackTrace()
                }
            }

        }
    }
}