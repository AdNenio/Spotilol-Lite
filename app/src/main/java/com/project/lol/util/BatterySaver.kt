package com.project.lol.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.provider.Settings
import androidx.core.net.toUri

object BatterySaver {

    fun isExempt(context: Context): Boolean {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return pm.isIgnoringBatteryOptimizations(context.packageName)
    }

    fun shouldPrompt(context: Context): Boolean = !isExempt(context)

    @SuppressLint("BatteryLife")
    fun requestExemption(context: Context): Boolean {
        val app = context.applicationContext
        val intent = Intent(
            Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
            "package:${app.packageName}".toUri()
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return runCatching { app.startActivity(intent) }.isSuccess
    }

    fun openAppSettings(context: Context) {
        val app = context.applicationContext
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            "package:${app.packageName}".toUri()
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { app.startActivity(intent) }
    }
}