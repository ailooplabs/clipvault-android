package com.example

import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.ClipVaultMainScreen
import com.example.ui.ClipVaultViewModel
import com.example.ui.theme.ClipVaultTheme

class MainActivity : ComponentActivity() {

    private val viewModel: ClipVaultViewModel by viewModels()
    private var clipboardListener: ClipboardManager.OnPrimaryClipChangedListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Setup real-time clipboard change listener
        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        clipboardListener = ClipboardManager.OnPrimaryClipChangedListener {
            viewModel.captureCurrentClipboard()
        }
        clipboardListener?.let {
            clipboardManager?.addPrimaryClipChangedListener(it)
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
