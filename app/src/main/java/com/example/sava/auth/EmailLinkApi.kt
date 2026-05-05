package com.example.sava.auth

import com.example.sava.BuildConfig
import org.json.JSONObject
import java.io.BufferedReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object EmailLinkApi {
    fun sendSignInLink(email: String, fullName: String): Result<Unit> {
        return runCatching {
            val baseUrl = BuildConfig.EMAIL_API_BASE_URL.trimEnd('/')
            val url = URL("$baseUrl/api/send-signin-link")
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 15000
                readTimeout = 15000
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                setRequestProperty("Accept", "application/json")
            }

            val payload = JSONObject()
                .put("email", email)
                .put("fullName", fullName)
                .toString()

            OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
                writer.write(payload)
                writer.flush()
            }

            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                val errorMessage = connection.errorStream?.bufferedReader()?.use(BufferedReader::readText)
                throw IllegalStateException(errorMessage ?: "Email request failed with code $responseCode")
            }

            connection.disconnect()
        }
    }
}
