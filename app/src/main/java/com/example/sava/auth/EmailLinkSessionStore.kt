package com.example.sava.auth

import android.content.Context

object EmailLinkSessionStore {
    private const val PREFS_NAME = "email_link_session"
    private const val KEY_PENDING_EMAIL = "pending_email"
    private const val KEY_VERIFIED_EMAIL = "verified_email"
    private const val KEY_PENDING_FULL_NAME = "pending_full_name"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun savePendingEmail(context: Context, email: String) {
        prefs(context).edit().putString(KEY_PENDING_EMAIL, email).apply()
    }

    fun getPendingEmail(context: Context): String? {
        return prefs(context).getString(KEY_PENDING_EMAIL, null)
    }

    fun markVerifiedEmail(context: Context, email: String) {
        prefs(context).edit().putString(KEY_VERIFIED_EMAIL, email).apply()
    }

    fun getVerifiedEmail(context: Context): String? {
        return prefs(context).getString(KEY_VERIFIED_EMAIL, null)
    }

    fun savePendingFullName(context: Context, fullName: String) {
        prefs(context).edit().putString(KEY_PENDING_FULL_NAME, fullName).apply()
    }

    fun getPendingFullName(context: Context): String? {
        return prefs(context).getString(KEY_PENDING_FULL_NAME, null)
    }

    fun clearPendingFullName(context: Context) {
        prefs(context).edit().remove(KEY_PENDING_FULL_NAME).apply()
    }

    fun clearVerifiedEmail(context: Context) {
        prefs(context).edit().remove(KEY_VERIFIED_EMAIL).apply()
    }

    fun clearPendingEmail(context: Context) {
        prefs(context).edit().remove(KEY_PENDING_EMAIL).apply()
    }

    fun clearAll(context: Context) {
        prefs(context).edit()
            .remove(KEY_PENDING_EMAIL)
            .remove(KEY_VERIFIED_EMAIL)
            .remove(KEY_PENDING_FULL_NAME)
            .apply()
    }
}
