package com.project.lol.util

import android.content.Context
import android.net.Uri
import android.os.Build
import com.project.lol.BuildConfig
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object LogExport {

    const val MIME_TYPE = "text/plain"
    private const val PREFIX = "spotilol-log"

    private val fileStamp = SimpleDateFormat("yyyy-MM-dd_HHmmss", Locale.US)
    private val humanStamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    private val unsafe = Regex("[^A-Za-z0-9._-]")

    fun fileName(): String {
        val version = BuildConfig.VERSION_NAME.replace(unsafe, "-")
        return "${PREFIX}_${version}_${fileStamp.format(Date())}.txt"
    }

    fun payload(entries: List<LogEntry>, total: Int, query: String, minLevel: LogLevel?): String {
        val zone = TimeZone.getDefault()
        val level = minLevel ?: LogLevel.VERBOSE
        val builder = StringBuilder()
        builder.appendLine("# spotilol log export")
        builder.appendLine("# app: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE}) ${BuildConfig.BUILD_TYPE}")
        builder.appendLine("# build: ${BuildInfo.id}")
        builder.appendLine(
            "# device: ${Build.MANUFACTURER} ${Build.MODEL} / Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
        )
        builder.appendLine("# exported: ${humanStamp.format(Date())} ${zone.getDisplayName(false, TimeZone.SHORT)}")
        builder.appendLine("# filter: level >= ${level.label}, query=\"${query.trim()}\"")
        builder.appendLine("# entries: ${entries.size} of $total")
        builder.appendLine()
        builder.append(LogFilter.text(entries))
        return builder.toString()
    }

    fun write(context: Context, uri: Uri, text: String) {
        val stream = context.contentResolver.openOutputStream(uri, "wt")
            ?: throw IllegalStateException("cannot open the destination file")
        stream.use { it.write(text.toByteArray(Charsets.UTF_8)) }
    }
}
