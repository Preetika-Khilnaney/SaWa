package com.example.sava.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sava.R
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
fun HomeScreen(
    onStartRiskQuiz: () -> Unit,
    onSignOut: () -> Unit,
    onGoalCardClick: (String) -> Unit
) {
    val adaptiveUi = rememberAdaptiveUi()
    @Suppress("UNUSED_VARIABLE")
    val signOutCallback = onSignOut
    var hasExistingAssessment by remember { mutableStateOf(false) }
    val imageCards = remember {
        listOf(
            HomeGoalCard(R.drawable.safety, "safety_net"),
            HomeGoalCard(R.drawable.big, "big_day_fund"),
            HomeGoalCard(R.drawable.travel, "travel_dreams"),
            HomeGoalCard(R.drawable.child, "my_childs_future"),
            HomeGoalCard(R.drawable.house, "my_first_home"),
            HomeGoalCard(R.drawable.prosperity, "prosperity_planning")
        )
    }

    LaunchedEffect(Unit) {
        RiskAssessmentStore.fetchLatestAssessment(
            onFound = { hasExistingAssessment = true },
            onNotFound = { hasExistingAssessment = false },
            onError = { hasExistingAssessment = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFFCF7), OffWhite, Color(0xFFF9F4EB))
                )
            )
            .verticalScroll(rememberScrollState())
            .padding(
                start = 24.dp.adaptive(adaptiveUi),
                top = 46.dp.adaptive(adaptiveUi),
                end = 24.dp.adaptive(adaptiveUi),
                bottom = 28.dp.adaptive(adaptiveUi)
            )
            .widthIn(max = adaptiveUi.maxContentWidth)
    ) {
        HeroSection(adaptiveUi)

        Spacer(modifier = Modifier.height(24.dp.adaptive(adaptiveUi)))

        SectionDivider(
            title = "Invest with Clarity. Grow with Confidence.",
            adaptiveUi = adaptiveUi
        )

        Spacer(modifier = Modifier.height(20.dp.adaptive(adaptiveUi)))

        RiskProfileButton(
            buttonText = if (hasExistingAssessment) {
                "Recalculate your risk profile"
            } else {
                "Calculate your risk profile"
            },
            onClick = onStartRiskQuiz,
            adaptiveUi = adaptiveUi
        )

        Spacer(modifier = Modifier.height(26.dp.adaptive(adaptiveUi)))

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(end = 10.dp.adaptive(adaptiveUi)),
            horizontalArrangement = Arrangement.spacedBy(12.dp.adaptive(adaptiveUi))
        ) {
            items(items = imageCards, key = { it.imageRes }) { card ->
                HomeImageCard(
                    imageRes = card.imageRes,
                    modifier = Modifier.width(
                        if (adaptiveUi.isCompact) 208.dp else if (adaptiveUi.isExpanded) 252.dp else 240.dp
                    ),
                    onClick = { onGoalCardClick(card.goalSlug) }
                )
            }
        }

        Spacer(modifier = Modifier.height(34.dp.adaptive(adaptiveUi)))

        CommitmentCard(adaptiveUi)

        Spacer(modifier = Modifier.height(28.dp.adaptive(adaptiveUi)))

        Text(
            text = "SaWa is currently not a SEBI registered investment advisor. Investment security markets are subject to market risks. Read all related documents carefully before investing.\n\nThe recommendations are based on available data and use our best judgement. This is not a guarantee of returns in any way. Investors are advised to conduct their own due diligence before making any investment decision.\n\nThe information provided is only for knowledge purposes.",
            style = MaterialTheme.typography.bodySmall.copy(
                color = DeepCharcoal.copy(alpha = 0.62f),
                fontSize = 11.sp.adaptive(adaptiveUi),
                lineHeight = 16.sp.adaptive(adaptiveUi),
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp.adaptive(adaptiveUi)))
    }
}

private data class HomeGoalCard(
    val imageRes: Int,
    val goalSlug: String
)

@Composable
private fun HeroSection(adaptiveUi: AdaptiveUi) {
    val titleStyle = MaterialTheme.typography.displayLarge.copy(
        fontFamily = RefinedSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp.adaptive(adaptiveUi),
        color = DeepCharcoal,
        lineHeight = 39.sp.adaptive(adaptiveUi)
    )
    val bodyStyle = MaterialTheme.typography.bodyLarge.copy(
        color = DeepCharcoal.copy(alpha = 0.66f),
        fontFamily = RefinedSerif,
        fontSize = 15.sp.adaptive(adaptiveUi),
        textAlign = TextAlign.Justify,
        lineHeight = 23.sp.adaptive(adaptiveUi),
        platformStyle = PlatformTextStyle(includeFontPadding = false)
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = buildAnnotatedString {
                append("About ")
                pushStyle(SpanStyle(color = ChampagneGold))
                append("Us")
                pop()
            },
            style = titleStyle,
            modifier = Modifier.fillMaxWidth(),
            maxLines = 1
        )

        Spacer(modifier = Modifier.height(18.dp.adaptive(adaptiveUi)))

        Text(
            text = "At SaWa, we bridge the gap between financial goals and reality by curating customised investment plans tailored to your unique profile and provide a seamless digital platform to execute those investments instantly.",
            modifier = Modifier.fillMaxWidth(),
            style = bodyStyle
        )
    }
}

@Composable
private fun SectionDivider(
    title: String,
    adaptiveUi: AdaptiveUi
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontFamily = FontFamily.Cursive,
                fontWeight = FontWeight.SemiBold,
                fontSize = when {
                    adaptiveUi.isVeryCompact -> 13.sp
                    adaptiveUi.isCompact -> 16.sp
                    adaptiveUi.isExpanded -> 22.sp
                    else -> 19.sp
                }.adaptive(adaptiveUi),
                lineHeight = when {
                    adaptiveUi.isVeryCompact -> 18.sp
                    adaptiveUi.isCompact -> 22.sp
                    adaptiveUi.isExpanded -> 28.sp
                    else -> 25.sp
                }.adaptive(adaptiveUi),
                color = ChampagneGold
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Clip
        )
    }
}

@Composable
private fun RiskProfileButton(
    buttonText: String,
    onClick: () -> Unit,
    adaptiveUi: AdaptiveUi
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp.adaptive(adaptiveUi))
            .clip(RoundedCornerShape(18.dp.adaptive(adaptiveUi)))
            .background(Color.White.copy(alpha = 0.9f))
            .border(
                border = BorderStroke(1.5.dp.adaptive(adaptiveUi), Color.Gray),
                shape = RoundedCornerShape(18.dp.adaptive(adaptiveUi))
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = buttonText,
            style = MaterialTheme.typography.labelLarge.copy(
                color = DeepCharcoal,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp.adaptive(adaptiveUi)
            )
        )
    }
}

@Composable
private fun HomeImageCard(
    imageRes: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val cardShape = RoundedCornerShape(18.dp)
    val painter = painterResource(id = imageRes)
    val imageAspectRatio = remember(painter) {
        val size = painter.intrinsicSize
        if (size.isSpecified && size.height > 0f) size.width / size.height else 0.58f
    }

    Card(
        modifier = modifier
            .shadow(
                elevation = 10.dp,
                shape = cardShape,
                ambientColor = Color(0x338E785A),
                spotColor = Color(0x558E785A)
            )
            .graphicsLayer {
                shadowElevation = 20.dp.toPx()
                shape = cardShape
                clip = false
                translationX = -6f // moves shadow to the right
            }
            .border(1.dp, Color.White.copy(alpha = 0.92f), cardShape)
            .clip(cardShape)
            .clickable { onClick() },
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.96f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.98f),
                            Color(0xFFFFFBF4)
                        )
                    )
                )
        ) {
            Image(
                painter = painter,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(imageAspectRatio),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
private fun CommitmentCard(adaptiveUi: AdaptiveUi) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                10.dp.adaptive(adaptiveUi),
                RoundedCornerShape(22.dp.adaptive(adaptiveUi)),
                clip = false
            ),
        shape = RoundedCornerShape(22.dp.adaptive(adaptiveUi)),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.96f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = if (adaptiveUi.isVeryCompact) 14.dp else 18.dp.adaptive(adaptiveUi),
                    vertical = if (adaptiveUi.isVeryCompact) 12.dp else 14.dp.adaptive(adaptiveUi)
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(
                if (adaptiveUi.isVeryCompact) 10.dp else 14.dp.adaptive(adaptiveUi)
            )
        ) {
            CommitmentIcon(adaptiveUi)
            CommitmentText(adaptiveUi)
        }
    }
}

@Composable
private fun CommitmentIcon(adaptiveUi: AdaptiveUi) {
    Box(
        modifier = Modifier
            .size(
                when {
                    adaptiveUi.isVeryCompact -> 58.dp
                    adaptiveUi.isCompact -> 68.dp
                    else -> 84.dp.adaptive(adaptiveUi)
                }
            )
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        ChampagneGold.copy(alpha = 0.2f),
                        ChampagneGold.copy(alpha = 0.05f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(
                    when {
                        adaptiveUi.isVeryCompact -> 34.dp
                        adaptiveUi.isCompact -> 40.dp
                        else -> 48.dp.adaptive(adaptiveUi)
                    }
                )
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.65f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = ChampagneGold,
                modifier = Modifier.size(
                    when {
                        adaptiveUi.isVeryCompact -> 18.dp
                        adaptiveUi.isCompact -> 22.dp
                        else -> 26.dp.adaptive(adaptiveUi)
                    }
                )
            )
        }
    }
}

@Composable
private fun CommitmentText(adaptiveUi: AdaptiveUi) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = 4.dp.adaptive(adaptiveUi)),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Our Commitment",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontFamily = RefinedSerif,
                fontWeight = FontWeight.Bold,
                fontSize = if (adaptiveUi.isVeryCompact) 17.sp else 20.sp.adaptive(adaptiveUi),
                color = DeepCharcoal
            ),
            textAlign = TextAlign.Start
        )

        Spacer(modifier = Modifier.height(if (adaptiveUi.isVeryCompact) 4.dp else 6.dp.adaptive(adaptiveUi)))

        Text(
            text = "Transparency, trust, and technology empowering you to grow wealth with confidence.",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontFamily = RefinedSerif,
                fontSize = if (adaptiveUi.isVeryCompact) 10.sp else 12.sp.adaptive(adaptiveUi),
                lineHeight = if (adaptiveUi.isVeryCompact) 15.sp else 18.sp.adaptive(adaptiveUi),
                color = DeepCharcoal.copy(alpha = 0.64f)
            ),
            textAlign = TextAlign.Start
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun HomeScreenPreview() {
    SavaTheme {
        HomeScreen(onStartRiskQuiz = {}, onSignOut = {}, onGoalCardClick = {})
    }
}
