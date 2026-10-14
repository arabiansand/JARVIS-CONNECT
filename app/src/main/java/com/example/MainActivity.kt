package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.ui.JarvisAppScaffold
import com.example.ui.JarvisMainViewModel
import com.example.ui.theme.JarvisTheme

class MainActivity : ComponentActivity() {

    private val viewModel: JarvisMainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val isDarkTheme by viewModel.isDarkTheme.collectAsState()

            // Runtime Permissions Request Launcher using BluetoothPermissionHelper
            val permissionsToRequest = buildList {
                addAll(com.example.transport.BluetoothPermissionHelper.getRequiredPermissions())
                add(Manifest.permission.RECORD_AUDIO)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    add(Manifest.permission.NEARBY_WIFI_DEVICES)
                    add(Manifest.permission.POST_NOTIFICATIONS)
                }
            }

            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestMultiplePermissions()
            ) { results ->
                // Permissions handled gracefully; fallback and simulated modes available
            }

            JarvisTheme(darkTheme = isDarkTheme) {
                JarvisAppScaffold(
                    viewModel = viewModel,
                    onRequestPermissions = {
                        permissionLauncher.launch(permissionsToRequest.toTypedArray())
                    }
                )
            }
        }
    }
}
