package com.example

import android.Manifest
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.example.service.ClipVaultForegroundService
import com.example.ui.ClipVaultMainScreen
import com.example.ui.ClipVaultViewModel
import com.example.ui.theme.ClipVaultTheme

class MainActivity : ComponentActivity() {

    private val viewModel: ClipVaultViewModel by viewModels()
    private var clipboardListener: ClipboardManager.OnPrimaryClipChangedListener? = null

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            ClipVaultForegroundService.startService(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Setup real-time clipboard change listener for in-app copies
        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        clipboardListener = ClipboardManager.OnPrimaryClipChangedListener {
            viewModel.captureCurrentClipboard()
        }
        clipboardListener?.let {
            clipboardManager?.addPrimaryClipChangedListener(it)
        }

        // Request notification permission on Android 13+ if needed, then start foreground service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                ClipVaultForegroundService.startService(this)
            }
        } else {
            ClipVaultForegroundService.startService(this)
        }

        setContent {
            ClipVaultTheme {
                ClipVaultMainScreen(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Sync any clip copied while the app was in the background
        viewModel.syncClipboardOnResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        clipboardListener?.let {
            clipboardManager?.removePrimaryClipChangedListener(it)
        }
    }
}
