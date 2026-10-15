package com.project.lol.util

import android.content.Context
import java.io.File
import java.util.Locale
import androidx.core.content.edit

object InnertubeCachePrefs {

    private const val PREFS = "spotilol_prefs"
    private const val KEY = "InnertubeCacheMb"

    const val UNLIMITED = 0
    const val DEFAULT_MB = 50

    val OPTIONS_MB = listOf(16, 32, 50, 64, 128, 256, 512, 1024, 2048, UNLIMITED)

    fun mb(context: Context): Int {
        val value = context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getInt(KEY, DEFAULT_MB)
        return if (value == UNLIMITED || value > 0) value else DEFAULT_MB
    }

    fun setMb(context: Context, mb: Int) {
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit {
                putInt(KEY, if (mb > 0) mb else UNLIMITED)
            }
    }

    fun maxBytes(context: Context): Long {
        val mb = mb(context)
        return if (mb == UNLIMITED) Long.MAX_VALUE else mb * 1024L * 1024L
    }

    fun dir(context: Context): File = File(context.applicationContext.cacheDir, "innertube_http")

    fun usedBytes(context: Context): Long = dirSize(dir(context))

    fun formatBytes(bytes: Long): String = when {
        bytes >= 1L shl 30 -> String.format(Locale.US, "%.1f GB", bytes / 1073741824.0)
        bytes >= 1L shl 20 -> String.format(Locale.US, "%.1f MB", bytes / 1048576.0)
        bytes >= 1L shl 10 -> "${bytes / 1024} KB"
        else -> "$bytes B"
    }

    private fun dirSize(file: File): Long {
        if (!file.exists()) return 0L
        if (file.isFile) return file.length()
        var total = 0L
        val children = file.listFiles() ?: return 0L
        for (child in children) total += dirSize(child)
        return total
    }
}