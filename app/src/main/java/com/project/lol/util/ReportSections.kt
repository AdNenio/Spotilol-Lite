package com.project.lol.util

import android.app.ActivityManager
import android.content.Context
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Debug
import android.os.Environment
import android.os.Process
import android.os.StatFs
import android.os.SystemClock
import android.telephony.TelephonyManager
import android.view.WindowManager
import androidx.webkit.WebViewCompat
import com.project.lol.BuildConfig
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.TimeZone

object ReportSections {

    private val timeFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)

    fun header(sb: StringBuilder, title: String, zone: TimeZone) {
        sb.appendLine(title)
        sb.appendLine("time: ${timeFmt.format(LocalDateTime.now())}  tz: ${zone.id} (${offsetLabel(zone)})")
        sb.appendLine()
    }

    fun app(sb: StringBuilder, context: Context) {
        sb.appendLine("[ app ]")
        sb.appendLine("package: ${context.packageName}")
        sb.appendLine("version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
        sb.appendLine("build: ${BuildConfig.BUILD_TYPE}  debuggable: ${BuildConfig.DEBUG}  id: ${BuildInfo.id}")
        sb.appendLine("pid: ${Process.myPid()}  threads: ${Thread.activeCount()}")
        sb.appendLine("uptime: ${uptimeLabel()}")
        runCatching {
            val info = context.packageManager.getPackageInfo(context.packageName, 0)
            sb.appendLine("installed: ${stamp(info.firstInstallTime)}")
            sb.appendLine("updated: ${stamp(info.lastUpdateTime)}")
        }
        sb.appendLine()
    }

    fun device(sb: StringBuilder) {
        sb.appendLine("[ device ]")
        sb.appendLine("model: ${Build.MANUFACTURER} ${Build.MODEL}")
        sb.appendLine("product: ${Build.PRODUCT}  device: ${Build.DEVICE}  board: ${Build.BOARD}")
        sb.appendLine("hardware: ${Build.HARDWARE}  brand: ${Build.BRAND}")
        sb.appendLine("abi: ${Build.SUPPORTED_ABIS.joinToString(", ")}")
        sb.appendLine("android: ${Build.VERSION.RELEASE} (sdk ${Build.VERSION.SDK_INT})")
        sb.appendLine("securityPatch: ${Build.VERSION.SECURITY_PATCH}")
        sb.appendLine("buildId: ${Build.DISPLAY} / ${Build.ID}")
        sb.appendLine("emulator: ${isEmulator()}")
        sb.appendLine()
    }

    fun display(sb: StringBuilder, context: Context) {
        sb.appendLine("[ display ]")
        runCatching {
            val dm = context.resources.displayMetrics
            sb.appendLine("resolution: ${dm.widthPixels}x${dm.heightPixels}")
            sb.appendLine(
                "sizeDp: ${(dm.widthPixels / dm.density).toInt()}x${(dm.heightPixels / dm.density).toInt()}dp"
            )
            sb.appendLine("density: ${dm.density} (${dm.densityDpi}dpi)")
            val orientation = if (context.resources.configuration.orientation ==
                Configuration.ORIENTATION_LANDSCAPE
            ) "landscape" else "portrait"
            sb.appendLine("orientation: $orientation")
        }
        runCatching {
            val refresh = displayRefreshRate(context)
            if (refresh != null) {
                sb.appendLine("refreshRate: ${String.format(Locale.US, "%.1f", refresh)}Hz")
            }
        }
        sb.appendLine()
    }

    fun region(sb: StringBuilder, context: Context, locale: Locale, zone: TimeZone) {
        sb.appendLine("[ region ]")
        sb.appendLine("locale: ${locale.toLanguageTag()} (${locale.displayName})")
        sb.appendLine("appLanguage: ${AppLocale.current(context)}")
        sb.appendLine("country: ${locale.country.ifBlank { "?" }} ${locale.displayCountry}")
        sb.appendLine("timezone: ${zone.id} (${offsetLabel(zone)})")
        runCatching {
            val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
            if (tm != null) {
                sb.appendLine("simCountry: ${tm.simCountryIso.ifBlank { "?" }}")
                sb.appendLine("networkCountry: ${tm.networkCountryIso.ifBlank { "?" }}")
                sb.appendLine("carrier: ${tm.simOperatorName.ifBlank { tm.networkOperatorName }}")
            }
        }
        sb.appendLine()
    }

    fun network(sb: StringBuilder, context: Context) {
        sb.appendLine("[ network ]")
        runCatching {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            val caps = cm?.activeNetwork?.let { cm.getNetworkCapabilities(it) }
            sb.appendLine("transport: ${transportLabel(caps)}")
            sb.appendLine("validated: ${caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true}")
            sb.appendLine("metered: ${cm?.isActiveNetworkMetered == true}")
            if (caps != null) {
                sb.appendLine("bandwidth: down=${caps.linkDownstreamBandwidthKbps}kbps up=${caps.linkUpstreamBandwidthKbps}kbps")
            }
        }
        sb.appendLine()
    }

    fun memory(sb: StringBuilder, context: Context) {
        sb.appendLine("[ memory ]")
        runCatching {
            val runtime = Runtime.getRuntime()
            sb.appendLine("heap: max=${mb(runtime.maxMemory())} used=${mb(runtime.totalMemory() - runtime.freeMemory())} free=${mb(runtime.freeMemory())}")
            sb.appendLine("nativeHeap: ${mb(Debug.getNativeHeapAllocatedSize())}")
        }
        runCatching {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            val info = ActivityManager.MemoryInfo()
            am?.getMemoryInfo(info)
            sb.appendLine("device: total=${mb(info.totalMem)} avail=${mb(info.availMem)} lowMemory=${info.lowMemory} threshold=${mb(info.threshold)}")
        }
        sb.appendLine()
    }

    fun storage(sb: StringBuilder, context: Context) {
        sb.appendLine("[ storage ]")
        runCatching {
            val stat = StatFs(context.filesDir.path)
            sb.appendLine("data: total=${gb(stat.blockCountLong * stat.blockSizeLong)} free=${gb(stat.availableBlocksLong * stat.blockSizeLong)}")
        }
        runCatching {
            @Suppress("DEPRECATION")
            val external = Environment.getExternalStorageDirectory()
            val stat = StatFs(external.path)
            sb.appendLine("external: total=${gb(stat.blockCountLong * stat.blockSizeLong)} free=${gb(stat.availableBlocksLong * stat.blockSizeLong)}")
        }
        sb.appendLine("filesDir: ${context.filesDir.absolutePath}")
        sb.appendLine()
    }

    fun webView(sb: StringBuilder, context: Context) {
        sb.appendLine("[ webview ]")
        runCatching {
            val pkg = WebViewCompat.getCurrentWebViewPackage(context)
            sb.appendLine("package: ${pkg?.packageName ?: "unknown"}")
            sb.appendLine("version: ${pkg?.versionName ?: "unknown"}")
        }
        sb.appendLine()
    }

    fun drm(sb: StringBuilder, context: Context) {
        sb.appendLine("[ drm ]")
        runCatching {
            sb.appendLine(PlaybackDiagnostics.probe(context).summary)
        }
        sb.appendLine()
    }

    fun session(sb: StringBuilder, context: Context) {
        val prefs = context.getSharedPreferences("spotilol_prefs", Context.MODE_PRIVATE)
        sb.appendLine("[ session ]")
        sb.appendLine("keys: ${prefs.all.size}")
        sb.appendLine("loggedIn: ${prefs.getBoolean("LoggedIn", false)}")
        sb.appendLine("serviceOn: ${prefs.getBoolean("ServiceOn", true)}")
        sb.appendLine("playerMode: ${prefs.getString("PlayerMode", "spotilol")}")
        sb.appendLine("aPlayMode: ${prefs.getString("APlayMode", "disabled")}")
        sb.appendLine("amoled: ${prefs.getBoolean("AmoledTheme", false)}")
        sb.appendLine("powerSave: ${prefs.getBoolean("PowerSave", false)}")
        sb.appendLine("blockServiceWorker: ${prefs.getBoolean("BlockServiceWorker", true)}")
        sb.appendLine("logging: ${Logger.isEnabled()}")
        sb.appendLine()
    }

    private fun stamp(epochMs: Long): String =
        timeFmt.format(Instant.ofEpochMilli(epochMs).atZone(ZoneId.systemDefault()))

    private fun transportLabel(caps: NetworkCapabilities?): String {
        if (caps == null) return "none"
        val types = ArrayList<String>()
        if (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) types.add("wifi")
        if (caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) types.add("cellular")
        if (caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) types.add("ethernet")
        if (caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) types.add("vpn")
        if (caps.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH)) types.add("bluetooth")
        return if (types.isEmpty()) "other" else types.joinToString("+")
    }

    private fun displayRefreshRate(context: Context): Float? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val direct = runCatching { context.display.refreshRate }.getOrNull()
            if (direct != null && direct > 0f) return direct
        }
        @Suppress("DEPRECATION")
        val fallback = (context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager)
            ?.defaultDisplay?.refreshRate
        return fallback?.takeIf { it > 0f }
    }

    private fun uptimeLabel(): String {
        val ms = SystemClock.elapsedRealtime()
        val hours = ms / 3_600_000
        val minutes = (ms % 3_600_000) / 60_000
        val seconds = (ms % 60_000) / 1000
        return "${hours}h ${minutes}m ${seconds}s"
    }

    private fun offsetLabel(zone: TimeZone): String {
        val total = zone.getOffset(System.currentTimeMillis()) / 60_000
        val sign = if (total < 0) "-" else "+"
        val abs = kotlin.math.abs(total)
        return "GMT$sign${abs / 60}:${(abs % 60).toString().padStart(2, '0')}"
    }

    private fun isEmulator(): Boolean =
        Build.FINGERPRINT.startsWith("generic") ||
            Build.FINGERPRINT.contains("vbox") ||
            Build.FINGERPRINT.contains("emulator") ||
            Build.MODEL.contains("Emulator") ||
            Build.HARDWARE.contains("goldfish") ||
            Build.HARDWARE.contains("ranchu")

    private fun mb(bytes: Long): String = "${bytes / 1024 / 1024}MB"

    private fun gb(bytes: Long): String =
        String.format(Locale.US, "%.2fGB", bytes.toDouble() / 1024 / 1024 / 1024)
}
