package com.example.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            val prefs = context.getSharedPreferences("clipvault_prefs", Context.MODE_PRIVATE)
            val bgEnabled = prefs.getBoolean("bg_monitor_enabled", true)
            if (bgEnabled) {
                ClipVaultForegroundService.startService(context)
            }
        }
    }
}
