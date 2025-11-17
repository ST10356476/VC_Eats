package com.varsitycollege.vc_eats.utils

import android.content.Context
import android.content.SharedPreferences

object TokenManager {
    private const val PREFS_NAME = "app_prefs"
    private const val KEY_TOKEN = "auth_token"
    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)
}
