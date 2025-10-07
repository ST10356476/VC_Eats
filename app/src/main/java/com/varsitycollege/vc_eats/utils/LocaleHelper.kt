package com.varsitycollege.vc_eats.utils

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.*

object LocaleHelper {

    private const val SELECTED_LANGUAGE = "language"

    fun setLocale(context: Context, languageCode: String): Context {
        persist(context, languageCode)
        return updateResources(context, languageCode)
    }

    fun getLanguage(context: Context): String {
        val prefs = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        return prefs.getString(SELECTED_LANGUAGE, "en") ?: "en"
    }

    private fun persist(context: Context, languageCode: String) {
        val prefs = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        prefs.edit().putString(SELECTED_LANGUAGE, languageCode).apply()
    }

    private fun updateResources(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.createConfigurationContext(configuration)
        } else {
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
            context
        }
    }

    fun getLanguageCode(language: String): String {
        return when (language) {
            "English" -> "en"
            "Afrikaans" -> "af"
            "Zulu" -> "zu"
            "Xhosa" -> "xh"
            else -> "en"
        }
    }

    fun getLanguageName(languageCode: String): String {
        return when (languageCode) {
            "en" -> "English"
            "af" -> "Afrikaans"
            "zu" -> "Zulu"
            "xh" -> "Xhosa"
            else -> "English"
        }
    }
}