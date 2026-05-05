package com.example.sava.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sava.auth.InvestmentPlanStore
import com.example.sava.auth.SavedInvestmentPlan
import com.example.sava.ui.theme.ChampagneGold
import com.example.sava.ui.theme.DeepCharcoal
import com.example.sava.ui.theme.OffWhite
import com.example.sava.ui.theme.RefinedSerif
import com.example.sava.ui.theme.adaptive
import com.example.sava.ui.theme.rememberAdaptiveUi
import com.example.sava.ui.theme.AdaptiveUi
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun YourPlansScreen() {
    val adaptiveUi = rememberAdaptiveUi()
    var plans by remember { mutableStateOf<List<SavedInvestmentPlan>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        InvestmentPlanStore.fetchPlansForCurrentUser(
            onSuccess = {
                plans = it
                errorMessage = null
            },
            onError = {
                errorMessage = it
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFFCF7), OffWhite, Color(0xFFF7F1E6))
                )
            )
            .padding(
                horizontal = 20.dp.adaptive(adaptiveUi),
                vertical = 18.dp.adaptive(adaptiveUi)
            ),
        verticalArrangement = Arrangement.spacedBy(16.dp.adaptive(adaptiveUi))
    ) {
        item {
            Text(
                text = "Your Plans",
                style = MaterialTheme.typography.displayLarge.copy(
                    color = DeepCharcoal,
                    fontFamily = RefinedSerif,
                    fontSize = 28.sp.adaptive(adaptiveUi),
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.height(8.dp.adaptive(adaptiveUi)))
            Text(
                text = "Every generated plan is stored here so you can revisit your previous allocations and goals.",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = DeepCharcoal.copy(alpha = 0.68f),
                    fontSize = 14.sp.adaptive(adaptiveUi),
                    lineHeight = 22.sp.adaptive(adaptiveUi)
                )
            )
        }

        if (!errorMessage.isNullOrBlank()) {
            item {
                EmptyMessageCard(
                    message = errorMessage ?: "Something went wrong while loading your plans.",
                    adaptiveUi = adaptiveUi
                )
            }
        } else if (plans.isEmpty()) {
            item {
                EmptyMessageCard(
                    message = "No saved plans yet. Generate a plan from your risk result screen and it will appear here.",
                    adaptiveUi = adaptiveUi
                )
            }
        } else {
            itemsIndexed(plans, key = { _, plan -> plan.id }) { index, plan ->
                SavedPlanCard(
                    plan = plan,
                    planNumber = index + 1,
                    adaptiveUi = adaptiveUi,
                    onDelete = {
                        InvestmentPlanStore.deletePlan(
                            plan = plan,
                            onSuccess = {
                                plans = plans.filterNot { it.id == plan.id }
                                errorMessage = null
                            },
                            onError = {
                                errorMessage = it
                            }
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun SavedPlanCard(
    plan: SavedInvestmentPlan,
    planNumber: Int,
    adaptiveUi: AdaptiveUi,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp.adaptive(adaptiveUi)),
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
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp.adaptive(adaptiveUi))
                ) {
                    Text(
                        text = "Plan Number: $planNumber",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = DeepCharcoal,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp.adaptive(adaptiveUi)
                        )
                    )
                    Text(
                        text = formatPlanDate(plan.createdAtMillis),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = DeepCharcoal.copy(alpha = 0.62f),
                            fontSize = 12.sp.adaptive(adaptiveUi)
                        )
                    )
                    Text(
                        text = "Profile: ${plan.optimumRiskProfile}",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = ChampagneGold,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp.adaptive(adaptiveUi)
                        )
                    )
                }
                Spacer(modifier = Modifier.width(12.dp.adaptive(adaptiveUi)))
                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF8E4E1),
                        contentColor = Color(0xFF9F2D20)
                    ),
                    shape = RoundedCornerShape(12.dp.adaptive(adaptiveUi)),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 12.dp.adaptive(adaptiveUi),
                        vertical = 6.dp.adaptive(adaptiveUi)
                    )
                ) {
                    Text(
                        "Delete",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontSize = 12.sp.adaptive(adaptiveUi)
                        )
                    )
                }
            }
            SummaryRow(
                label = "Monthly Corpus",
                value = formatPlanCurrency(plan.investableCorpusPerMonth),
                adaptiveUi = adaptiveUi
            )
            SummaryRow(
                label = "Allocated SIP",
                value = formatPlanCurrency(plan.allocatedSip),
                adaptiveUi = adaptiveUi
            )
            SummaryRow(
                label = "Corpus Left",
                value = formatPlanCurrency(plan.remainingCorpus),
                adaptiveUi = adaptiveUi
            )

            Spacer(modifier = Modifier.height(4.dp.adaptive(adaptiveUi)))

            plan.goals.forEach { goal ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = DeepCharcoal.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(18.dp.adaptive(adaptiveUi))
                        )
                        .background(Color(0xFFFFFCF8), RoundedCornerShape(18.dp.adaptive(adaptiveUi)))
                        .padding(14.dp.adaptive(adaptiveUi))
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp.adaptive(adaptiveUi))) {
                        Text(
                            text = goal.goalName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = ChampagneGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp.adaptive(adaptiveUi)
                            )
                        )
                        SummaryRow(label = "Fund Type", value = goal.fundType, adaptiveUi = adaptiveUi)
                        SummaryRow(label = "Expected Corpus", value = formatPlanCurrency(goal.expectedEarnings), adaptiveUi = adaptiveUi)
                        SummaryRow(label = "Time Available", value = "${goal.timeAvailableYears} years", adaptiveUi = adaptiveUi)
                        SummaryRow(label = "Monthly SIP", value = formatPlanCurrency(goal.monthlySip), adaptiveUi = adaptiveUi)
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String, adaptiveUi: AdaptiveUi) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium.copy(
                color = DeepCharcoal.copy(alpha = 0.62f),
                fontSize = 13.sp.adaptive(adaptiveUi)
            )
        )
        Text(
            text = value,
            modifier = Modifier.weight(1.3f),
            style = MaterialTheme.typography.bodyMedium.copy(
                color = DeepCharcoal,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp.adaptive(adaptiveUi)
            ),
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun EmptyMessageCard(message: String, adaptiveUi: AdaptiveUi) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp.adaptive(adaptiveUi)),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.96f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(20.dp.adaptive(adaptiveUi)),
            style = MaterialTheme.typography.bodyLarge.copy(
                color = DeepCharcoal.copy(alpha = 0.74f),
                fontSize = 14.sp.adaptive(adaptiveUi),
                lineHeight = 22.sp.adaptive(adaptiveUi)
            )
        )
    }
}

private fun formatPlanCurrency(value: Double): String = "Rs\u00A0${"%,.0f".format(value)}"

private fun formatPlanDate(createdAtMillis: Long): String {
    if (createdAtMillis <= 0L) return "Date unavailable"
    return SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(createdAtMillis))
}
