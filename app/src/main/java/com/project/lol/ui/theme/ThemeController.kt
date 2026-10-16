package com.project.lol.ui.theme

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object ThemeController {

    private const val PREFS_NAME = "spotilol_prefs"
    private const val KEY_AMOLED = "AmoledTheme"

    var amoled by mutableStateOf(false)
        private set

    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        if (prefs != null) return
        prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        amoled = prefs!!.getBoolean(KEY_AMOLED, false)
    }

    fun updateAmoled(value: Boolean) {
        amoled = value
        prefs?.edit()?.putBoolean(KEY_AMOLED, value)?.apply()
    }
}
