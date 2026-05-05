package com.example.sava.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sava.auth.RiskAssessmentRecord
import com.example.sava.auth.RiskAssessmentStore
import com.example.sava.ui.theme.ChampagneGold
import com.example.sava.ui.theme.DeepCharcoal
import com.example.sava.ui.theme.OffWhite
import com.example.sava.ui.theme.RefinedSerif
import com.example.sava.ui.theme.SavaTheme
import com.example.sava.ui.theme.adaptive
import com.example.sava.ui.theme.rememberAdaptiveUi
import com.example.sava.ui.theme.AdaptiveUi
import kotlinx.coroutines.delay

@Composable
fun RiskAssessmentGateScreen(
    selectedGoalSlug: String? = null,
    onBack: () -> Unit,
    onStartQuiz: () -> Unit,
    onOpenSavedResult: (RiskAssessmentRecord) -> Unit,
    onOpenGoalPlanner: (RiskAssessmentRecord, String) -> Unit
) {
    val adaptiveUi = rememberAdaptiveUi()
    var savedRecord by remember { mutableStateOf<RiskAssessmentRecord?>(null) }
    var showMissingAssessmentPopup by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        RiskAssessmentStore.fetchLatestAssessment(
            onFound = { record ->
                if (selectedGoalSlug != null) {
                    onOpenGoalPlanner(record, selectedGoalSlug)
                } else {
                    savedRecord = record
                }
            },
            onNotFound = {
                if (selectedGoalSlug != null) {
                    showMissingAssessmentPopup = true
                } else {
                    onStartQuiz()
                }
            },
            onError = {
                if (selectedGoalSlug != null) {
                    showMissingAssessmentPopup = true
                } else {
                    onStartQuiz()
                }
            }
        )
    }

    LaunchedEffect(savedRecord) {
        savedRecord?.let { record ->
            delay(1200)
            onOpenSavedResult(record)
        }
    }

    LaunchedEffect(showMissingAssessmentPopup) {
        if (showMissingAssessmentPopup) {
            delay(1200)
            onStartQuiz()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(OffWhite, OffWhite.copy(alpha = 0.96f))
                )
            )
            .padding(28.dp.adaptive(adaptiveUi)),
        contentAlignment = Alignment.Center
    ) {
        SecondaryScreenBackButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.TopStart)
        )
        if (savedRecord != null || showMissingAssessmentPopup) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 340.dp.adaptive(adaptiveUi))
                    .shadow(20.dp.adaptive(adaptiveUi), RoundedCornerShape(30.dp.adaptive(adaptiveUi)), clip = false)
                    .clip(RoundedCornerShape(30.dp.adaptive(adaptiveUi)))
                    .background(Color.White.copy(alpha = 0.95f))
                    .border(
                        width = 1.dp.adaptive(adaptiveUi),
                        color = ChampagneGold.copy(alpha = 0.22f),
                        shape = RoundedCornerShape(30.dp.adaptive(adaptiveUi))
                    )
                    .padding(horizontal = 24.dp.adaptive(adaptiveUi), vertical = 28.dp.adaptive(adaptiveUi)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp.adaptive(adaptiveUi))
            ) {

                Text(
                    text = if (showMissingAssessmentPopup) {
                        "Looks like you haven't calculated your risk profile yet"
                    } else {
                        "You have already taken the quiz"
                    },
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = DeepCharcoal,
                        fontFamily = RefinedSerif,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 22.sp.adaptive(adaptiveUi)
                    ),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = if (showMissingAssessmentPopup) {
                        "Redirecting to the quiz..."
                    } else {
                        "Redirecting to previous results"
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = ChampagneGold,
                        fontSize = 14.sp.adaptive(adaptiveUi),
                        fontWeight = FontWeight.Medium
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RiskAssessmentGateScreenPreview() {
    SavaTheme {
        RiskAssessmentGateScreen(
            onBack = {},
            onStartQuiz = {},
            onOpenSavedResult = {},
            onOpenGoalPlanner = { _, _ -> }
        )
    }
}
