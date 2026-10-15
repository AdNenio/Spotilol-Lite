package com.project.lol.util

import android.content.Context
import android.util.Log
import com.project.lol.BuildConfig
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

enum class LogLevel(val letter: Char, val label: String, val priority: Int) {
    VERBOSE('V', "verbose", 2),
    DEBUG('D', "debug", 3),
    INFO('I', "info", 4),
    SYS('S', "sys", 4),
    WARN('W', "warn", 5),
    ERROR('E', "error", 6);

    companion object {
        fun of(letter: Char?): LogLevel? = entries.firstOrNull { it.letter == letter }

        fun parse(token: String): LogLevel? {
            val clean = token.trim().trim('*').lowercase()
            if (clean.isEmpty()) return null
            entries.firstOrNull { it.label == clean || it.letter.toString().lowercase() == clean }?.let { return it }
            return when (clean) {
                "d", "dbg" -> DEBUG
                "i", "information" -> INFO
                "s", "system" -> SYS
                "w", "warning" -> WARN
                "e", "err", "fatal" -> ERROR
                else -> null
            }
        }
    }
}

class LogEntry internal constructor(
    val id: Long,
    val timestamp: Long,
    val level: LogLevel,
    val tag: String,
    val message: String,
    val throwable: Throwable?
) {
    val time: String = Logger.timeOf(timestamp)
    val stack: String? = throwable?.stackTraceToString()?.let(LogRedactor::redact)

    fun line(): String {
        val builder = StringBuilder()
            .append(time)
            .append(' ')
            .append(level.letter)
            .append('/')
            .append(tag)
            .append(": ")
            .append(message)
        stack?.let { builder.append('\n').append(it) }
        return builder.toString()
    }
}

object Logger {
    const val PREFS = "spotilol_prefs"

    private const val PREF_KEY = "Logging"
    private const val LEGACY_PREF_KEY = "DebugOverlay"
    private const val PREF_KEY_AUTOSCROLL = "LoggerAutoScroll"
    private const val MAX_MESSAGE = 4000
    private const val MAX_ENTRIES = 4000
    private const val APP_TAG = "app"

    private val timeFmt: DateTimeFormatter =
        DateTimeFormatter.ofPattern("HH:mm:ss.SSS").withZone(ZoneId.systemDefault())

    private val enabledFlag = AtomicBoolean(BuildConfig.DEBUG)
    private val seq = AtomicLong()
    private val buf = ArrayDeque<LogEntry>()
    private val lock = Any()

    fun init(context: Context) {
        val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val stored = when {
            prefs.contains(PREF_KEY) -> prefs.getBoolean(PREF_KEY, BuildConfig.DEBUG)
            prefs.contains(LEGACY_PREF_KEY) -> prefs.getBoolean(LEGACY_PREF_KEY, BuildConfig.DEBUG)
            else -> BuildConfig.DEBUG
        }
        enabledFlag.set(stored)
        if (!prefs.contains(PREF_KEY)) prefs.edit().putBoolean(PREF_KEY, stored).apply()
    }

    fun isEnabled(): Boolean = enabledFlag.get()

    fun setEnabled(context: Context, enabled: Boolean) {
        enabledFlag.set(enabled)
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(PREF_KEY, enabled)
            .apply()
    }

    fun isAutoScroll(context: Context): Boolean =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(PREF_KEY_AUTOSCROLL, true)

    fun setAutoScroll(context: Context, enabled: Boolean) {
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(PREF_KEY_AUTOSCROLL, enabled)
            .apply()
    }

    fun timeOf(timestamp: Long): String = timeFmt.format(Instant.ofEpochMilli(timestamp))

    fun entries(): List<LogEntry> = synchronized(lock) { ArrayList(buf) }

    fun snapshot(): List<String> = synchronized(lock) { buf.map { it.line() } }

    fun count(): Int = synchronized(lock) { buf.size }

    fun clear() = synchronized(lock) { buf.clear() }

    fun log(level: LogLevel, tag: String, message: String?, throwable: Throwable? = null) {
        val enabled = enabledFlag.get()
        if (!enabled && level.priority < LogLevel.WARN.priority) return
        val safeTag = tag.ifBlank { APP_TAG }
        val text = LogRedactor.redact((message ?: throwable?.message ?: "").take(MAX_MESSAGE))
        if (enabled) {
            val entry = LogEntry(
                seq.incrementAndGet(),
                System.currentTimeMillis(),
                level,
                safeTag,
                text,
                throwable
            )
            synchronized(lock) {
                buf.addLast(entry)
                while (buf.size > MAX_ENTRIES) buf.removeFirst()
            }
        }
        val detail = throwable?.let { LogRedactor.redact(it.stackTraceToString()) }
        val console = if (detail.isNullOrBlank()) text else "$text\n$detail"
        when (level) {
            LogLevel.VERBOSE -> Log.v(safeTag, console)
            LogLevel.DEBUG -> Log.d(safeTag, console)
            LogLevel.INFO, LogLevel.SYS -> Log.i(safeTag, console)
            LogLevel.WARN -> Log.w(safeTag, console)
            LogLevel.ERROR -> Log.e(safeTag, console)
        }
    }

    fun v(tag: String, message: String, throwable: Throwable? = null) =
        log(LogLevel.VERBOSE, tag, message, throwable)

    fun d(tag: String, message: String, throwable: Throwable? = null) =
        log(LogLevel.DEBUG, tag, message, throwable)

    fun i(tag: String, message: String, throwable: Throwable? = null) =
        log(LogLevel.INFO, tag, message, throwable)

    fun s(tag: String, message: String, throwable: Throwable? = null) =
        log(LogLevel.SYS, tag, message, throwable)

    fun w(tag: String, message: String, throwable: Throwable? = null) =
        log(LogLevel.WARN, tag, message, throwable)

    fun e(tag: String, message: String, throwable: Throwable? = null) =
        log(LogLevel.ERROR, tag, message, throwable)

    fun js(level: String?, message: String?) {
        val msg = message ?: return
        val key = level.orEmpty().lowercase().trim()
        val mapped = when (key) {
            "v", "verbose" -> LogLevel.VERBOSE to "js.verbose"
            "i", "info" -> LogLevel.INFO to "js.info"
            "s", "sys", "system" -> LogLevel.SYS to "js.sys"
            "w", "warn", "warning" -> LogLevel.WARN to "js.warn"
            "e", "err", "error" -> LogLevel.ERROR to "js.err"
            else -> LogLevel.DEBUG to "js"
        }
        log(mapped.first, mapped.second, msg)
    }
}
