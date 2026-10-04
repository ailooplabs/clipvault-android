package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.ClipDatabase
import com.example.data.ClipRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ClipVaultForegroundService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var repository: ClipRepository? = null
    private var clipboardManager: ClipboardManager? = null
    private var clipListener: ClipboardManager.OnPrimaryClipChangedListener? = null
    private var lastCapturedText: String? = null

    companion object {
        const val CHANNEL_ID = "clipvault_bg_monitor_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_START = "ACTION_START_MONITOR"
        const val ACTION_STOP = "ACTION_STOP_MONITOR"
        private const val TAG = "ClipVaultFGService"

        var isForegroundServiceRunning: Boolean = false
            private set

        fun startService(context: Context) {
            val intent = Intent(context, ClipVaultForegroundService::class.java).apply {
                action = ACTION_START
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start foreground service", e)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, ClipVaultForegroundService::class.java).apply {
                action = ACTION_STOP
            }
            try {
                context.startService(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to stop foreground service", e)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        val database = ClipDatabase.getDatabase(applicationContext, serviceScope)
        repository = ClipRepository(database.clipDao())
        clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager

        createNotificationChannel()

        clipListener = ClipboardManager.OnPrimaryClipChangedListener {
            checkAndCaptureClipboard()
        }
        clipListener?.let {
            clipboardManager?.addPrimaryClipChangedListener(it)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            isForegroundServiceRunning = false
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        val notification = buildNotification(lastText = null)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        isForegroundServiceRunning = true
        checkAndCaptureClipboard()

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        isForegroundServiceRunning = false
        clipListener?.let {
            clipboardManager?.removePrimaryClipChangedListener(it)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

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
                        updateNotification("Captured: ${text.take(30)}...")
                    }
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "Clipboard check in FG service: ${e.message}")
        }
    }

    private fun updateNotification(subText: String) {
        try {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(NOTIFICATION_ID, buildNotification(subText))
        } catch (_: Exception) {}
    }

    private fun buildNotification(lastText: String?): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val content = lastText ?: "Active · Auto-capturing copied text into your vault"

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("ClipVault Background Monitor")
            .setContentText(content)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setShowWhen(false)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "ClipVault Background Monitor",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Monitors clipboard in the background to automatically save copied items."
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}
