package com.example.service

import android.accessibilityservice.AccessibilityService
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.example.data.ClipDatabase
import com.example.data.ClipRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ClipVaultAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var repository: ClipRepository? = null
    private var clipboardManager: ClipboardManager? = null
    private var clipListener: ClipboardManager.OnPrimaryClipChangedListener? = null
    private var lastCapturedText: String? = null

    companion object {
        private const val TAG = "ClipVaultA11yService"
        var isServiceRunning: Boolean = false
            private set
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        isServiceRunning = true
        Log.d(TAG, "ClipVaultAccessibilityService connected and active")

        try {
            val database = ClipDatabase.getDatabase(applicationContext, serviceScope)
            repository = ClipRepository(database.clipDao())
            clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager

            clipListener = ClipboardManager.OnPrimaryClipChangedListener {
                checkAndCaptureClipboard()
            }
            clipListener?.let {
                clipboardManager?.addPrimaryClipChangedListener(it)
            }

            // Initial capture on connect
            checkAndCaptureClipboard()
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing AccessibilityService", e)
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        // Whenever an event occurs (e.g., text selection, click on 'Copy', window change)
        val eventType = event.eventType
        if (eventType == AccessibilityEvent.TYPE_VIEW_CLICKED ||
            eventType == AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED ||
            eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
            eventType == AccessibilityEvent.TYPE_VIEW_LONG_CLICKED ||
            eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
        ) {
            checkAndCaptureClipboard()
        }
    }

    override fun onInterrupt() {
        Log.w(TAG, "ClipVaultAccessibilityService interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
        clipListener?.let {
            clipboardManager?.removePrimaryClipChangedListener(it)
        }
    }

    private fun checkAndCaptureClipboard() {
        try {
            val cm = clipboardManager ?: return
            val clip = cm.primaryClip ?: return
            if (clip.itemCount > 0) {
                val item = clip.getItemAt(0)
                val text = item?.text?.toString()?.trim()
                if (!text.isNullOrBlank() && text != lastCapturedText) {
                    lastCapturedText = text
                    val repo = repository ?: return
                    serviceScope.launch {
                        repo.insert(text)
                        Log.d(TAG, "Successfully auto-captured clip in background: ${text.take(30)}...")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Could not read clipboard", e)
        }
    }
}
