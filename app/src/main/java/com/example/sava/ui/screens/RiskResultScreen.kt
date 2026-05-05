package com.example.sava.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sava.auth.RiskAssessmentStore
import com.example.sava.ui.theme.ChampagneGold
import com.example.sava.ui.theme.DeepCharcoal
import com.example.sava.ui.theme.OffWhite
import com.example.sava.ui.theme.RefinedSerif
import com.example.sava.ui.theme.SavaTheme
import com.example.sava.ui.theme.adaptive
import com.example.sava.ui.theme.rememberAdaptiveUi
import com.example.sava.ui.theme.AdaptiveUi

@Composable
fun RiskResultScreen(
    profile: String,
    age: Int,
    investableCorpusPerMonth: Long,
    onBack: () -> Unit,
    onCalculatePlan: (String) -> Unit,
    onRetakeQuiz: () -> Unit,
    persistAssessment: Boolean = true
) {
    val adaptiveUi = rememberAdaptiveUi()
    val behaviouralRiskProfile = normalizeRiskProfile(profile)
    val riskCapacity = calculateRiskCapacity(age)
    val optimumRiskCapability = calculateOptimumRiskCapability(
        riskCapacity = riskCapacity,
        behaviouralRiskProfile = behaviouralRiskProfile
    )

    LaunchedEffect(age, investableCorpusPerMonth, behaviouralRiskProfile, riskCapacity, optimumRiskCapability, persistAssessment) {
        if (persistAssessment) {
            RiskAssessmentStore.saveLatestAssessment(
                age = age,
                investableCorpusPerMonth = investableCorpusPerMonth,
                behaviouralRiskProfile = behaviouralRiskProfile,
                riskCapacity = riskCapacity,
                optimumRiskCapability = optimumRiskCapability
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OffWhite)
    ) {
        Text(
            text = optimumRiskCapability.uppercase(),
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = (if (adaptiveUi.isCompact) 64.sp else 104.sp).adaptive(adaptiveUi),
                fontWeight = FontWeight.Black,
                color = DeepCharcoal
            ),
            modifier = Modifier
                .align(Alignment.Center)
                .alpha(0.04f)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = 24.dp.adaptive(adaptiveUi),
                    vertical = 40.dp.adaptive(adaptiveUi)
                )
                .widthIn(max = adaptiveUi.maxContentWidth),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                SecondaryScreenBackButton(onClick = onBack)
            }
            Spacer(modifier = Modifier.height(24.dp.adaptive(adaptiveUi)))

            Text(
                text = "Your Risk Assessment",
                style = MaterialTheme.typography.displayLarge.copy(
                    color = DeepCharcoal,
                    fontFamily = RefinedSerif,
                    fontSize = 34.sp.adaptive(adaptiveUi),
                    lineHeight = 36.sp.adaptive(adaptiveUi)
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp.adaptive(adaptiveUi)))

            RiskSummaryCard(
                title = "Risk Capacity",
                value = riskCapacity,
                description = "Derived using a structured evaluation of key demographic and financial parameters to assess your inherent ability to take risk.",
                adaptiveUi = adaptiveUi
            )

            Spacer(modifier = Modifier.height(16.dp.adaptive(adaptiveUi)))

            RiskSummaryCard(
                title = "Behavioural Risk Profile",
                value = behaviouralRiskProfile,
                description = "Based on a comprehensive analysis of your responses, reflecting your comfort with market movements and investment tendencies.",
                adaptiveUi = adaptiveUi
            )

            Spacer(modifier = Modifier.height(16.dp.adaptive(adaptiveUi)))

            RiskSummaryCard(
                title = "Optimum Risk Capability",
                value = optimumRiskCapability,
                description = "Arrived at through a calibrated alignment of your financial capacity and behavioural preferences to ensure a balanced and suitable approach.",
                adaptiveUi = adaptiveUi
            )

            Spacer(modifier = Modifier.height(24.dp.adaptive(adaptiveUi)))

            Text(
                text = "Your recommended profile is determined through a multi-dimensional assessment framework designed to align opportunity with comfort, ensuring consistency and long-term suitability.",
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 420.dp.adaptive(adaptiveUi))
                    .padding(horizontal = 8.dp.adaptive(adaptiveUi)),
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = DeepCharcoal.copy(alpha = 0.72f),
                    fontSize = 13.sp.adaptive(adaptiveUi),
                    lineHeight = 20.sp.adaptive(adaptiveUi)
                ),
                textAlign = TextAlign.Justify
            )

            Spacer(modifier = Modifier.height(18.dp.adaptive(adaptiveUi)))

            Text(
                text = "Retake the quiz",
                style = MaterialTheme.typography.labelLarge.copy(
                    color = ChampagneGold,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp.adaptive(adaptiveUi)
                ),
                modifier = Modifier.clickable(onClick = onRetakeQuiz)
            )

            Spacer(modifier = Modifier.height(96.dp.adaptive(adaptiveUi)))
        }

        CalculatePlanButton(
            visible = true,
            adaptiveUi = adaptiveUi,
            onClick = { onCalculatePlan(optimumRiskCapability) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(
                    start = 24.dp.adaptive(adaptiveUi),
                    end = 24.dp.adaptive(adaptiveUi),
                    bottom = 32.dp.adaptive(adaptiveUi)
                )
        )
    }
}

@Composable
private fun RiskSummaryCard(
    title: String,
    value: String,
    description: String,
    adaptiveUi: AdaptiveUi
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp.adaptive(adaptiveUi)))
            .background(Color.White.copy(alpha = 0.88f))
            .border(
                width = 1.dp.adaptive(adaptiveUi),
                color = ChampagneGold.copy(alpha = 0.22f),
                shape = RoundedCornerShape(28.dp.adaptive(adaptiveUi))
            )
            .padding(20.dp.adaptive(adaptiveUi)),
        verticalArrangement = Arrangement.spacedBy(10.dp.adaptive(adaptiveUi))
    ) {
        if (adaptiveUi.isCompact) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp.adaptive(adaptiveUi))
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = DeepCharcoal,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp.adaptive(adaptiveUi)
                    )
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = ChampagneGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp.adaptive(adaptiveUi)
                    )
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = DeepCharcoal,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp.adaptive(adaptiveUi)
                    )
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = ChampagneGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp.adaptive(adaptiveUi)
                    )
                )
            }
        }

        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = DeepCharcoal.copy(alpha = 0.7f),
                fontSize = 14.sp.adaptive(adaptiveUi),
                lineHeight = 20.sp.adaptive(adaptiveUi)
            )
        )
    }
}

@Composable
fun CalculatePlanButton(
    visible: Boolean, 
    adaptiveUi: AdaptiveUi,
    onClick: () -> Unit, 
    modifier: Modifier = Modifier
) {
    var isClicked by remember { mutableStateOf(false) }
    val infiniteTransition = rememberInfiniteTransition(label = "breathing")
    val alphaValue by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    val widthValue by animateDpAsState(
        targetValue = if (isClicked) 2.dp else 280.dp.adaptive(adaptiveUi),
        animationSpec = tween(500),
        label = "width"
    )

    if (visible) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .widthIn(max = widthValue)
                .height(60.dp.adaptive(adaptiveUi))
                .alpha(alphaValue)
                .clip(CircleShape)
                .background(DeepCharcoal)
                .clickable {
                    isClicked = true
                    onClick()
                },
            contentAlignment = Alignment.Center
        ) {
            if (!isClicked) {
                Text(
                    "Calculate Your Best Plan",
                    color = ChampagneGold,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 14.sp.adaptive(adaptiveUi)
                    )
                )
            }
        }
    }
}

private fun normalizeRiskProfile(profile: String): String = when (profile.lowercase()) {
    "conservative" -> "Conservative"
    "moderate" -> "Moderate"
    "high", "high risk", "aggressive" -> "Aggressive"
    else -> "Moderate"
}

private fun calculateRiskCapacity(age: Int): String = when {
    age > 50 -> "Conservative"
    age in 40..50 -> "Moderate"
    else -> "Aggressive"
}

private fun calculateOptimumRiskCapability(
    riskCapacity: String,
    behaviouralRiskProfile: String
): String {
    val riskCapacityValue = riskProfileValue(riskCapacity)
    val behaviouralValue = riskProfileValue(behaviouralRiskProfile)
    return if (riskCapacityValue <= behaviouralValue) riskCapacity else behaviouralRiskProfile
}

private fun riskProfileValue(profile: String): Int = when (normalizeRiskProfile(profile)) {
    "Conservative" -> 1
    "Moderate" -> 2
    "Aggressive" -> 3
    else -> 2
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun RiskResultScreenPreview() {
    SavaTheme {
        RiskResultScreen(
            profile = "Moderate",
            age = 30,
            investableCorpusPerMonth = 15000,
            onBack = {},
            onCalculatePlan = {},
            onRetakeQuiz = {}
        )
    }
}
