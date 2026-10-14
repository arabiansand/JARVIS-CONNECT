package com.example.transport

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * Helper utility to manage and check Android 12+ (API 31+) runtime Bluetooth permissions:
 * - BLUETOOTH_SCAN: Discover nearby Bluetooth devices without requiring location
 * - BLUETOOTH_CONNECT: Connect to paired or detected Bluetooth devices
 * - BLUETOOTH_ADVERTISE: Broadcast local node presence over Bluetooth
 *
 * Also provides backward-compatible fallback for Android 11 and lower.
 */
object BluetoothPermissionHelper {

    /**
     * Modern Android 12+ (API 31+) Bluetooth permissions
     */
    @RequiresApi(Build.VERSION_CODES.S)
    val ANDROID_12_BLUETOOTH_PERMISSIONS = arrayOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_ADVERTISE
    )

    /**
     * Legacy permissions for Android 11 and lower
     */
    val LEGACY_BLUETOOTH_PERMISSIONS = arrayOf(
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    /**
     * Returns the appropriate list of permissions needed for the current Android OS version.
     */
    fun getRequiredPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_ADVERTISE
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }

    /**
     * Checks whether all required Bluetooth permissions are granted.
     */
    fun hasAllBluetoothPermissions(context: Context): Boolean {
        return getRequiredPermissions().all { perm ->
            ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Checks if BLUETOOTH_SCAN (or legacy Location) is granted.
     */
    fun hasScanPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Checks if BLUETOOTH_CONNECT (or legacy Bluetooth) is granted.
     */
    fun hasConnectPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Checks if BLUETOOTH_ADVERTISE (or legacy Bluetooth Admin) is granted.
     */
    fun hasAdvertisePermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_ADVERTISE
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_ADMIN
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Returns a list of required permissions that have not yet been granted.
     */
    fun getMissingPermissions(context: Context): List<String> {
        return getRequiredPermissions().filter { perm ->
            ContextCompat.checkSelfPermission(context, perm) != PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Checks if UI rationale should be shown for any of the missing Bluetooth permissions.
     */
    fun shouldShowRationale(activity: Activity): Boolean {
        return getRequiredPermissions().any { perm ->
            ActivityCompat.shouldShowRequestPermissionRationale(activity, perm)
        }
    }

    /**
     * Opens the device Application Details Settings page so user can manually toggle permissions if permanently denied.
     */
    fun openAppSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    /**
     * Evaluates whether Bluetooth discovery can be safely initiated.
     */
    fun checkDiscoveryReadiness(context: Context, isBluetoothEnabled: Boolean): DiscoveryPermissionResult {
        val missing = getMissingPermissions(context)
        if (missing.isNotEmpty()) {
            return DiscoveryPermissionResult.MissingPermissions(missing)
        }
        if (!isBluetoothEnabled) {
            return DiscoveryPermissionResult.BluetoothDisabled
        }
        return DiscoveryPermissionResult.Ready
    }
}

/**
 * Result of checking whether Bluetooth discovery can proceed.
 */
sealed class DiscoveryPermissionResult {
    object Ready : DiscoveryPermissionResult()
    object BluetoothDisabled : DiscoveryPermissionResult()
    data class MissingPermissions(val missing: List<String>) : DiscoveryPermissionResult()
}

/**
 * Jetpack Compose state holder and launcher for Bluetooth permissions.
 */
class BluetoothPermissionLauncher(
    private val launcher: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>,
    private val context: Context,
    val onAllGranted: () -> Unit,
    val onDenied: (List<String>) -> Unit
) {
    val isGranted: Boolean
        get() = BluetoothPermissionHelper.hasAllBluetoothPermissions(context)

    fun launch() {
        val missing = BluetoothPermissionHelper.getMissingPermissions(context)
        if (missing.isEmpty()) {
            onAllGranted()
        } else {
            launcher.launch(missing.toTypedArray())
        }
    }
}

/**
 * Convenient Compose hook to remember a BluetoothPermissionLauncher.
 */
@Composable
fun rememberBluetoothPermissionLauncher(
    context: Context,
    onAllGranted: () -> Unit,
    onDenied: (List<String>) -> Unit = {}
): BluetoothPermissionLauncher {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val ungranted = results.filterValues { !it }.keys.toList()
        if (ungranted.isEmpty()) {
            onAllGranted()
        } else {
            onDenied(ungranted)
        }
    }

    return remember(launcher, context) {
        BluetoothPermissionLauncher(
            launcher = launcher,
            context = context,
            onAllGranted = onAllGranted,
            onDenied = onDenied
        )
    }
}
