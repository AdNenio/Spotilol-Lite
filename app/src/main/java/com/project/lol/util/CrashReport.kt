package com.project.lol.util

import android.content.Context
import android.os.Build
import android.os.Looper
import java.util.Locale
import java.util.TimeZone

object CrashReport {

    fun build(context: Context, thread: Thread, throwable: Throwable, logs: List<String>): String {
        val sb = StringBuilder()
        val locale = Locale.getDefault()
        val zone = TimeZone.getDefault()

        ReportSections.header(sb, "spotilol crash report", zone)
        appendCrash(sb, thread, throwable)
        ReportSections.app(sb, context)
        ReportSections.device(sb)
        ReportSections.display(sb, context)
        ReportSections.region(sb, context, locale, zone)
        ReportSections.network(sb, context)
        ReportSections.memory(sb, context)
        ReportSections.storage(sb, context)
        ReportSections.webView(sb, context)
        ReportSections.drm(sb, context)
        ReportSections.session(sb, context)
        sb.appendLine()
        sb.appendLine("[ logs ] (${logs.size} lines)")
        if (logs.isEmpty()) {
            sb.appendLine("(empty)")
        } else {
            logs.forEach { sb.appendLine(it) }
        }
        return sb.toString()
    }

    @Suppress("DEPRECATION")
    private fun appendCrash(sb: StringBuilder, thread: Thread, throwable: Throwable) {
        val threadId = if (Build.VERSION.SDK_INT >= 36) thread.threadId() else thread.id
        sb.appendLine("[ crash ]")
        sb.appendLine("thread: ${thread.name} (id=$threadId, prio=${thread.priority})")
        sb.appendLine("onMainThread: ${Looper.myLooper() == Looper.getMainLooper()}")
        sb.appendLine("type: ${throwable.javaClass.name}")
        sb.appendLine("message: ${throwable.message ?: "none"}")
        var cause = throwable.cause
        var depth = 1
        while (cause != null && depth <= 6) {
            sb.appendLine("cause[$depth]: ${cause.javaClass.name}: ${cause.message ?: "none"}")
            cause = cause.cause
            depth++
        }
        sb.appendLine("suppressed: ${throwable.suppressed.size}")
        sb.appendLine()
        sb.appendLine("stackTrace:")
        sb.appendLine(throwable.stackTraceToString())
        sb.appendLine()
    }
}
