package com.example.sava.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.sava.auth.UserProfileStore
import com.example.sava.ui.theme.ChampagneGold
import com.example.sava.ui.theme.DeepCharcoal
import com.example.sava.ui.theme.OffWhite
import com.example.sava.ui.theme.RefinedSerif
import com.example.sava.ui.theme.SavaTheme
import com.example.sava.ui.theme.adaptive
import com.example.sava.ui.theme.rememberAdaptiveUi
import com.example.sava.ui.theme.AdaptiveUi
import kotlinx.coroutines.delay

private data class QuizOption(
    val code: String,
    val label: String,
    val points: Int
)

private data class RiskQuestion(
    val section: String,
    val prompt: String,
    val options: List<QuizOption>
)

private data class AssessmentInsights(
    val totalScore: Int,
    val profile: String,
    val confidenceScore: Int,
    val flags: List<String>
)

private val riskQuestions = listOf(
    RiskQuestion(
        section = "Section 1: Time Horizon",
        prompt = "When do you expect to need most of this invested money?",
        options = listOf(
            QuizOption("A", "Within 1-3 years", 1),
            QuizOption("B", "3-7 years", 2),
            QuizOption("C", "7+ years", 3)
        )
    ),
    RiskQuestion(
        section = "Section 2: Income Stability",
        prompt = "Your primary source of savings comes from:",
        options = listOf(
            QuizOption("A", "Fixed salary / stable income", 1),
            QuizOption("B", "Salary + variable income / business", 2),
            QuizOption("C", "Business / variable / high upside income", 3)
        )
    ),
    RiskQuestion(
        section = "Section 3: Market Reaction",
        prompt = "If your portfolio falls by 20% in a short time, what would you do?",
        options = listOf(
            QuizOption("A", "Withdraw or stop investing", 1),
            QuizOption("B", "Stay invested, wait for recovery", 2),
            QuizOption("C", "Invest more (buy the dip)", 3)
        )
    ),
    RiskQuestion(
        section = "Section 4: Return Expectation",
        prompt = "What kind of returns do you expect from your investments?",
        options = listOf(
            QuizOption("A", "Stable 8-10% returns", 1),
            QuizOption("B", "10-12% with moderate fluctuations", 2),
            QuizOption("C", "12%+ with significant ups and downs", 3)
        )
    ),
    RiskQuestion(
        section = "Section 5: Investment Experience",
        prompt = "Your experience with market-linked investments:",
        options = listOf(
            QuizOption("A", "None or very limited", 1),
            QuizOption("B", "Some experience (mutual funds, SIPs)", 2),
            QuizOption("C", "Active investor (stocks, thematic funds, etc.)", 3)
        )
    ),
    RiskQuestion(
        section = "Section 6: Financial Goals",
        prompt = "Your primary goal is:",
        options = listOf(
            QuizOption("A", "Capital protection", 1),
            QuizOption("B", "Balanced growth", 2),
            QuizOption("C", "Wealth creation / aggressive growth", 3)
        )
    ),
    RiskQuestion(
        section = "Section 7: Emergency Preparedness",
        prompt = "How many months of expenses do you have as emergency funds?",
        options = listOf(
            QuizOption("A", "Less than 3 months", 1),
            QuizOption("B", "3-6 months", 2),
            QuizOption("C", "More than 6 months", 3)
        )
    ),
    RiskQuestion(
        section = "Section 8: Risk Comfort",
        prompt = "Which statement resonates most with you?",
        options = listOf(
            QuizOption("A", "I prefer safety over high returns", 1),
            QuizOption("B", "I want growth but not extreme volatility", 2),
            QuizOption("C", "I'm comfortable taking risks for higher returns", 3)
        )
    )
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun RiskQuizScreen(
    onComplete: (String, Int, Long) -> Unit,
    onBack: () -> Unit,
    onScheduleMeet: () -> Unit = {}
) {
    val adaptiveUi = rememberAdaptiveUi()
    var currentStep by rememberSaveable { mutableStateOf(0) }
    var ageInput by rememberSaveable { mutableStateOf("") }
    var corpusInput by rememberSaveable { mutableStateOf("") }
    var ageError by rememberSaveable { mutableStateOf<String?>(null) }
    var corpusError by rememberSaveable { mutableStateOf<String?>(null) }
    var responses by rememberSaveable { mutableStateOf(List(riskQuestions.size) { -1 }) }
    var isSubmitting by rememberSaveable { mutableStateOf(false) }
    var insights by remember { mutableStateOf<AssessmentInsights?>(null) }

    val currentQuestionIndex = (currentStep - 1).coerceAtLeast(0)
    val progress = if (currentStep == 0) 0f else currentStep / riskQuestions.size.toFloat()
    val canMoveNext = if (currentStep == 0) {
        parseValidatedAge(ageInput) != null && parseValidatedCorpus(corpusInput) != null
    } else {
        responses[currentQuestionIndex] >= 0
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFFCF7), OffWhite, Color(0xFFF7F1E6))
                )
            )
            .padding(20.dp.adaptive(adaptiveUi))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(18.dp.adaptive(adaptiveUi))
        ) {
            SecondaryScreenBackButton(onClick = onBack)
            QuizHeader(onScheduleMeet = onScheduleMeet, adaptiveUi = adaptiveUi)
            if (currentStep > 0) {
                QuizProgress(
                    questionIndex = currentStep,
                    questionCount = riskQuestions.size,
                    sectionLabel = riskQuestions[currentQuestionIndex].section,
                    progress = progress,
                    adaptiveUi = adaptiveUi
                )
            }

            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    (slideInHorizontally(
                        animationSpec = tween(320, easing = FastOutSlowInEasing),
                        initialOffsetX = { it / 3 }
                    ) + fadeIn()).togetherWith(
                        slideOutHorizontally(
                            animationSpec = tween(220, easing = FastOutSlowInEasing),
                            targetOffsetX = { -it / 5 }
                        ) + fadeOut()
                    )
                },
                label = "quiz_step"
            ) { step ->
                if (step == 0) {
                    AgeInputCard(
                        ageInput = ageInput,
                        corpusInput = corpusInput,
                        errorText = ageError,
                        corpusErrorText = corpusError,
                        onAgeChange = {
                            ageInput = it.filter(Char::isDigit).take(3)
                            ageError = null
                        },
                        onCorpusChange = {
                            corpusInput = it.filter(Char::isDigit).take(9)
                            corpusError = null
                        },
                        adaptiveUi = adaptiveUi
                    )
                } else {
                    QuestionCard(
                        question = riskQuestions[step - 1],
                        selectedIndex = responses[step - 1],
                        adaptiveUi = adaptiveUi,
                        onSelect = { selected ->
                            responses = responses.toMutableList().also { it[step - 1] = selected }
                        }
                    )
                }
            }

            NavigationControls(
                showPrevious = currentStep > 0,
                nextLabel = if (currentStep == riskQuestions.size) "Submit Assessment" else "Next",
                nextEnabled = canMoveNext,
                adaptiveUi = adaptiveUi,
                onPrevious = {
                    if (currentStep > 0) currentStep--
                },
                onNext = {
                    if (currentStep == 0) {
                        val validAge = parseValidatedAge(ageInput)
                        val validCorpus = parseValidatedCorpus(corpusInput)
                        if (validAge == null || validCorpus == null) {
                            ageError = if (validAge == null) {
                                "Please enter a valid age to continue"
                            } else {
                                null
                            }
                            corpusError = if (validCorpus == null) {
                                "Please enter a valid monthly investable corpus"
                            } else {
                                null
                            }
                        } else {
                            ageError = null
                            corpusError = null
                            currentStep = 1
                        }
                    } else if (currentStep < riskQuestions.size) {
                        currentStep++
                    } else {
                        val validAge = parseValidatedAge(ageInput)
                        val validCorpus = parseValidatedCorpus(corpusInput)
                        if (validAge != null && validCorpus != null) {
                            insights = calculateAssessment(responses)
                            UserProfileStore.updateInvestableCorpusForCurrentUser(validCorpus)
                            isSubmitting = true
                        } else {
                            currentStep = 0
                            ageError = if (validAge == null) {
                                "Please enter a valid age to continue"
                            } else {
                                null
                            }
                            corpusError = if (validCorpus == null) {
                                "Please enter a valid monthly investable corpus"
                            } else {
                                null
                            }
                        }
                    }
                }
            )
        }

        if (isSubmitting) {
            BlackHoleEffect(adaptiveUi = adaptiveUi) {
                val validAge = parseValidatedAge(ageInput) ?: 30
                val validCorpus = parseValidatedCorpus(corpusInput) ?: 0L
                onComplete(insights?.profile ?: "Moderate", validAge, validCorpus)
            }
        }
    }
}

@Composable
private fun QuizHeader(onScheduleMeet: () -> Unit, adaptiveUi: AdaptiveUi) {
    var showInfoDialog by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(24.dp.adaptive(adaptiveUi)),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.96f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp.adaptive(adaptiveUi))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp.adaptive(adaptiveUi)),
            verticalArrangement = Arrangement.spacedBy(10.dp.adaptive(adaptiveUi))
        ) {
            Text(
                text = "Investor Risk Profiling",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontFamily = RefinedSerif,
                    fontSize = 25.sp.adaptive(adaptiveUi),
                    lineHeight = 36.sp.adaptive(adaptiveUi),
                    fontWeight = FontWeight.Bold,
                    color = DeepCharcoal
                )
            )
            Text(
                text = "Discover your ideal investment strategy",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = ChampagneGold,
                    fontFamily = RefinedSerif,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp.adaptive(adaptiveUi),
                    lineHeight = 23.sp.adaptive(adaptiveUi)
                )
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showInfoDialog = true },
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Risk profiling information",
                    tint = DeepCharcoal.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp.adaptive(adaptiveUi))
                )
                Spacer(modifier = Modifier.size(6.dp.adaptive(adaptiveUi)))
                Text(
                    text = "Why is risk profiling important?",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = DeepCharcoal.copy(alpha = 0.65f),
                        fontSize = 13.sp.adaptive(adaptiveUi),
                        lineHeight = 18.sp.adaptive(adaptiveUi)
                    )
                )
            }
        }
    }

    if (showInfoDialog) {
        RiskInfoDialog(
            onDismiss = { showInfoDialog = false },
            onScheduleMeet = onScheduleMeet,
            adaptiveUi = adaptiveUi
        )
    }
}

@Composable
private fun RiskInfoDialog(
    onDismiss: () -> Unit,
    onScheduleMeet: () -> Unit,
    adaptiveUi: AdaptiveUi
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp.adaptive(adaptiveUi)),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp.adaptive(adaptiveUi))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp.adaptive(adaptiveUi)),
                verticalArrangement = Arrangement.spacedBy(16.dp.adaptive(adaptiveUi))
            ) {
                Text(
                    text = "Most people loose money due to investing outside their risk capabilities. In fact according to FINRA Foundation, about 55% of consumers are unable to correctly recognize risk-mitigation strategies.",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = DeepCharcoal,
                        fontFamily = RefinedSerif,
                        fontSize = 19.sp.adaptive(adaptiveUi),
                        lineHeight = 28.sp.adaptive(adaptiveUi)
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Want to know more? ",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = DeepCharcoal.copy(alpha = 0.62f),
                            fontSize = 11.sp.adaptive(adaptiveUi)
                        )
                    )
                    Text(
                        text = "Schedule a meet",
                        modifier = Modifier.clickable {
                            onDismiss()
                            onScheduleMeet()
                        },
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = ChampagneGold,
                            fontWeight = FontWeight.SemiBold,
                            textDecoration = TextDecoration.Underline,
                            fontSize = 11.sp.adaptive(adaptiveUi)
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun AgeInputCard(
    ageInput: String,
    corpusInput: String,
    errorText: String?,
    corpusErrorText: String?,
    onAgeChange: (String) -> Unit,
    onCorpusChange: (String) -> Unit,
    adaptiveUi: AdaptiveUi
) {
    QuizCardShell(adaptiveUi = adaptiveUi) {
        Text(
            text = "Enter Your Age",
            style = MaterialTheme.typography.headlineMedium.copy(
                color = DeepCharcoal,
                fontFamily = RefinedSerif,
                fontWeight = FontWeight.Bold,
                fontSize = 21.sp.adaptive(adaptiveUi),
                lineHeight = 27.sp.adaptive(adaptiveUi)
            )
        )
        OutlinedTextField(
            value = ageInput,
            onValueChange = onAgeChange,
            label = { Text("Enter Your Age", fontSize = 12.sp.adaptive(adaptiveUi)) },
            singleLine = true,
            isError = errorText != null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp.adaptive(adaptiveUi)),
            textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp.adaptive(adaptiveUi))
        )
        if (errorText != null) {
            Spacer(modifier = Modifier.height(10.dp.adaptive(adaptiveUi)))
            Text(
                text = errorText,
                color = Color(0xFFB3261E),
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp.adaptive(adaptiveUi))
            )
        }

        Spacer(modifier = Modifier.height(18.dp.adaptive(adaptiveUi)))

        Text(
            text = "Investable Corpus (per month)",
            style = MaterialTheme.typography.headlineMedium.copy(
                color = DeepCharcoal,
                fontFamily = RefinedSerif,
                fontWeight = FontWeight.Bold,
                fontSize = 21.sp.adaptive(adaptiveUi),
                lineHeight = 27.sp.adaptive(adaptiveUi)
            )
        )
        Spacer(modifier = Modifier.height(8.dp.adaptive(adaptiveUi)))
        Text(
            text = "Enter the amount you can invest every month. ",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = DeepCharcoal.copy(alpha = 0.65f),
                fontFamily = RefinedSerif,
                fontSize = 13.sp.adaptive(adaptiveUi),
                lineHeight = 19.sp.adaptive(adaptiveUi)
            )
        )
        Spacer(modifier = Modifier.height(10.dp.adaptive(adaptiveUi)))
        OutlinedTextField(
            value = corpusInput,
            onValueChange = onCorpusChange,
            label = { Text("Investable Corpus (per month)", fontSize = 12.sp.adaptive(adaptiveUi)) },
            singleLine = true,
            isError = corpusErrorText != null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp.adaptive(adaptiveUi)),
            textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp.adaptive(adaptiveUi))
        )
        if (corpusErrorText != null) {
            Spacer(modifier = Modifier.height(10.dp.adaptive(adaptiveUi)))
            Text(
                text = corpusErrorText,
                color = Color(0xFFB3261E),
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp.adaptive(adaptiveUi))
            )
        }
    }
}

@Composable
private fun QuizProgress(
    questionIndex: Int,
    questionCount: Int,
    sectionLabel: String,
    progress: Float,
    adaptiveUi: AdaptiveUi
) {
    Card(
        shape = RoundedCornerShape(22.dp.adaptive(adaptiveUi)),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.94f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp.adaptive(adaptiveUi), vertical = 16.dp.adaptive(adaptiveUi)),
            verticalArrangement = Arrangement.spacedBy(10.dp.adaptive(adaptiveUi))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Question $questionIndex of $questionCount",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = DeepCharcoal,
                        fontFamily = RefinedSerif,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp.adaptive(adaptiveUi)
                    )
                )
                Text(
                    text = sectionLabel,
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = ChampagneGold,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.6.sp,
                        fontSize = 12.sp.adaptive(adaptiveUi)
                    ),
                    textAlign = TextAlign.End
                )
            }
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp.adaptive(adaptiveUi))
                    .clip(RoundedCornerShape(999.dp)),
                color = ChampagneGold,
                trackColor = DeepCharcoal.copy(alpha = 0.08f)
            )
        }
    }
}

@Composable
private fun QuestionCard(
    question: RiskQuestion,
    selectedIndex: Int,
    adaptiveUi: AdaptiveUi,
    onSelect: (Int) -> Unit
) {
    QuizCardShell(adaptiveUi = adaptiveUi) {
        Text(
            text = question.section,
            style = MaterialTheme.typography.labelLarge.copy(
                color = ChampagneGold,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                fontSize = 14.sp.adaptive(adaptiveUi)
            )
        )
        Spacer(modifier = Modifier.height(6.dp.adaptive(adaptiveUi)))
        Text(
            text = question.prompt,
            style = MaterialTheme.typography.headlineMedium.copy(
                color = DeepCharcoal,
                fontFamily = RefinedSerif,
                fontWeight = FontWeight.Bold,
                fontSize = 21.sp.adaptive(adaptiveUi),
                lineHeight = 27.sp.adaptive(adaptiveUi)
            )
        )
        Spacer(modifier = Modifier.height(18.dp.adaptive(adaptiveUi)))
        question.options.forEachIndexed { index, option ->
            OptionCard(
                option = option,
                selected = selectedIndex == index,
                adaptiveUi = adaptiveUi,
                onClick = { onSelect(index) }
            )
            Spacer(modifier = Modifier.height(12.dp.adaptive(adaptiveUi)))
        }
        Text(
            text = "Choose the option that best reflects your natural reaction, not what you think is ideal.",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = DeepCharcoal.copy(alpha = 0.56f),
                fontFamily = RefinedSerif,
                fontSize = 14.sp.adaptive(adaptiveUi),
                lineHeight = 20.sp.adaptive(adaptiveUi)
            )
        )
    }
}

@Composable
private fun OptionCard(
    option: QuizOption,
    selected: Boolean,
    adaptiveUi: AdaptiveUi,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp.adaptive(adaptiveUi)))
            .background(if (selected) ChampagneGold.copy(alpha = 0.14f) else Color.White)
            .border(
                width = 1.dp.adaptive(adaptiveUi),
                color = if (selected) ChampagneGold else DeepCharcoal.copy(alpha = 0.08f),
                shape = RoundedCornerShape(18.dp.adaptive(adaptiveUi))
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp.adaptive(adaptiveUi), vertical = 14.dp.adaptive(adaptiveUi)),
        horizontalArrangement = Arrangement.spacedBy(14.dp.adaptive(adaptiveUi)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp.adaptive(adaptiveUi))
                .clip(CircleShape)
                .background(if (selected) ChampagneGold else ChampagneGold.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = option.code,
                style = MaterialTheme.typography.labelLarge.copy(
                    color = if (selected) OffWhite else ChampagneGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp.adaptive(adaptiveUi)
                )
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = option.label,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = DeepCharcoal,
                    fontFamily = RefinedSerif,
                    lineHeight = 23.sp.adaptive(adaptiveUi),
                    fontSize = 16.sp.adaptive(adaptiveUi)
                )
            )
        }
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = ChampagneGold,
                unselectedColor = DeepCharcoal.copy(alpha = 0.3f)
            ),
            modifier = Modifier.size(24.dp.adaptive(adaptiveUi))
        )
    }
}

@Composable
private fun NavigationControls(
    showPrevious: Boolean,
    nextLabel: String,
    nextEnabled: Boolean,
    adaptiveUi: AdaptiveUi,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp.adaptive(adaptiveUi))
    ) {
        if (showPrevious) {
            OutlinedButton(
                onClick = onPrevious,
                modifier = Modifier.weight(1f).height(50.dp.adaptive(adaptiveUi)),
                shape = RoundedCornerShape(16.dp.adaptive(adaptiveUi))
            ) {
                Text("Previous", fontSize = 14.sp.adaptive(adaptiveUi))
            }
        }
        Button(
            onClick = onNext,
            enabled = nextEnabled,
            modifier = Modifier.weight(if (showPrevious) 1f else 2f).height(50.dp.adaptive(adaptiveUi)),
            colors = ButtonDefaults.buttonColors(
                containerColor = DeepCharcoal,
                contentColor = ChampagneGold,
                disabledContainerColor = DeepCharcoal.copy(alpha = 0.25f),
                disabledContentColor = ChampagneGold.copy(alpha = 0.55f)
            ),
            shape = RoundedCornerShape(16.dp.adaptive(adaptiveUi))
        ) {
            Text(
                text = nextLabel,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    fontSize = 14.sp.adaptive(adaptiveUi)
                )
            )
        }
    }
}

@Composable
private fun QuizCardShell(adaptiveUi: AdaptiveUi, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp.adaptive(adaptiveUi)),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.96f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp.adaptive(adaptiveUi))
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

private fun parseValidatedAge(ageInput: String): Int? {
    val parsed = ageInput.toIntOrNull() ?: return null
    return parsed.takeIf { it in 18..100 }
}

private fun parseValidatedCorpus(corpusInput: String): Long? {
    val parsed = corpusInput.toLongOrNull() ?: return null
    return parsed.takeIf { it > 0L }
}

private fun calculateAssessment(responses: List<Int>): AssessmentInsights {
    val totalScore = responses.mapIndexed { index, answerIndex ->
        riskQuestions[index].options.getOrNull(answerIndex)?.points ?: 0
    }.sum()

    val profile = when (totalScore) {
        in 8..13 -> "Conservative"
        in 14..19 -> "Moderate"
        else -> "Aggressive"
    }

    val q3 = responses.getOrNull(2)
    val q4 = responses.getOrNull(3)
    val q8 = responses.getOrNull(7)
    val flags = buildList {
        if (q3 != null && q4 != null && q8 != null) {
            val mismatch = maxOf(q3, q4, q8) - minOf(q3, q4, q8) >= 2
            if (mismatch) {
                add("Risk preference inconsistency detected")
            }
            if (q3 == 0 && q4 == 2) {
                add("User expects high returns but has low tolerance for losses")
            }
            if (q3 == 2 && q8 == 0) {
                add("User is behaviorally opportunistic but self-identifies as highly risk averse")
            }
        }
    }

    val confidenceScore = (100 - (flags.size * 15)).coerceIn(55, 100)

    return AssessmentInsights(
        totalScore = totalScore,
        profile = profile,
        confidenceScore = confidenceScore,
        flags = flags
    )
}

@Composable
fun BlackHoleEffect(adaptiveUi: AdaptiveUi, onFinished: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "blackhole")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(1000, easing = LinearEasing)),
        label = "rotation"
    )

    var startScaling by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (startScaling) 0f else 1f,
        animationSpec = tween(1500, easing = FastOutSlowInEasing),
        label = "scale"
    )

    LaunchedEffect(Unit) {
        startScaling = true
        delay(1600)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.9f)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(200.dp.adaptive(adaptiveUi))
                .graphicsLayer {
                    rotationZ = rotation
                    scaleX = scale
                    scaleY = scale
                }
                .background(
                    brush = androidx.compose.ui.graphics.Brush.sweepGradient(
                        colors = listOf(Color.Transparent, ChampagneGold, Color.Transparent)
                    ),
                    shape = CircleShape
                )
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun RiskQuizScreenPreview() {
    SavaTheme {
        RiskQuizScreen(onComplete = { _, _, _ -> }, onBack = {}, onScheduleMeet = {})
    }
}
