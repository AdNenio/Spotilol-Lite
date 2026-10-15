package com.project.lol.util

import android.content.Context
import java.util.Locale
import java.util.TimeZone

object DeviceInfo {

    fun build(context: Context): String {
        val app = context.applicationContext
        val sb = StringBuilder()
        val locale = Locale.getDefault()
        val zone = TimeZone.getDefault()

        ReportSections.header(sb, "spotilol device report", zone)
        ReportSections.app(sb, app)
        ReportSections.device(sb)
        ReportSections.display(sb, context)
        ReportSections.region(sb, app, locale, zone)
        ReportSections.network(sb, app)
        ReportSections.memory(sb, app)
        ReportSections.storage(sb, app)
        ReportSections.webView(sb, app)
        ReportSections.drm(sb, app)
        ReportSections.session(sb, app)
        return sb.toString().trimEnd()
    }
}
