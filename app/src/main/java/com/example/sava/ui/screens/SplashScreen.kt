package com.example.sava.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sava.R
import com.example.sava.ui.theme.ChampagneGold
import com.example.sava.ui.theme.DeepCharcoal
import com.example.sava.ui.theme.OffWhite
import com.example.sava.ui.theme.RefinedSerif
import com.example.sava.ui.theme.SavaTheme
import com.example.sava.ui.theme.adaptive
import com.example.sava.ui.theme.rememberAdaptiveUi
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onNext: () -> Unit) {
    val adaptiveUi = rememberAdaptiveUi()
    var startAnimation by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (startAnimation) 0f else 360f,
        animationSpec = tween(durationMillis = 500, easing = LinearEasing),
        label = "rotation"
    )
    
    val appName = "SAWA"
    val displayedLetters = remember { mutableStateListOf<Int>() }

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(500)
        appName.forEachIndexed { index, _ ->
            delay(100)
            displayedLetters.add(index)
        }
        delay(1000)
        onNext()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OffWhite),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 24.dp.adaptive(adaptiveUi))
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(160.dp.adaptive(adaptiveUi))
                    .graphicsLayer {
                        rotationY = rotation
                        rotationX = rotation / 2
                    }
            )
            
            Spacer(modifier = Modifier.height(14.dp.adaptive(adaptiveUi)))
            
            Row {
                appName.forEachIndexed { index, char ->
                    AnimatedVisibility(
                        visible = index in displayedLetters,
                        enter = slideInVertically(
                            initialOffsetY = { -it },
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                        ) + fadeIn(),
                        label = "letter_$index"
                    ) {
                        Text(
                            text = char.toString(),
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontFamily = RefinedSerif,
                                fontWeight = FontWeight.Normal,
                                fontSize = 56.sp.adaptive(adaptiveUi),
                                letterSpacing = 5.sp.adaptive(adaptiveUi),
                                color = DeepCharcoal,
                                platformStyle = PlatformTextStyle(includeFontPadding = false)
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp.adaptive(adaptiveUi)))

            Text(
                text = "— COINVEST WEALTH —",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = ChampagneGold,
                    fontFamily = RefinedSerif,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 10.sp.adaptive(adaptiveUi),
                    letterSpacing = 2.8.sp.adaptive(adaptiveUi),
                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp.adaptive(adaptiveUi)))

            Text(
                text = buildAnnotatedString {
                    append("TRANSPARENCY")
                    pushStyle(SpanStyle(color = ChampagneGold, fontWeight = FontWeight.Bold))
                    append(" | ")
                    pop()
                    append("ALIGNMENT")
                    pushStyle(SpanStyle(color = ChampagneGold, fontWeight = FontWeight.Bold))
                    append(" | \n")
                    pop()
                    append("WEALTH CREATION")
                },
                style = MaterialTheme.typography.labelLarge.copy(
                    color = DeepCharcoal.copy(alpha = 0.72f),
                    fontSize = 8.sp.adaptive(adaptiveUi),
                    letterSpacing = 1.2.sp.adaptive(adaptiveUi),
                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                    lineHeight = 14.sp.adaptive(adaptiveUi)
                ),
                textAlign = TextAlign.Center
            )
        }
        
        // Lens flare simulation
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(2.dp.adaptive(adaptiveUi))
                .background(
                    brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                        colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.5f), Color.Transparent)
                    )
                )
                .offset(y = (-50).dp.adaptive(adaptiveUi))
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SplashScreenPreview() {
    SavaTheme {
        SplashScreen(onNext = {})
    }
}
