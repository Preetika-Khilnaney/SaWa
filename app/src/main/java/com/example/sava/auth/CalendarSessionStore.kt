package com.example.sava.auth

import android.content.Context

object CalendarSessionStore {
    private const val PREFS_NAME = "calendar_session"
    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_IS_CONNECTED = "is_connected"

    fun getAccessToken(context: Context): String? {
        return prefs(context).getString(KEY_ACCESS_TOKEN, null)
    }

    fun isConnected(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_IS_CONNECTED, false) && !getAccessToken(context).isNullOrBlank()
    }

    fun saveConnection(context: Context, accessToken: String) {
        prefs(context).edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putBoolean(KEY_IS_CONNECTED, true)
            .apply()
    }

    fun clearConnection(context: Context) {
        prefs(context).edit()
            .remove(KEY_ACCESS_TOKEN)
            .putBoolean(KEY_IS_CONNECTED, false)
            .apply()
    }

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
