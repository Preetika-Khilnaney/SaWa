package com.example.sava.ui.screens

import android.content.ContentValues
import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sava.auth.InvestmentPlanStore
import com.example.sava.auth.SavedPlanGoal
import com.example.sava.ui.theme.ChampagneGold
import com.example.sava.ui.theme.DeepCharcoal
import com.example.sava.ui.theme.OffWhite
import com.example.sava.ui.theme.RefinedSerif
import com.example.sava.ui.theme.SavaTheme
import com.example.sava.ui.theme.adaptive
import com.example.sava.ui.theme.rememberAdaptiveUi
import com.example.sava.ui.theme.AdaptiveUi
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private data class GoalDefinition(
    val name: String,
    val color: Color
)

private data class GoalInputState(
    val goalName: String? = null,
    val expectedEarningsInput: String = "",
    val timeAvailableInput: String = "",
    val continueWithAnotherGoal: Boolean? = null
)

private data class GoalPlan(
    val goalName: String,
    val expectedEarnings: Double,
    val timeAvailableYears: Double,
    val annualRate: Double,
    val fundType: String,
    val monthlySip: Double,
    val color: Color
)

private data class GoalEvaluation(
    val input: GoalInputState,
    val plan: GoalPlan?,
    val availableCorpusBefore: Double,
    val remainingCorpusAfter: Double,
    val isRejected: Boolean,
    val shortfall: Double,
    val availableGoalsAfter: List<GoalDefinition>
)

private val goalDefinitions = listOf(
    GoalDefinition("Safety Net", Color(0xFFD2AF4B)),
    GoalDefinition("Big Day Fund", Color(0xFFC58F59)),
    GoalDefinition("Travel Dreams", Color(0xFF4D89A8)),
    GoalDefinition("My Child's Future", Color(0xFF5F8C5A)),
    GoalDefinition("My First Home", Color(0xFF8F6AAE)),
    GoalDefinition("Prosperity Planning", Color(0xFFBB6E79))
)

@Composable
fun TailoredPlansScreen(
    profile: String,
    age: Int,
    investableCorpusPerMonth: Double = 0.0,
    initialGoalSlug: String? = null,
    onBack: () -> Unit,
    onDownloadPlan: () -> Unit = {},
    onScheduleMeet: () -> Unit = {}
) {
    val context = LocalContext.current
    val adaptiveUi = rememberAdaptiveUi()
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val initialGoalName = goalNameFromSlug(initialGoalSlug)
    var goalInputs by remember(initialGoalName) {
        mutableStateOf(
            listOf(
                GoalInputState(goalName = initialGoalName)
            )
        )
    }
    var lastSavedPlanSignature by remember { mutableStateOf<String?>(null) }
    var summaryOffsetPx by remember { mutableStateOf<Int?>(null) }

    val normalizedProfile = normalizePlanProfile(profile)
    val evaluations = evaluateGoals(goalInputs = goalInputs, profile = normalizedProfile, corpus = investableCorpusPerMonth)
    val visibleCount = calculateVisibleGoalCount(goalInputs, evaluations)
    val visibleInputs = goalInputs.take(visibleCount)
    val visibleEvaluations = evaluations.take(visibleCount)
    val acceptedPlans = visibleEvaluations.mapNotNull { evaluation ->
        evaluation.plan?.takeIf { !evaluation.isRejected }
    }
    val totalSip = acceptedPlans.sumOf { it.monthlySip }
    val remainingCorpus = investableCorpusPerMonth - totalSip
    val shouldShowSummary = shouldShowSummary(visibleInputs, visibleEvaluations, acceptedPlans)

    LaunchedEffect(visibleCount, goalInputs.size) {
        when {
            visibleCount > goalInputs.size -> {
                goalInputs = goalInputs + GoalInputState()
            }
            visibleCount < goalInputs.size -> {
                goalInputs = goalInputs.take(visibleCount)
            }
        }
    }

    val acceptedPlanSignature = acceptedPlans.joinToString(separator = "|") { plan ->
        listOf(
            plan.goalName,
            plan.expectedEarnings.toString(),
            plan.timeAvailableYears.toString(),
            plan.annualRate.toString(),
            plan.fundType,
            plan.monthlySip.toString()
        ).joinToString(separator = "~")
    }

    LaunchedEffect(
        shouldShowSummary,
        acceptedPlanSignature,
        investableCorpusPerMonth,
        normalizedProfile,
        age
    ) {
        if (shouldShowSummary && acceptedPlans.isNotEmpty() && acceptedPlanSignature != lastSavedPlanSignature) {
            InvestmentPlanStore.savePlan(
                optimumRiskProfile = normalizedProfile,
                age = age,
                investableCorpusPerMonth = investableCorpusPerMonth,
                allocatedSip = totalSip,
                remainingCorpus = remainingCorpus.coerceAtLeast(0.0),
                goals = acceptedPlans.map { plan ->
                    SavedPlanGoal(
                        goalName = plan.goalName,
                        expectedEarnings = plan.expectedEarnings,
                        timeAvailableYears = plan.timeAvailableYears,
                        annualRate = plan.annualRate,
                        fundType = plan.fundType,
                        monthlySip = plan.monthlySip
                    )
                },
                onSuccess = {
                    lastSavedPlanSignature = acceptedPlanSignature
                }
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFFCF7), OffWhite, Color(0xFFF7F1E6))
                )
            )
            .verticalScroll(scrollState)
            .padding(20.dp.adaptive(adaptiveUi)),
        verticalArrangement = Arrangement.spacedBy(18.dp.adaptive(adaptiveUi))
    ) {
        SecondaryScreenBackButton(onClick = onBack)
        PlannerHeader(
            profile = normalizedProfile,
            age = age,
            investableCorpusPerMonth = investableCorpusPerMonth,
            adaptiveUi = adaptiveUi
        )

        visibleInputs.forEachIndexed { index, input ->
            val evaluation = visibleEvaluations.getOrNull(index)
            GoalPlanningCard(
                step = index + 1,
                input = input,
                availableGoals = dropdownGoalsForIndex(
                    goalInputs = visibleInputs,
                    evaluations = visibleEvaluations,
                    currentIndex = index
                ),
                calculatedPlan = evaluation?.plan,
                isRejected = evaluation?.isRejected == true,
                shortfall = evaluation?.shortfall ?: 0.0,
                remainingCorpusBefore = evaluation?.availableCorpusBefore ?: investableCorpusPerMonth,
                adaptiveUi = adaptiveUi,
                onInputChange = { updated ->
                    goalInputs = goalInputs.toMutableList().also { it[index] = updated }
                }
            )

            val canPromptForNext = evaluation?.let {
                it.plan != null &&
                    input.goalName != null &&
                    input.goalName != "None" &&
                    it.availableGoalsAfter.isNotEmpty() &&
                    (it.isRejected || it.remainingCorpusAfter > 0)
            } == true

            if (canPromptForNext) {
                Spacer(modifier = Modifier.height(10.dp.adaptive(adaptiveUi)))
                ContinueGoalCard(
                    isRejected = evaluation?.isRejected == true,
                    remainingCorpus = evaluation?.remainingCorpusAfter ?: 0.0,
                    adaptiveUi = adaptiveUi,
                    onChooseYes = {
                        goalInputs = goalInputs.toMutableList().also {
                            it[index] = input.copy(continueWithAnotherGoal = true)
                        }
                        scope.launch {
                            delay(180)
                            scrollState.animateScrollTo(scrollState.maxValue)
                        }
                    },
                    onChooseNo = {
                        goalInputs = goalInputs.toMutableList().also {
                            it[index] = input.copy(continueWithAnotherGoal = false)
                        }
                        scope.launch {
                            delay(180)
                            scrollState.animateScrollTo(summaryOffsetPx ?: scrollState.maxValue)
                        }
                    }
                )
            }
        }

        if (shouldShowSummary) {
            Box(
                modifier = Modifier.onGloballyPositioned { coordinates ->
                    summaryOffsetPx = coordinates.boundsInParent().top.toInt()
                }
            ) {
                GoalPlanSummary(
                    plans = acceptedPlans,
                    corpus = investableCorpusPerMonth,
                    totalSip = totalSip,
                    remainingCorpus = remainingCorpus,
                    adaptiveUi = adaptiveUi,
                    onDownloadPlan = {
                        val savedFileName = exportInvestmentSummary(
                            context = context,
                            profile = normalizedProfile,
                            age = age,
                            corpus = investableCorpusPerMonth,
                            totalSip = totalSip,
                            remainingCorpus = remainingCorpus,
                            plans = acceptedPlans
                        )
                        if (savedFileName != null) {
                            Toast.makeText(
                                context,
                                "Investment summary saved to Downloads as $savedFileName",
                                Toast.LENGTH_LONG
                            ).show()
                            onDownloadPlan()
                        } else {
                            Toast.makeText(
                                context,
                                "We couldn't save the investment summary right now.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    },
                    onScheduleMeet = onScheduleMeet
                )
            }
        }
    }
}

@Composable
private fun PlannerHeader(
    profile: String,
    age: Int,
    investableCorpusPerMonth: Double,
    adaptiveUi: AdaptiveUi
) {
    PlannerCard(adaptiveUi = adaptiveUi) {
        Text(
            text = "Goal-Based Investment Planner",
            style = MaterialTheme.typography.displayLarge.copy(
                color = DeepCharcoal,
                fontFamily = RefinedSerif,
                fontSize = 25.sp.adaptive(adaptiveUi),
                fontWeight = FontWeight.Bold,
                lineHeight = 30.sp.adaptive(adaptiveUi)
            )
        )
        Spacer(modifier = Modifier.height(10.dp.adaptive(adaptiveUi)))
        Text(
            text = "Optimum Risk Capability: $profile",
            style = MaterialTheme.typography.titleLarge.copy(
                color = ChampagneGold,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp.adaptive(adaptiveUi)
            )
        )
        Spacer(modifier = Modifier.height(8.dp.adaptive(adaptiveUi)))
        Text(
            text = "Age: $age | Investable Corpus (per month): ${formatCurrency(investableCorpusPerMonth)}",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = DeepCharcoal.copy(alpha = 0.72f),
                fontSize = 14.sp.adaptive(adaptiveUi)
            )
        )
    }
}

@Composable
private fun GoalPlanningCard(
    step: Int,
    input: GoalInputState,
    availableGoals: List<GoalDefinition>,
    calculatedPlan: GoalPlan?,
    isRejected: Boolean,
    shortfall: Double,
    remainingCorpusBefore: Double,
    adaptiveUi: AdaptiveUi,
    onInputChange: (GoalInputState) -> Unit
) {
    var dropdownExpanded by remember(step, input.goalName) { mutableStateOf(false) }

    PlannerCard(adaptiveUi = adaptiveUi) {
        Text(
            text = "Priority $step",
            style = MaterialTheme.typography.labelLarge.copy(
                color = ChampagneGold,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp.adaptive(adaptiveUi)
            )
        )
        Spacer(modifier = Modifier.height(13.dp.adaptive(adaptiveUi)))
        BoxWithConstraints {
            val dropdownWidth = maxWidth
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp.adaptive(adaptiveUi)))
                    .background(Color.White)
                    .border(1.dp, DeepCharcoal.copy(alpha = 0.1f), RoundedCornerShape(16.dp.adaptive(adaptiveUi)))
                    .clickable { dropdownExpanded = true }
                    .padding(horizontal = 16.dp.adaptive(adaptiveUi), vertical = 18.dp.adaptive(adaptiveUi))
            ) {
                Text(
                    text = input.goalName ?: "Select a goal",
                    color = if (input.goalName == null) DeepCharcoal.copy(alpha = 0.45f) else DeepCharcoal,
                    fontSize = 14.sp.adaptive(adaptiveUi)
                )
            }

            DropdownMenu(
                expanded = dropdownExpanded,
                onDismissRequest = { dropdownExpanded = false },
                modifier = Modifier.width(dropdownWidth)
            ) {
                availableGoals.forEach { goal ->
                    DropdownMenuItem(
                        text = { Text(goal.name, fontSize = 14.sp.adaptive(adaptiveUi)) },
                        onClick = {
                            dropdownExpanded = false
                            onInputChange(
                                input.copy(
                                    goalName = goal.name,
                                    continueWithAnotherGoal = null
                                )
                            )
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp.adaptive(adaptiveUi)))

        OutlinedTextField(
            value = input.expectedEarningsInput,
            onValueChange = {
                onInputChange(
                    input.copy(
                        expectedEarningsInput = it.filter { ch -> ch.isDigit() || ch == '.' },
                        continueWithAnotherGoal = null
                    )
                )
            },
            label = { Text("Expected corpus", fontSize = 12.sp.adaptive(adaptiveUi)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp.adaptive(adaptiveUi))
        )

        Spacer(modifier = Modifier.height(14.dp.adaptive(adaptiveUi)))

        OutlinedTextField(
            value = input.timeAvailableInput,
            onValueChange = {
                onInputChange(
                    input.copy(
                        timeAvailableInput = it.filter { ch -> ch.isDigit() || ch == '.' },
                        continueWithAnotherGoal = null
                    )
                )
            },
            label = { Text("Time available (years)", fontSize = 12.sp.adaptive(adaptiveUi)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp.adaptive(adaptiveUi))
        )

        if (calculatedPlan != null) {
            Spacer(modifier = Modifier.height(18.dp.adaptive(adaptiveUi)))
            ResultPill(label = "Fund Type to invest in", value = calculatedPlan.fundType, adaptiveUi = adaptiveUi)
            Spacer(modifier = Modifier.height(10.dp.adaptive(adaptiveUi)))
            ResultPill(
                label = "SIP Needed Monthly",
                value = formatCurrency(calculatedPlan.monthlySip),
                isRejected = isRejected,
                adaptiveUi = adaptiveUi
            )
            if (isRejected) {
                Spacer(modifier = Modifier.height(12.dp.adaptive(adaptiveUi)))
                Text(
                    text = "* Enough funds are not available for this goal.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFFB3261E),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp.adaptive(adaptiveUi)
                    )
                )
                Spacer(modifier = Modifier.height(8.dp.adaptive(adaptiveUi)))
                Text(
                    text = "You need ${formatCurrency(shortfall)} more in your investable corpus to be able to achieve this goal.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = DeepCharcoal.copy(alpha = 0.74f),
                        lineHeight = 22.sp.adaptive(adaptiveUi),
                        fontSize = 13.sp.adaptive(adaptiveUi)
                    )
                )
                Spacer(modifier = Modifier.height(8.dp.adaptive(adaptiveUi)))
                Text(
                    text = "Funds available before this goal: ${formatCurrency(remainingCorpusBefore)}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = DeepCharcoal.copy(alpha = 0.56f),
                        fontSize = 11.sp.adaptive(adaptiveUi)
                    )
                )
            }
        }
    }
}

@Composable
private fun ResultPill(label: String, value: String, adaptiveUi: AdaptiveUi, isRejected: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp.adaptive(adaptiveUi)))
            .background(if (isRejected) Color(0xFFFFE9E7) else ChampagneGold.copy(alpha = 0.1f))
            .padding(horizontal = 14.dp.adaptive(adaptiveUi), vertical = 12.dp.adaptive(adaptiveUi)),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge.copy(
                color = DeepCharcoal,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp.adaptive(adaptiveUi)
            )
        )
        Text(
            text = value,
            modifier = Modifier.weight(1.3f),
            style = MaterialTheme.typography.bodyLarge.copy(
                color = if (isRejected) Color(0xFFB3261E) else ChampagneGold,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp.adaptive(adaptiveUi)
            ),
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun ContinueGoalCard(
    isRejected: Boolean,
    remainingCorpus: Double,
    adaptiveUi: AdaptiveUi,
    onChooseYes: () -> Unit,
    onChooseNo: () -> Unit
) {
    PlannerCard(adaptiveUi = adaptiveUi) {
        Text(
            text = if (isRejected) {
                "Do you want to try another goal?"
            } else {
                "You still have corpus left (${formatCurrency(remainingCorpus)}) - would you like to invest for another goal?"
            },
            style = MaterialTheme.typography.bodyLarge.copy(
                color = DeepCharcoal,
                lineHeight = 24.sp.adaptive(adaptiveUi),
                fontSize = 14.sp.adaptive(adaptiveUi)
            )
        )
        Spacer(modifier = Modifier.height(14.dp.adaptive(adaptiveUi)))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp.adaptive(adaptiveUi))
        ) {
            Button(
                onClick = onChooseYes,
                modifier = Modifier.width(120.dp.adaptive(adaptiveUi)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DeepCharcoal,
                    contentColor = ChampagneGold
                ),
                shape = RoundedCornerShape(12.dp.adaptive(adaptiveUi))
            ) {
                Text("Yes", fontSize = 14.sp.adaptive(adaptiveUi))
            }
            Button(
                onClick = onChooseNo,
                modifier = Modifier.width(120.dp.adaptive(adaptiveUi)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = DeepCharcoal
                ),
                shape = RoundedCornerShape(12.dp.adaptive(adaptiveUi)),
                border = borderStrokeAdaptive(1.dp, DeepCharcoal, adaptiveUi)
            ) {
                Text("No", fontSize = 14.sp.adaptive(adaptiveUi))
            }
        }
    }
}

@Composable
private fun GoalPlanSummary(
    plans: List<GoalPlan>,
    corpus: Double,
    totalSip: Double,
    remainingCorpus: Double,
    adaptiveUi: AdaptiveUi,
    onDownloadPlan: () -> Unit,
    onScheduleMeet: () -> Unit
) {
    PlannerCard(adaptiveUi = adaptiveUi) {
        Text(
            text = "Investment Summary",
            style = MaterialTheme.typography.headlineMedium.copy(
                color = DeepCharcoal,
                fontFamily = RefinedSerif,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp.adaptive(adaptiveUi)
            )
        )
        Spacer(modifier = Modifier.height(12.dp.adaptive(adaptiveUi)))
        SummaryTotals(
            corpus = corpus,
            totalSip = totalSip,
            remainingCorpus = remainingCorpus,
            adaptiveUi = adaptiveUi
        )
        Spacer(modifier = Modifier.height(18.dp.adaptive(adaptiveUi)))
        SummaryTable(plans = plans, adaptiveUi = adaptiveUi)
        Spacer(modifier = Modifier.height(18.dp.adaptive(adaptiveUi)))
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            CorpusUsageChart(
                totalSip = totalSip,
                corpus = corpus,
                adaptiveUi = adaptiveUi,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(14.dp.adaptive(adaptiveUi)))
            GoalMixChart(
                plans = plans,
                adaptiveUi = adaptiveUi,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Spacer(modifier = Modifier.height(18.dp.adaptive(adaptiveUi)))
        MonthlySipBarChart(plans = plans, adaptiveUi = adaptiveUi)
        Spacer(modifier = Modifier.height(20.dp.adaptive(adaptiveUi)))
        Button(
            onClick = onDownloadPlan,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = DeepCharcoal,
                contentColor = ChampagneGold
            ),
            shape = RoundedCornerShape(16.dp.adaptive(adaptiveUi))
        ) {
            Text("Download investment summary", fontSize = 14.sp.adaptive(adaptiveUi))
        }
        Spacer(modifier = Modifier.height(18.dp.adaptive(adaptiveUi)))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Wish to know more? ",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = DeepCharcoal.copy(alpha = 0.8f),
                    fontSize = 14.sp.adaptive(adaptiveUi)
                )
            )
            Text(
                text = "Schedule a meet",
                modifier = Modifier.clickable { onScheduleMeet() },
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = ChampagneGold,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = TextDecoration.Underline,
                    fontSize = 14.sp.adaptive(adaptiveUi)
                )
            )
        }
    }
}

@Composable
private fun SummaryTotals(
    corpus: Double,
    totalSip: Double,
    remainingCorpus: Double,
    adaptiveUi: AdaptiveUi
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp.adaptive(adaptiveUi))
    ) {
        SummaryMetric("Corpus", formatCurrency(corpus), adaptiveUi = adaptiveUi)
        SummaryMetric("Allocated SIP", formatCurrency(totalSip), adaptiveUi = adaptiveUi)
        SummaryMetric(
            "Balance",
            formatCurrency(if (remainingCorpus > 0) remainingCorpus else 0.0),
            adaptiveUi = adaptiveUi
        )
    }
}

@Composable
private fun SummaryMetric(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    adaptiveUi: AdaptiveUi
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp.adaptive(adaptiveUi)))
            .background(Color.White)
            .border(1.dp, DeepCharcoal.copy(alpha = 0.08f), RoundedCornerShape(18.dp.adaptive(adaptiveUi)))
            .padding(14.dp.adaptive(adaptiveUi))
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium.copy(
                color = DeepCharcoal.copy(alpha = 0.58f),
                fontSize = 12.sp.adaptive(adaptiveUi)
            )
        )
        Spacer(modifier = Modifier.height(6.dp.adaptive(adaptiveUi)))
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

@Composable
private fun SummaryTable(
    plans: List<GoalPlan>,
    adaptiveUi: AdaptiveUi
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp.adaptive(adaptiveUi))) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp.adaptive(adaptiveUi))
        ) {
            HeaderText("Goal", Modifier.weight(1f), adaptiveUi = adaptiveUi)
            HeaderText("Fund Type", Modifier.weight(1.2f), adaptiveUi = adaptiveUi)
            HeaderText("Monthly SIP", Modifier.weight(0.9f), textAlign = TextAlign.End, adaptiveUi = adaptiveUi)
        }

        plans.forEach { plan ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp.adaptive(adaptiveUi)))
                    .background(Color.White.copy(alpha = 0.92f))
                    .padding(horizontal = 12.dp.adaptive(adaptiveUi), vertical = 12.dp.adaptive(adaptiveUi)),
                horizontalArrangement = Arrangement.spacedBy(10.dp.adaptive(adaptiveUi)),
                verticalAlignment = Alignment.Top
            ) {
                BodyText(plan.goalName, Modifier.weight(1f), adaptiveUi = adaptiveUi)
                BodyText(plan.fundType, Modifier.weight(1.2f), adaptiveUi = adaptiveUi)
                BodyText(
                    formatCurrency(plan.monthlySip),
                    Modifier.weight(0.9f),
                    textAlign = TextAlign.End,
                    adaptiveUi = adaptiveUi
                )
            }
        }
    }
}

@Composable
private fun HeaderText(
    text: String,
    modifier: Modifier,
    adaptiveUi: AdaptiveUi,
    textAlign: TextAlign = TextAlign.Start
) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.labelLarge.copy(
            color = DeepCharcoal.copy(alpha = 0.7f),
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp.adaptive(adaptiveUi)
        ),
        textAlign = textAlign,
        maxLines = 2
    )
}

@Composable
private fun BodyText(
    text: String,
    modifier: Modifier,
    adaptiveUi: AdaptiveUi,
    textAlign: TextAlign = TextAlign.Start
) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.bodyMedium.copy(
            color = DeepCharcoal,
            fontSize = 12.sp.adaptive(adaptiveUi)
        ),
        textAlign = textAlign,
        maxLines = if (adaptiveUi.isVeryCompact) 4 else 3
    )
}

@Composable
private fun CorpusUsageChart(
    totalSip: Double,
    corpus: Double,
    adaptiveUi: AdaptiveUi,
    modifier: Modifier = Modifier
) {
    val safeCorpus = corpus.coerceAtLeast(1.0)
    val usage = (totalSip / safeCorpus).coerceAtMost(1.0).toFloat()
    val chartSize = if (adaptiveUi.isVeryCompact) 120.dp else 140.dp.adaptive(adaptiveUi)
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp.adaptive(adaptiveUi)))
            .background(Color.White.copy(alpha = 0.94f))
            .padding(16.dp.adaptive(adaptiveUi)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Corpus Usage",
            style = MaterialTheme.typography.titleMedium.copy(
                color = DeepCharcoal,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp.adaptive(adaptiveUi)
            )
        )
        Spacer(modifier = Modifier.height(10.dp.adaptive(adaptiveUi)))
        Box(
            modifier = Modifier.size(chartSize),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val strokeWidth = 16.dp.adaptive(adaptiveUi).toPx()
                drawArc(
                    color = DeepCharcoal.copy(alpha = 0.08f),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                drawArc(
                    color = ChampagneGold,
                    startAngle = -90f,
                    sweepAngle = usage * 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
            Text(
                text = "${(usage * 100).roundToInt()}%",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = ChampagneGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp.adaptive(adaptiveUi)
                )
            )
        }
    }
}

@Composable
private fun GoalMixChart(plans: List<GoalPlan>, adaptiveUi: AdaptiveUi, modifier: Modifier = Modifier) {
    val total = plans.sumOf { it.monthlySip }.coerceAtLeast(1.0)
    val chartSize = if (adaptiveUi.isVeryCompact) 120.dp else 140.dp.adaptive(adaptiveUi)
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp.adaptive(adaptiveUi)))
            .background(Color.White.copy(alpha = 0.94f))
            .padding(16.dp.adaptive(adaptiveUi))
    ) {
        Text(
            text = "Goal Mix",
            style = MaterialTheme.typography.titleMedium.copy(
                color = DeepCharcoal,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp.adaptive(adaptiveUi)
            )
        )
        Spacer(modifier = Modifier.height(10.dp.adaptive(adaptiveUi)))
        Canvas(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(chartSize)
        ) {
            var startAngle = -90f
            plans.forEach { plan ->
                val sweep = ((plan.monthlySip / total) * 360.0).toFloat()
                drawArc(
                    color = plan.color,
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = true
                )
                startAngle += sweep
            }
            drawCircle(
                color = Color.White,
                radius = size.minDimension * 0.24f
            )
        }
        Spacer(modifier = Modifier.height(12.dp.adaptive(adaptiveUi)))
        plans.forEach { plan ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp.adaptive(adaptiveUi)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp.adaptive(adaptiveUi))
                        .clip(CircleShape)
                        .background(plan.color)
                )
                Text(
                    text = plan.goalName,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = DeepCharcoal,
                        fontSize = 11.sp.adaptive(adaptiveUi)
                    ),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${((plan.monthlySip / total) * 100).roundToInt()}%",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = DeepCharcoal.copy(alpha = 0.65f),
                        fontSize = 11.sp.adaptive(adaptiveUi)
                    )
                )
            }
            Spacer(modifier = Modifier.height(6.dp.adaptive(adaptiveUi)))
        }
    }
}

@Composable
private fun MonthlySipBarChart(plans: List<GoalPlan>, adaptiveUi: AdaptiveUi) {
    val maxSip = plans.maxOfOrNull { it.monthlySip }?.coerceAtLeast(1.0) ?: 1.0
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp.adaptive(adaptiveUi)))
            .background(Color.White.copy(alpha = 0.94f))
            .padding(16.dp.adaptive(adaptiveUi))
    ) {
        Text(
            text = "Monthly SIP by Goal",
            style = MaterialTheme.typography.titleMedium.copy(
                color = DeepCharcoal,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp.adaptive(adaptiveUi)
            )
        )
        Spacer(modifier = Modifier.height(12.dp.adaptive(adaptiveUi)))
        plans.forEach { plan ->
            Text(
                text = "${plan.goalName} • ${formatCurrency(plan.monthlySip)}",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = DeepCharcoal,
                    fontSize = 13.sp.adaptive(adaptiveUi)
                )
            )
            Spacer(modifier = Modifier.height(6.dp.adaptive(adaptiveUi)))
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp.adaptive(adaptiveUi))
            ) {
                drawRoundRect(
                    color = DeepCharcoal.copy(alpha = 0.08f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(100f, 100f)
                )
                drawRoundRect(
                    color = plan.color,
                    size = Size(
                        width = size.width * (plan.monthlySip / maxSip).toFloat(),
                        height = size.height
                    ),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(100f, 100f)
                )
            }
            Spacer(modifier = Modifier.height(12.dp.adaptive(adaptiveUi)))
        }
    }
}

@Composable
private fun PlannerCard(adaptiveUi: AdaptiveUi, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp.adaptive(adaptiveUi)),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.96f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp.adaptive(adaptiveUi))
        ) {
            content()
        }
    }
}

@Composable
fun SecondaryScreenBackButton(onClick: () -> Unit) {
    Text(
        text = "← Back",
        modifier = Modifier.clickable { onClick() },
        style = MaterialTheme.typography.bodyLarge.copy(
            color = DeepCharcoal.copy(alpha = 0.6f),
            fontWeight = FontWeight.Medium
        )
    )
}

@Composable
private fun borderStrokeAdaptive(width: androidx.compose.ui.unit.Dp, color: Color, ui: AdaptiveUi) = 
    androidx.compose.foundation.BorderStroke(width.adaptive(ui), color)

private fun buildGoalPlan(input: GoalInputState, profile: String): GoalPlan? {
    val goalName = input.goalName ?: return null
    if (goalName == "None") return null
    val expected = input.expectedEarningsInput.toDoubleOrNull()?.takeIf { it > 0 } ?: return null
    val years = input.timeAvailableInput.toDoubleOrNull()?.takeIf { it > 0 } ?: return null
    val annualRate = annualRateForGoal(goalName = goalName, profile = profile)
    val sip = calculateSip(
        annualRate = annualRate,
        years = years,
        expectedEarnings = expected
    )
    val fundType = fundTypeForGoal(goalName = goalName, profile = profile)
    val color = goalDefinitions.firstOrNull { it.name == goalName }?.color ?: ChampagneGold
    return GoalPlan(
        goalName = goalName,
        expectedEarnings = expected,
        timeAvailableYears = years,
        annualRate = annualRate,
        fundType = fundType,
        monthlySip = sip,
        color = color
    )
}

private fun normalizePlanProfile(profile: String): String = when (profile.lowercase()) {
    "conservative" -> "Conservative"
    "aggressive", "high", "high risk" -> "Aggressive"
    else -> "Moderate"
}

private fun goalNameFromSlug(goalSlug: String?): String? = when (goalSlug) {
    "safety_net" -> "Safety Net"
    "big_day_fund" -> "Big Day Fund"
    "travel_dreams" -> "Travel Dreams"
    "my_childs_future" -> "My Child's Future"
    "my_first_home" -> "My First Home"
    "prosperity_planning" -> "Prosperity Planning"
    else -> null
}

private fun annualRateForGoal(goalName: String, profile: String): Double = when (goalName) {
    "Safety Net" -> when (profile) {
        "Conservative" -> 0.12
        "Moderate" -> 0.14
        else -> 0.16
    }
    "Travel Dreams", "Prosperity Planning" -> 0.16
    "My Child's Future", "My First Home", "Big Day Fund" -> 0.14
    else -> 0.14
}

private fun fundTypeForGoal(goalName: String, profile: String): String = when (goalName) {
    "Safety Net", "Big Day Fund" -> "Balanced Alpha Portfolio"
    "Travel Dreams", "Prosperity Planning" -> "Alpha Optimisation Portfolio"
    else -> "All Cap Alpha Portfolio"
}

private fun calculateSip(
    annualRate: Double,
    years: Double,
    expectedEarnings: Double
): Double {
    val rate = annualRate / 12.0
    val periods = years * 12.0
    if (periods <= 0.0) return 0.0
    val growth = Math.pow(1 + rate, periods)
    val payment = expectedEarnings * rate / ((growth - 1) * (1 + rate))
    return abs(payment)
}

private fun exportInvestmentSummary(
    context: Context,
    profile: String,
    age: Int,
    corpus: Double,
    totalSip: Double,
    remainingCorpus: Double,
    plans: List<GoalPlan>
): String? = runCatching {
    val timestamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault()).format(Date())
    val fileName = "sava-investment-summary-$timestamp.pdf"
    val summaryLines = buildInvestmentSummaryLines(
        profile = profile,
        age = age,
        corpus = corpus,
        totalSip = totalSip,
        remainingCorpus = remainingCorpus,
        plans = plans
    )

    val values = ContentValues().apply {
        put(MediaStore.Downloads.DISPLAY_NAME, fileName)
        put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }
    }

    val resolver = context.contentResolver
    val collection = MediaStore.Downloads.EXTERNAL_CONTENT_URI
    val uri = resolver.insert(collection, values) ?: return null
    resolver.openOutputStream(uri)?.use { outputStream ->
        val pdfDocument = createInvestmentSummaryPdf(summaryLines)
        pdfDocument.writeTo(outputStream)
        pdfDocument.close()
    } ?: return null

    fileName
}.getOrNull()

private fun buildInvestmentSummaryLines(
    profile: String,
    age: Int,
    corpus: Double,
    totalSip: Double,
    remainingCorpus: Double,
    plans: List<GoalPlan>
): List<String> = buildList {
    add("SaWa Investment Summary")
    add("")
    add("Optimum Risk Capability: $profile")
    add("Age: $age")
    add("Corpus per month: ${formatCurrency(corpus)}")
    add("Allocated SIP: ${formatCurrency(totalSip)}")
    add("Balance: ${formatCurrency(remainingCorpus.coerceAtLeast(0.0))}")
    add("")
    add("Recommended Goals")
    add("-----------------")
    plans.forEachIndexed { index, plan ->
        add("${index + 1}. ${plan.goalName}")
        add("Expected Corpus: ${formatCurrency(plan.expectedEarnings)}")
        add("Time Available: ${plan.timeAvailableYears} years")
        add("Fund Type: ${plan.fundType}")
        add("Monthly SIP: ${formatCurrency(plan.monthlySip)}")
        add("")
    }
}

private fun createInvestmentSummaryPdf(lines: List<String>): PdfDocument {
    val document = PdfDocument()
    val pageWidth = 595
    val pageHeight = 842
    val leftMargin = 48f
    val topMargin = 56f
    val rightMargin = 48f
    val bottomMargin = 56f
    val maxTextWidth = pageWidth - leftMargin - rightMargin

    val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.BLACK
        textSize = 20f
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
    }
    val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.BLACK
        textSize = 12f
    }
    val headingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.BLACK
        textSize = 13f
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
    }

    var pageNumber = 1
    var page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
    var canvas = page.canvas
    var y = topMargin

    fun startNewPage() {
        document.finishPage(page)
        pageNumber += 1
        page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
        canvas = page.canvas
        y = topMargin
    }

    fun ensureSpace(lineHeight: Float) {
        if (y + lineHeight > pageHeight - bottomMargin) {
            startNewPage()
        }
    }

    fun drawWrappedLine(text: String, paint: Paint) {
        val wrapped = wrapPdfText(text, paint, maxTextWidth)
        val lineHeight = paint.textSize * 1.45f
        wrapped.forEach { line ->
            ensureSpace(lineHeight)
            canvas.drawText(line, leftMargin, y, paint)
            y += lineHeight
        }
    }

    lines.forEachIndexed { index, line ->
        when {
            index == 0 -> {
                drawWrappedLine(line, titlePaint)
                y += 8f
            }
            line.isBlank() -> {
                y += 8f
            }
            line == "Recommended Goals" -> {
                drawWrappedLine(line, headingPaint)
            }
            line == "-----------------" -> {
                ensureSpace(12f)
                canvas.drawLine(leftMargin, y, pageWidth - rightMargin, y, bodyPaint)
                y += 16f
            }
            Regex("""\d+\..+""").matches(line) -> {
                y += 4f
                drawWrappedLine(line, headingPaint)
            }
            else -> drawWrappedLine(line, bodyPaint)
        }
    }

    document.finishPage(page)
    return document
}

private fun wrapPdfText(text: String, paint: Paint, maxWidth: Float): List<String> {
    if (text.isBlank()) return listOf("")
    val words = text.split(Regex("\\s+"))
    val lines = mutableListOf<String>()
    var currentLine = ""

    words.forEach { word ->
        val candidate = if (currentLine.isEmpty()) word else "$currentLine $word"
        if (paint.measureText(candidate) <= maxWidth) {
            currentLine = candidate
        } else {
            if (currentLine.isNotEmpty()) {
                lines += currentLine
            }
            currentLine = word
        }
    }

    if (currentLine.isNotEmpty()) {
        lines += currentLine
    }
    return lines
}

private fun formatCurrency(value: Double): String = "Rs\u00A0${"%,.0f".format(value)}"

private fun dropdownGoalsForIndex(
    goalInputs: List<GoalInputState>,
    evaluations: List<GoalEvaluation>,
    currentIndex: Int
): List<GoalDefinition> {
    val selectedGoals = goalInputs.mapIndexedNotNull { index, input ->
        val wasAccepted = evaluations.getOrNull(index)?.isRejected == false
        input.goalName?.takeIf { it != "None" && index != currentIndex && wasAccepted }
    }.toSet()
    val baseGoals = goalDefinitions.filter { it.name !in selectedGoals }
    return if (currentIndex > 0) {
        baseGoals + GoalDefinition("None", DeepCharcoal.copy(alpha = 0.4f))
    } else {
        baseGoals
    }
}

private fun evaluateGoals(
    goalInputs: List<GoalInputState>,
    profile: String,
    corpus: Double
): List<GoalEvaluation> {
    var remaining = corpus
    val evaluations = mutableListOf<GoalEvaluation>()

    goalInputs.forEachIndexed { index, input ->
        val plan = buildGoalPlan(input, profile)
        val isRejected = plan != null && plan.monthlySip > remaining
        val shortfall = if (isRejected) plan!!.monthlySip - remaining else 0.0
        val remainingAfter = if (plan != null && !isRejected) remaining - plan.monthlySip else remaining
        val acceptedGoalNames = evaluations.mapNotNull { evaluation ->
            evaluation.input.goalName?.takeIf { it != "None" && !evaluation.isRejected }
        }.toMutableSet()
        if (input.goalName != null && input.goalName != "None" && !isRejected) {
            acceptedGoalNames += input.goalName
        }
        val availableAfterCurrent = goalDefinitions.filter { it.name !in acceptedGoalNames }

        evaluations += GoalEvaluation(
            input = input,
            plan = plan,
            availableCorpusBefore = remaining,
            remainingCorpusAfter = remainingAfter,
            isRejected = isRejected,
            shortfall = shortfall,
            availableGoalsAfter = availableAfterCurrent
        )

        if (plan != null && !isRejected) {
            remaining = remainingAfter
        }
    }

    return evaluations
}

private fun calculateVisibleGoalCount(
    goalInputs: List<GoalInputState>,
    evaluations: List<GoalEvaluation>
): Int {
    if (goalInputs.isEmpty()) return 1

    var count = 1
    for (index in goalInputs.indices) {
        val input = goalInputs[index]
        val evaluation = evaluations.getOrNull(index)
        count = index + 1

        if (input.goalName == null) break
        if (input.goalName == "None") break
        if (evaluation?.plan == null) break

        val canPromptForNext =
            evaluation.availableGoalsAfter.isNotEmpty() &&
                (evaluation.isRejected || evaluation.remainingCorpusAfter > 0)

        if (canPromptForNext && input.continueWithAnotherGoal == true) {
            count = index + 2
            continue
        }

        break
    }
    return count.coerceAtLeast(1)
}

private fun shouldShowSummary(
    visibleInputs: List<GoalInputState>,
    evaluations: List<GoalEvaluation>,
    acceptedPlans: List<GoalPlan>
): Boolean {
    if (acceptedPlans.isEmpty() || visibleInputs.isEmpty()) return false

    val lastInput = visibleInputs.last()
    val lastEvaluation = evaluations.lastOrNull() ?: return false

    if (lastInput.goalName == "None") return true
    if (lastInput.goalName == null || lastEvaluation.plan == null) return false

    val canPromptForNext =
        lastEvaluation.availableGoalsAfter.isNotEmpty() &&
            (lastEvaluation.isRejected || lastEvaluation.remainingCorpusAfter > 0)

    return when {
        canPromptForNext && lastInput.continueWithAnotherGoal == false -> true
        !canPromptForNext -> true
        else -> false
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun TailoredPlansScreenPreview() {
    SavaTheme {
        TailoredPlansScreen(
            profile = "Moderate",
            age = 30,
            investableCorpusPerMonth = 25000.0,
            onBack = {}
        )
    }
}
