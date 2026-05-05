package com.example.sava.ui.screens

import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import android.content.IntentSender
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sava.auth.CalendarEventItem
import com.example.sava.auth.CalendarSessionStore
import com.example.sava.auth.GoogleCalendarManager
import com.example.sava.auth.MeetingRequest
import com.example.sava.ui.theme.ChampagneGold
import com.example.sava.ui.theme.DeepCharcoal
import com.example.sava.ui.theme.OffWhite
import com.example.sava.ui.theme.RefinedSerif
import com.example.sava.ui.theme.adaptive
import com.example.sava.ui.theme.rememberAdaptiveUi
import com.example.sava.ui.theme.AdaptiveUi
import kotlinx.coroutines.launch
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private fun Context.findActivity(): ComponentActivity? {
    var current: Context? = this
    while (current is ContextWrapper) {
        if (current is ComponentActivity) return current
        current = current.baseContext
    }
    return null
}

private enum class CalendarAction {
    LoadEvents,
    ScheduleMeeting
}

private enum class EventCategory {
    SavaAppointment,
    CalendarEvent
}

private data class CalendarUiEvent(
    val id: String,
    val title: String,
    val startDate: String,
    val startLabel: String,
    val meetLink: String?,
    val category: EventCategory
)

private data class CalendarDayCell(
    val year: Int,
    val month: Int,
    val day: Int,
    val inCurrentMonth: Boolean
) {
    val key: String = "%04d-%02d-%02d".format(year, month + 1, day)
}

@Composable
fun CalendarScreen(
    openScheduleOnStart: Boolean = false,
    onCloseRequest: () -> Unit = {}
) {
    val adaptiveUi = rememberAdaptiveUi()
    val context = LocalContext.current
    val appContext = context.applicationContext
    val activity = context.findActivity()
    val scope = rememberCoroutineScope()
    val todayCalendar = remember { Calendar.getInstance() }

    var accessToken by remember { mutableStateOf(CalendarSessionStore.getAccessToken(appContext)) }
    var pendingAction by remember { mutableStateOf<CalendarAction?>(null) }
    var events by remember { mutableStateOf<List<CalendarEventItem>>(emptyList()) }
    var statusMessage by remember { mutableStateOf("Connect your Google Calendar to explore upcoming events, appointments, and Meet schedules in one place.") }
    var topic by remember { mutableStateOf("Sava appointment with admin") }
    var date by remember { mutableStateOf(formatCalendarDate(todayCalendar)) }
    var startTime by remember { mutableStateOf("10:00") }
    var durationMinutes by remember { mutableStateOf("30") }
    var latestMeetLink by remember { mutableStateOf<String?>(null) }
    var showingAppointmentsList by remember { mutableStateOf(false) }
    var visibleMonth by remember { mutableStateOf(todayCalendar.get(Calendar.MONTH)) }
    var visibleYear by remember { mutableStateOf(todayCalendar.get(Calendar.YEAR)) }
    var selectedDateKey by remember { mutableStateOf(formatCalendarDate(todayCalendar)) }
    var showScheduleForm by remember(openScheduleOnStart) { mutableStateOf(openScheduleOnStart) }
    var isConnected by remember { mutableStateOf(CalendarSessionStore.isConnected(appContext)) }

    val uiEvents = remember(events) {
        events.map { event ->
            CalendarUiEvent(
                id = event.id,
                title = event.title,
                startDate = event.startDate,
                startLabel = event.startLabel,
                meetLink = event.meetLink,
                category = categorizeEvent(event)
            )
        }
    }

    val appointments = remember(uiEvents) {
        uiEvents.filter { it.category == EventCategory.SavaAppointment }
    }

    val eventsByDate = remember(uiEvents) {
        uiEvents.groupBy { it.startDate }
    }

    LaunchedEffect(isConnected, accessToken) {
        if (isConnected && !accessToken.isNullOrBlank() && events.isEmpty()) {
            val token = accessToken ?: return@LaunchedEffect
            GoogleCalendarManager.fetchUpcomingEvents(token)
                .onSuccess {
                    events = it
                    statusMessage = if (it.isEmpty()) {
                        "Connected successfully. Your current calendar has no upcoming events yet."
                    } else {
                        "Calendar synced successfully. Explore your month view below."
                    }
                }
                .onFailure {
                    statusMessage = "Your Google Calendar session has expired. Please reconnect to continue."
                }
        }
    }

    fun runCalendarAction(action: CalendarAction) {
        when (action) {
            CalendarAction.LoadEvents -> {
                val token = accessToken ?: return
                scope.launch {
                    GoogleCalendarManager.fetchUpcomingEvents(token)
                        .onSuccess {
                            events = it
                            statusMessage = if (it.isEmpty()) {
                                "Connected successfully. Your current filter has no events yet."
                            } else {
                                "Calendar synced successfully. Explore your month view below."
                            }
                        }
                        .onFailure {
                            statusMessage = "We couldn't load your Google Calendar right now. Please reconnect and try again."
                        }
                }
            }

            CalendarAction.ScheduleMeeting -> {
                val token = accessToken ?: return
                val parsedDuration = durationMinutes.toIntOrNull()?.takeIf { it > 0 } ?: 30
                scope.launch {
                    GoogleCalendarManager.scheduleMeeting(
                        accessToken = token,
                        request = MeetingRequest(
                            topic = topic,
                            date = date,
                            startTime = startTime,
                            durationMinutes = parsedDuration
                        )
                    ).onSuccess { result ->
                        latestMeetLink = result.meetLink ?: result.htmlLink
                        statusMessage = if (result.meetLink != null) {
                            "Google Meet scheduled successfully with the SaWa admin."
                        } else {
                            "Appointment created successfully and invitation sent to the SaWa admin."
                        }
                        showScheduleForm = false
                        showingAppointmentsList = true
                        selectedDateKey = date
                        val parsedDate = parseDateToCalendar(date)
                        visibleMonth = parsedDate.get(Calendar.MONTH)
                        visibleYear = parsedDate.get(Calendar.YEAR)
                        runCalendarAction(CalendarAction.LoadEvents)
                        if (openScheduleOnStart) {
                            onCloseRequest()
                        }
                    }.onFailure {
                        statusMessage = "We couldn't schedule the appointment right now. Please reconnect to Google Calendar and try again."
                    }
                }
            }
        }
    }

    val authorizationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        val currentActivity = activity ?: return@rememberLauncherForActivityResult
        GoogleCalendarManager.resolveAuthorizationResult(
            activity = currentActivity,
            data = result.data,
            onAuthorized = { token ->
                accessToken = token
                isConnected = true
                CalendarSessionStore.saveConnection(appContext, token)
                pendingAction?.let { action -> runCalendarAction(action) }
            },
            onError = { error ->
                statusMessage = "Google Calendar couldn't be connected right now. Please try again."
            }
        )
    }

    val authorizeAndRun: (CalendarAction) -> Unit = { action ->
        if (activity == null) {
            statusMessage = "Google Calendar needs an activity context to continue."
        } else {
            pendingAction = action
            GoogleCalendarManager.requestCalendarAccess(
                activity = activity,
                onAuthorized = { token ->
                    accessToken = token
                    isConnected = true
                    CalendarSessionStore.saveConnection(appContext, token)
                    runCalendarAction(action)
                },
                onResolutionRequired = { pendingIntent ->
                    try {
                        authorizationLauncher.launch(
                            IntentSenderRequest.Builder(pendingIntent.intentSender).build()
                        )
                    } catch (_: IntentSender.SendIntentException) {
                        statusMessage = "Google Calendar approval could not be opened. Please try again."
                    } catch (exception: Throwable) {
                        statusMessage = exception.message ?: "Google Calendar approval could not be opened on this device."
                    }
                },
                onError = { error ->
                    statusMessage = "Google Calendar couldn't be connected right now. Please try again."
                }
            )
        }
    }

    val disconnectCalendar = {
        CalendarSessionStore.clearConnection(appContext)
        accessToken = null
        isConnected = false
        events = emptyList()
        latestMeetLink = null
        showScheduleForm = false
        showingAppointmentsList = false
        statusMessage = "Google Calendar disconnected. Connect again whenever you want to view events or schedule a Meet."
    }

    val days = remember(visibleYear, visibleMonth) {
        buildMonthGrid(visibleYear, visibleMonth)
    }
    val selectedDayEvents = eventsByDate[selectedDateKey].orEmpty()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFFCF7), OffWhite, Color(0xFFF7F1E6))
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp.adaptive(adaptiveUi), vertical = 18.dp.adaptive(adaptiveUi)),
            verticalArrangement = Arrangement.spacedBy(16.dp.adaptive(adaptiveUi))
        ) {
            if (openScheduleOnStart) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Surface(
                            modifier = Modifier.clickable(onClick = onCloseRequest),
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.96f),
                            shadowElevation = 6.dp
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close schedule page",
                                tint = DeepCharcoal,
                                modifier = Modifier.padding(12.dp.adaptive(adaptiveUi))
                            )
                        }
                    }
                }
            }

            item {
                CalendarHeader(
                    statusMessage = statusMessage,
                    latestMeetLink = latestMeetLink,
                    isConnected = isConnected,
                    adaptiveUi = adaptiveUi,
                    onConnect = { authorizeAndRun(CalendarAction.LoadEvents) },
                    onDisconnect = disconnectCalendar,
                    onRefresh = { authorizeAndRun(CalendarAction.LoadEvents) }
                )
            }

            item {
                AnimatedVisibility(
                    visible = isConnected,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp.adaptive(adaptiveUi))) {
                        FilterRow(
                            showingAppointmentsList = showingAppointmentsList,
                            adaptiveUi = adaptiveUi,
                            onShowAll = {
                                showingAppointmentsList = false
                                statusMessage = "Showing all synced calendar events."
                            },
                            onShowAppointments = {
                                showingAppointmentsList = true
                                statusMessage = "Showing your list of SaWa appointments."
                                appointments.firstOrNull()?.let { event ->
                                    selectedDateKey = event.startDate
                                    date = event.startDate
                                    val parsedDate = parseDateToCalendar(event.startDate)
                                    visibleMonth = parsedDate.get(Calendar.MONTH)
                                    visibleYear = parsedDate.get(Calendar.YEAR)
                                }
                            }
                        )
                        if (showingAppointmentsList) {
                            AppointmentListPanel(appointments = appointments, adaptiveUi = adaptiveUi)
                        } else {
                            CalendarMonthCard(
                                visibleYear = visibleYear,
                                visibleMonth = visibleMonth,
                                days = days,
                                eventsByDate = eventsByDate,
                                selectedDateKey = selectedDateKey,
                                adaptiveUi = adaptiveUi,
                                onPreviousMonth = {
                                    val previous = shiftMonth(visibleYear, visibleMonth, -1)
                                    visibleYear = previous.first
                                    visibleMonth = previous.second
                                    selectedDateKey = "%04d-%02d-01".format(visibleYear, visibleMonth + 1)
                                },
                                onNextMonth = {
                                    val next = shiftMonth(visibleYear, visibleMonth, 1)
                                    visibleYear = next.first
                                    visibleMonth = next.second
                                    selectedDateKey = "%04d-%02d-01".format(visibleYear, visibleMonth + 1)
                                },
                                onDateSelected = { day ->
                                    selectedDateKey = day.key
                                    date = day.key
                                }
                            )
                            SelectedDayPanel(
                                selectedDateKey = selectedDateKey,
                                events = selectedDayEvents,
                                adaptiveUi = adaptiveUi
                            )
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = {
                showScheduleForm = !showScheduleForm
                date = selectedDateKey
                startTime = "10:00"
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(22.dp.adaptive(adaptiveUi)),
            containerColor = Color(0xFFA8B8F1),
            contentColor = DeepCharcoal
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Schedule appointment"
            )
        }

        if (showScheduleForm) {
            Dialog(
                onDismissRequest = {
                    if (openScheduleOnStart) {
                        onCloseRequest()
                    } else {
                        showScheduleForm = false
                    }
                }
            ) {
                ScheduleAppointmentCard(
                    topic = topic,
                    date = date,
                    startTime = startTime,
                    durationMinutes = durationMinutes,
                    adaptiveUi = adaptiveUi,
                    onTopicChange = { topic = it },
                    onDateChange = { date = it },
                    onStartTimeChange = { startTime = it },
                    onDurationChange = { durationMinutes = it.filter { ch -> ch.isDigit() } },
                    onSchedule = {
                        if (date.isBlank() || startTime.isBlank()) {
                            statusMessage = "Please enter both a date and a start time before scheduling the appointment."
                        } else {
                            authorizeAndRun(CalendarAction.ScheduleMeeting)
                        }
                    },
                    onDismiss = {
                        if (openScheduleOnStart) {
                            onCloseRequest()
                        } else {
                            showScheduleForm = false
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun CalendarHeader(
    statusMessage: String,
    latestMeetLink: String?,
    isConnected: Boolean,
    adaptiveUi: AdaptiveUi,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    onRefresh: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp.adaptive(adaptiveUi)),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.96f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp.adaptive(adaptiveUi)),
            verticalArrangement = Arrangement.spacedBy(10.dp.adaptive(adaptiveUi))
        ) {
            Text(
                text = "Calendar",
                style = MaterialTheme.typography.displayLarge.copy(
                    color = DeepCharcoal,
                    fontFamily = RefinedSerif,
                    fontSize = 29.sp.adaptive(adaptiveUi),
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = statusMessage,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = DeepCharcoal.copy(alpha = 0.7f),
                    fontSize = 11.sp.adaptive(adaptiveUi),
                    lineHeight = 18.sp.adaptive(adaptiveUi)
                )
            )
            latestMeetLink?.let { link ->
                Text(
                    text = "Latest Meet link: $link",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = ChampagneGold,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp.adaptive(adaptiveUi)
                    )
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp.adaptive(adaptiveUi)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = if (isConnected) onDisconnect else onConnect,
                    modifier = Modifier.weight(1.2f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DeepCharcoal,
                        contentColor = ChampagneGold
                    ),
                    shape = RoundedCornerShape(16.dp.adaptive(adaptiveUi)),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 12.dp.adaptive(adaptiveUi))
                ) {
                    Text(
                        if (isConnected) "Disconnect" else "Connect Calendar",
                        maxLines = 1,
                        fontSize = 12.sp.adaptive(adaptiveUi)
                    )
                }
                Surface(
                    modifier = Modifier
                        .weight(0.8f)
                        .border(1.dp, DeepCharcoal.copy(alpha = 0.1f), RoundedCornerShape(16.dp.adaptive(adaptiveUi)))
                        .clickable(onClick = onRefresh),
                    color = Color.White,
                    shape = RoundedCornerShape(16.dp.adaptive(adaptiveUi))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp.adaptive(adaptiveUi)),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = DeepCharcoal,
                            modifier = Modifier.size(16.dp.adaptive(adaptiveUi))
                        )
                        Spacer(modifier = Modifier.width(6.dp.adaptive(adaptiveUi)))
                        Text(
                            "Refresh",
                            color = DeepCharcoal,
                            maxLines = 1,
                            fontSize = 12.sp.adaptive(adaptiveUi)
                        )
                    }
                }
            }
            if (!isConnected) {
                Text(
                    text = "Your calendar grid will appear after Google Calendar connects successfully.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = DeepCharcoal.copy(alpha = 0.58f),
                        fontSize = 11.sp.adaptive(adaptiveUi),
                        lineHeight = 16.sp.adaptive(adaptiveUi)
                    )
                )
            }
        }
    }
}

@Composable
private fun FilterRow(
    showingAppointmentsList: Boolean,
    adaptiveUi: AdaptiveUi,
    onShowAll: () -> Unit,
    onShowAppointments: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp.adaptive(adaptiveUi))
    ) {
        FilterToggleButton(
            selected = !showingAppointmentsList,
            label = "All",
            adaptiveUi = adaptiveUi,
            onClick = onShowAll
        )
        FilterToggleButton(
            selected = showingAppointmentsList,
            label = "Appointments",
            adaptiveUi = adaptiveUi,
            onClick = onShowAppointments
        )
    }
}

@Composable
private fun FilterToggleButton(
    selected: Boolean,
    label: String,
    adaptiveUi: AdaptiveUi,
    onClick: () -> Unit
) {
    if (selected) {
        Button(
            onClick = onClick,
            shape = RoundedCornerShape(20.dp.adaptive(adaptiveUi)),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF7EC6B5),
                contentColor = Color.White
            )
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp.adaptive(adaptiveUi)
                )
            )
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            shape = RoundedCornerShape(20.dp.adaptive(adaptiveUi)),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = DeepCharcoal
            )
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp.adaptive(adaptiveUi)
                )
            )
        }
    }
}

@Composable
private fun CalendarMonthCard(
    visibleYear: Int,
    visibleMonth: Int,
    days: List<CalendarDayCell>,
    eventsByDate: Map<String, List<CalendarUiEvent>>,
    selectedDateKey: String,
    adaptiveUi: AdaptiveUi,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDateSelected: (CalendarDayCell) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp.adaptive(adaptiveUi)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F6FF)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp.adaptive(adaptiveUi)),
            verticalArrangement = Arrangement.spacedBy(12.dp.adaptive(adaptiveUi))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.clickable(onClick = onPreviousMonth),
                    shape = CircleShape,
                    color = Color.White
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = "Previous month",
                        tint = DeepCharcoal,
                        modifier = Modifier.padding(8.dp.adaptive(adaptiveUi))
                    )
                }
                Text(
                    text = monthYearLabel(visibleYear, visibleMonth),
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = DeepCharcoal,
                        fontFamily = RefinedSerif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp.adaptive(adaptiveUi)
                    )
                )
                Surface(
                    modifier = Modifier.clickable(onClick = onNextMonth),
                    shape = CircleShape,
                    color = Color.White
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Next month",
                        tint = DeepCharcoal,
                        modifier = Modifier.padding(8.dp.adaptive(adaptiveUi))
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                weekDayLabels().forEach { label ->
                    Text(
                        text = label,
                        modifier = Modifier.widthIn(min = 36.dp.adaptive(adaptiveUi)),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = DeepCharcoal.copy(alpha = 0.68f),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp.adaptive(adaptiveUi)
                        )
                    )
                }
            }

            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val cellWidth = (maxWidth - 24.dp.adaptive(adaptiveUi)) / 7
                val rows = days.chunked(7)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp.adaptive(adaptiveUi))) {
                    rows.forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp.adaptive(adaptiveUi))) {
                            row.forEach { day ->
                                val dayEvents = eventsByDate[day.key].orEmpty()
                                val isSelected = selectedDateKey == day.key
                                CalendarDayCard(
                                    day = day,
                                    events = dayEvents,
                                    isSelected = isSelected,
                                    width = cellWidth,
                                    adaptiveUi = adaptiveUi,
                                    onClick = { onDateSelected(day) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCard(
    day: CalendarDayCell,
    events: List<CalendarUiEvent>,
    isSelected: Boolean,
    width: androidx.compose.ui.unit.Dp,
    adaptiveUi: AdaptiveUi,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(width)
            .height(92.dp.adaptive(adaptiveUi))
            .background(
                color = if (isSelected) Color(0xFFE6EBFF) else Color.White.copy(alpha = 0.92f),
                shape = RoundedCornerShape(14.dp.adaptive(adaptiveUi))
            )
            .border(
                width = if (isSelected) 1.4.dp.adaptive(adaptiveUi) else 1.dp.adaptive(adaptiveUi),
                color = if (isSelected) Color(0xFF6E83C2) else Color(0xFFE8E8F0),
                shape = RoundedCornerShape(14.dp.adaptive(adaptiveUi))
            )
            .clickable(onClick = onClick)
            .padding(6.dp.adaptive(adaptiveUi))
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp.adaptive(adaptiveUi))) {
            Box(
                modifier = Modifier
                    .align(Alignment.End)
                    .background(
                        color = if (isSelected) Color(0xFF586A9E) else Color.Transparent,
                        shape = CircleShape
                    )
                    .padding(horizontal = 8.dp.adaptive(adaptiveUi), vertical = if (isSelected) 3.dp.adaptive(adaptiveUi) else 0.dp)
            ) {
                Text(
                    text = day.day.toString(),
                    color = if (isSelected) Color.White else if (day.inCurrentMonth) DeepCharcoal else DeepCharcoal.copy(alpha = 0.35f),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp.adaptive(adaptiveUi)
                    )
                )
            }
            events.take(2).forEach { event ->
                EventChip(
                    title = shortenEventTitle(event.title),
                    color = eventColor(event.category),
                    adaptiveUi = adaptiveUi
                )
            }
            if (events.size > 2) {
                Text(
                    text = "+${events.size - 2} more",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = DeepCharcoal.copy(alpha = 0.55f),
                        fontSize = 9.sp.adaptive(adaptiveUi)
                    )
                )
            }
        }
    }
}

@Composable
private fun EventChip(title: String, color: Color, adaptiveUi: AdaptiveUi) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = color, shape = RoundedCornerShape(8.dp.adaptive(adaptiveUi)))
            .padding(horizontal = 5.dp.adaptive(adaptiveUi), vertical = 2.dp.adaptive(adaptiveUi))
    ) {
        Text(
            text = title,
            maxLines = 1,
            color = Color.White,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 10.sp.adaptive(adaptiveUi),
                fontWeight = FontWeight.SemiBold
            )
        )
    }
}

@Composable
private fun SelectedDayPanel(
    selectedDateKey: String,
    events: List<CalendarUiEvent>,
    adaptiveUi: AdaptiveUi
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp.adaptive(adaptiveUi)),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.96f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp.adaptive(adaptiveUi)),
            verticalArrangement = Arrangement.spacedBy(12.dp.adaptive(adaptiveUi))
        ) {
            Text(
                text = "Selected Day",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = DeepCharcoal,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp.adaptive(adaptiveUi)
                )
            )
            Text(
                text = prettyDateLabel(selectedDateKey),
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = ChampagneGold,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp.adaptive(adaptiveUi)
                )
            )
            if (events.isEmpty()) {
                Text(
                    text = "No events on this day for the active filter.",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = DeepCharcoal.copy(alpha = 0.68f),
                        fontSize = 13.sp.adaptive(adaptiveUi),
                        lineHeight = 20.sp.adaptive(adaptiveUi)
                    )
                )
            } else {
                events.forEach { event ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp.adaptive(adaptiveUi)),
                        color = Color(0xFFFFFCF8)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp.adaptive(adaptiveUi)),
                            verticalArrangement = Arrangement.spacedBy(5.dp.adaptive(adaptiveUi))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = event.title,
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = DeepCharcoal,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp.adaptive(adaptiveUi)
                                    )
                                )
                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = eventColor(event.category),
                                            shape = RoundedCornerShape(10.dp.adaptive(adaptiveUi))
                                        )
                                        .padding(horizontal = 8.dp.adaptive(adaptiveUi), vertical = 4.dp.adaptive(adaptiveUi))
                                ) {
                                    Text(
                                        text = if (event.category == EventCategory.SavaAppointment) "SaWa" else "Calendar",
                                        color = Color.White,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 10.sp.adaptive(adaptiveUi)
                                        )
                                    )
                                }
                            }
                            Text(
                                text = event.startLabel,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = ChampagneGold,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp.adaptive(adaptiveUi)
                                )
                            )
                            event.meetLink?.let { link ->
                                Text(
                                    text = link,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = DeepCharcoal.copy(alpha = 0.65f),
                                        fontSize = 11.sp.adaptive(adaptiveUi)
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppointmentListPanel(appointments: List<CalendarUiEvent>, adaptiveUi: AdaptiveUi) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp.adaptive(adaptiveUi)),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.96f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp.adaptive(adaptiveUi)),
            verticalArrangement = Arrangement.spacedBy(12.dp.adaptive(adaptiveUi))
        ) {
            Text(
                text = "Your Appointments",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = DeepCharcoal,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp.adaptive(adaptiveUi)
                )
            )
            if (appointments.isEmpty()) {
                Text(
                    text = "No SaWa appointments are available yet. Use the + button to schedule one.",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = DeepCharcoal.copy(alpha = 0.68f),
                        fontSize = 13.sp.adaptive(adaptiveUi),
                        lineHeight = 22.sp.adaptive(adaptiveUi)
                    )
                )
            } else {
                appointments.forEach { event ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp.adaptive(adaptiveUi)),
                        color = Color(0xFFF7F9FF)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp.adaptive(adaptiveUi)),
                            verticalArrangement = Arrangement.spacedBy(6.dp.adaptive(adaptiveUi))
                        ) {
                            Text(
                                text = event.title,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = DeepCharcoal,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp.adaptive(adaptiveUi)
                                )
                            )
                            Text(
                                text = event.startLabel,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = ChampagneGold,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp.adaptive(adaptiveUi)
                                )
                            )
                            event.meetLink?.let { link ->
                                Text(
                                    text = link,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = DeepCharcoal.copy(alpha = 0.65f),
                                        fontSize = 11.sp.adaptive(adaptiveUi)
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScheduleAppointmentCard(
    topic: String,
    date: String,
    startTime: String,
    durationMinutes: String,
    adaptiveUi: AdaptiveUi,
    onTopicChange: (String) -> Unit,
    onDateChange: (String) -> Unit,
    onStartTimeChange: (String) -> Unit,
    onDurationChange: (String) -> Unit,
    onSchedule: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp.adaptive(adaptiveUi)),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.96f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp.adaptive(adaptiveUi)),
            verticalArrangement = Arrangement.spacedBy(12.dp.adaptive(adaptiveUi))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Schedule an appointment",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = DeepCharcoal,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp.adaptive(adaptiveUi)
                    )
                )
                Surface(
                    modifier = Modifier.clickable(onClick = onDismiss),
                    shape = CircleShape,
                    color = Color(0xFFF7F9FF)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close schedule appointment",
                        tint = DeepCharcoal,
                        modifier = Modifier.padding(10.dp.adaptive(adaptiveUi))
                    )
                }
            }
            Text(
                text = "Create a Google Meet and invite the SaWa admin directly from your calendar.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = DeepCharcoal.copy(alpha = 0.68f),
                    fontSize = 12.sp.adaptive(adaptiveUi),
                    lineHeight = 18.sp.adaptive(adaptiveUi)
                )
            )
            OutlinedTextField(
                value = topic,
                onValueChange = onTopicChange,
                label = { Text("Topic", fontSize = 12.sp.adaptive(adaptiveUi)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp.adaptive(adaptiveUi))
            )
            OutlinedTextField(
                value = date,
                onValueChange = onDateChange,
                label = { Text("Date (YYYY-MM-DD)", fontSize = 12.sp.adaptive(adaptiveUi)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp.adaptive(adaptiveUi))
            )
            OutlinedTextField(
                value = startTime,
                onValueChange = onStartTimeChange,
                label = { Text("Start Time (HH:MM)", fontSize = 12.sp.adaptive(adaptiveUi)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp.adaptive(adaptiveUi))
            )
            OutlinedTextField(
                value = durationMinutes,
                onValueChange = onDurationChange,
                label = { Text("Duration (minutes)", fontSize = 12.sp.adaptive(adaptiveUi)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp.adaptive(adaptiveUi))
            )
            Button(
                onClick = onSchedule,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DeepCharcoal,
                    contentColor = ChampagneGold
                ),
                shape = RoundedCornerShape(16.dp.adaptive(adaptiveUi))
            ) {
                Text("Schedule appointment", fontSize = 14.sp.adaptive(adaptiveUi))
            }
        }
    }
}

private fun categorizeEvent(event: CalendarEventItem): EventCategory {
    val title = event.title.lowercase()
    return if (
        "sava" in title ||
        "appointment" in title ||
        event.meetLink != null
    ) {
        EventCategory.SavaAppointment
    } else {
        EventCategory.CalendarEvent
    }
}

private fun eventColor(category: EventCategory): Color = when (category) {
    EventCategory.SavaAppointment -> Color(0xFF8FA9FF)
    EventCategory.CalendarEvent -> Color(0xFF6BC1AE)
}

private fun shortenEventTitle(title: String): String {
    return if (title.length > 12) title.take(11) else title
}

private fun buildMonthGrid(year: Int, month: Int): List<CalendarDayCell> {
    val firstDay = Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month)
        set(Calendar.DAY_OF_MONTH, 1)
    }
    val startOffset = firstDay.get(Calendar.DAY_OF_WEEK) - 1
    val gridStart = firstDay.clone() as Calendar
    gridStart.add(Calendar.DAY_OF_MONTH, -startOffset)

    return List(42) { index ->
        val current = gridStart.clone() as Calendar
        current.add(Calendar.DAY_OF_MONTH, index)
        CalendarDayCell(
            year = current.get(Calendar.YEAR),
            month = current.get(Calendar.MONTH),
            day = current.get(Calendar.DAY_OF_MONTH),
            inCurrentMonth = current.get(Calendar.MONTH) == month
        )
    }
}

private fun shiftMonth(year: Int, month: Int, delta: Int): Pair<Int, Int> {
    val calendar = Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month)
        set(Calendar.DAY_OF_MONTH, 1)
        add(Calendar.MONTH, delta)
    }
    return calendar.get(Calendar.YEAR) to calendar.get(Calendar.MONTH)
}

private fun monthYearLabel(year: Int, month: Int): String {
    return "${DateFormatSymbols.getInstance().months[month]} $year"
}

private fun weekDayLabels(): List<String> = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

private fun prettyDateLabel(dateKey: String): String {
    return runCatching {
        val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateKey)
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(parsed!!)
    }.getOrDefault(dateKey)
}

private fun formatCalendarDate(calendar: Calendar): String {
    return "%04d-%02d-%02d".format(
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH) + 1,
        calendar.get(Calendar.DAY_OF_MONTH)
    )
}

private fun parseDateToCalendar(value: String): Calendar {
    val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(value)
        ?: Calendar.getInstance().time
    return Calendar.getInstance().apply { time = parsed }
}
