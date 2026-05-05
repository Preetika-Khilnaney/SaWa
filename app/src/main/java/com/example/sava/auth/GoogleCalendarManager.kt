package com.example.sava.auth

import android.app.PendingIntent
import android.content.Intent
import androidx.activity.ComponentActivity
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URLEncoder
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

data class CalendarEventItem(
    val id: String,
    val title: String,
    val startDate: String,
    val startLabel: String,
    val meetLink: String? = null
)

data class MeetingRequest(
    val topic: String,
    val date: String,
    val startTime: String,
    val durationMinutes: Int = 30
)

data class ScheduledMeetingResult(
    val eventId: String,
    val meetLink: String?,
    val htmlLink: String?
)

object GoogleCalendarManager {
    private const val CALENDAR_SCOPE = "https://www.googleapis.com/auth/calendar"
    private const val ADMIN_EMAIL = "vkk2904@gmail.com"
    private const val EVENTS_URL = "https://www.googleapis.com/calendar/v3/calendars/primary/events"

    fun requestCalendarAccess(
        activity: ComponentActivity,
        onAuthorized: (String) -> Unit,
        onResolutionRequired: (PendingIntent) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val authorizationRequest = AuthorizationRequest.builder()
                .setRequestedScopes(listOf(Scope(CALENDAR_SCOPE)))
                .build()

            Identity.getAuthorizationClient(activity)
                .authorize(authorizationRequest)
                .addOnSuccessListener { authorizationResult ->
                    handleAuthorizationResult(
                        authorizationResult = authorizationResult,
                        onAuthorized = onAuthorized,
                        onResolutionRequired = onResolutionRequired,
                        onError = onError
                    )
                }
                .addOnFailureListener { exception ->
                    onError(exception.message ?: "Could not connect Google Calendar.")
                }
        } catch (exception: Throwable) {
            onError(exception.message ?: "Google Calendar authorization could not be started on this device.")
        }
    }

    fun resolveAuthorizationResult(
        activity: ComponentActivity,
        data: Intent?,
        onAuthorized: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val authorizationResult = Identity.getAuthorizationClient(activity)
                .getAuthorizationResultFromIntent(data)
            val accessToken = authorizationResult.accessToken
            if (accessToken.isNullOrBlank()) {
                onError("Google Calendar access was granted, but no access token was returned.")
            } else {
                onAuthorized(accessToken)
            }
        } catch (exception: ApiException) {
            onError(exception.message ?: "Could not finish Google Calendar authorization.")
        } catch (exception: Throwable) {
            onError(exception.message ?: "Google Calendar authorization returned an unexpected error.")
        }
    }

    suspend fun fetchUpcomingEvents(accessToken: String): Result<List<CalendarEventItem>> = withContext(Dispatchers.IO) {
        runCatching {
            val now = isoDateTimeFormatter().format(Calendar.getInstance().time)
            val encodedNow = URLEncoder.encode(now, "UTF-8")
            val url = URL("$EVENTS_URL?maxResults=20&singleEvents=true&orderBy=startTime&timeMin=$encodedNow")
            val response = executeRequest(
                url = url,
                method = "GET",
                accessToken = accessToken
            )
            parseEvents(response)
        }
    }

    suspend fun scheduleMeeting(accessToken: String, request: MeetingRequest): Result<ScheduledMeetingResult> = withContext(Dispatchers.IO) {
        runCatching {
            val startCalendar = buildStartCalendar(request.date, request.startTime)
            val endCalendar = (startCalendar.clone() as Calendar).apply {
                add(Calendar.MINUTE, request.durationMinutes)
            }
            val zoneId = TimeZone.getDefault().id

            val payload = JSONObject().apply {
                put("summary", request.topic.ifBlank { "Sava appointment with admin" })
                put("description", "Scheduled through Sava. App admin attendee: $ADMIN_EMAIL")
                put("start", JSONObject().apply {
                    put("dateTime", isoDateTimeFormatter().format(startCalendar.time))
                    put("timeZone", zoneId)
                })
                put("end", JSONObject().apply {
                    put("dateTime", isoDateTimeFormatter().format(endCalendar.time))
                    put("timeZone", zoneId)
                })
                put("attendees", JSONArray().put(JSONObject().apply {
                    put("email", ADMIN_EMAIL)
                }))
                put("conferenceData", JSONObject().apply {
                    put("createRequest", JSONObject().apply {
                        put("requestId", UUID.randomUUID().toString())
                        put("conferenceSolutionKey", JSONObject().apply {
                            put("type", "hangoutsMeet")
                        })
                    })
                })
            }

            val response = executeRequest(
                url = URL("$EVENTS_URL?conferenceDataVersion=1&sendUpdates=all"),
                method = "POST",
                accessToken = accessToken,
                body = payload.toString()
            )

            val json = JSONObject(response)
            val conferenceData = json.optJSONObject("conferenceData")
            val entryPoints = conferenceData?.optJSONArray("entryPoints")
            val meetLink = json.optString("hangoutLink").takeIf { it.isNotBlank() }
                ?: findVideoEntryPoint(entryPoints)

            ScheduledMeetingResult(
                eventId = json.optString("id"),
                meetLink = meetLink,
                htmlLink = json.optString("htmlLink").takeIf { it.isNotBlank() }
            )
        }
    }

    private fun handleAuthorizationResult(
        authorizationResult: AuthorizationResult,
        onAuthorized: (String) -> Unit,
        onResolutionRequired: (PendingIntent) -> Unit,
        onError: (String) -> Unit
    ) {
        when {
            authorizationResult.hasResolution() -> {
                val pendingIntent = authorizationResult.pendingIntent
                if (pendingIntent != null) {
                    onResolutionRequired(pendingIntent)
                } else {
                    onError("Google Calendar authorization needs approval, but the approval flow could not be opened.")
                }
            }

            authorizationResult.accessToken.isNullOrBlank() -> {
                onError("Google Calendar access token was not returned.")
            }

            else -> onAuthorized(authorizationResult.accessToken!!)
        }
    }

    private fun parseEvents(response: String): List<CalendarEventItem> {
        val json = JSONObject(response)
        val items = json.optJSONArray("items") ?: JSONArray()
        return buildList {
            for (index in 0 until items.length()) {
                val item = items.optJSONObject(index) ?: continue
                val title = item.optString("summary").ifBlank { "Untitled event" }
                val start = item.optJSONObject("start")
                val startValue = start?.optString("dateTime").takeIf { !it.isNullOrBlank() }
                    ?: start?.optString("date").orEmpty()
                add(
                    CalendarEventItem(
                        id = item.optString("id"),
                        title = title,
                        startDate = extractStartDate(startValue),
                        startLabel = formatEventStart(startValue),
                        meetLink = item.optString("hangoutLink").takeIf { it.isNotBlank() }
                    )
                )
            }
        }
    }

    private fun formatEventStart(value: String): String {
        return runCatching {
            if (value.contains("T")) {
                val parsed = isoDateTimeParser().parse(value)
                displayDateTimeFormatter().format(parsed!!)
            } else {
                val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(value)
                SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(parsed!!)
            }
        }.getOrDefault(value)
    }

    private fun extractStartDate(value: String): String {
        return if (value.contains("T")) value.substringBefore("T") else value
    }

    private fun buildStartCalendar(date: String, timeInput: String): Calendar {
        val parsedDate = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            .parse("$date $timeInput")
            ?: throw IllegalArgumentException("Please enter the date as YYYY-MM-DD and time as HH:MM.")
        return Calendar.getInstance().apply {
            this.time = parsedDate
        }
    }

    private fun findVideoEntryPoint(entryPoints: JSONArray?): String? {
        if (entryPoints == null) return null
        for (index in 0 until entryPoints.length()) {
            val entry = entryPoints.optJSONObject(index) ?: continue
            if (entry.optString("entryPointType") == "video") {
                return entry.optString("uri").takeIf { it.isNotBlank() }
            }
        }
        return null
    }

    private fun executeRequest(
        url: URL,
        method: String,
        accessToken: String,
        body: String? = null
    ): String {
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = method
            setRequestProperty("Authorization", "Bearer $accessToken")
            setRequestProperty("Accept", "application/json")
            if (body != null) {
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
            }
        }

        body?.let {
            OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
                writer.write(it)
            }
        }

        val responseCode = connection.responseCode
        val stream = if (responseCode in 200..299) connection.inputStream else connection.errorStream
        val text = BufferedReader(stream.reader()).use { reader ->
            reader.readText()
        }

        if (responseCode !in 200..299) {
            throw IllegalStateException(extractErrorMessage(text))
        }

        return text
    }

    private fun extractErrorMessage(response: String): String {
        return runCatching {
            val error = JSONObject(response).optJSONObject("error")
            error?.optString("message")
        }.getOrNull().takeIf { !it.isNullOrBlank() }
            ?: "Google Calendar request failed."
    }

    private fun isoDateTimeFormatter(): SimpleDateFormat {
        return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US).apply {
            timeZone = TimeZone.getDefault()
        }
    }

    private fun isoDateTimeParser(): SimpleDateFormat {
        return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US)
    }

    private fun displayDateTimeFormatter(): SimpleDateFormat {
        return SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    }
}
