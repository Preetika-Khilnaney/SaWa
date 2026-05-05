package com.example.sava.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class AdaptiveUi(
    val screenWidthDp: Int,
    val screenHeightDp: Int,
    val isNarrow: Boolean,
    val isCompact: Boolean,
    val isVeryCompact: Boolean,
    val isExpanded: Boolean,
    val spacingScale: Float,
    val textScale: Float,
    val maxContentWidth: Dp
)

@Composable
fun rememberAdaptiveUi(): AdaptiveUi {
    val configuration = LocalConfiguration.current
    return remember(configuration.screenWidthDp, configuration.screenHeightDp) {
        val widthDp = configuration.screenWidthDp
        val heightDp = configuration.screenHeightDp
        val narrow = widthDp < 430
        val veryCompact = widthDp < 380 || heightDp < 680
        val compact = widthDp < 400 || heightDp < 760
        val expanded = widthDp >= 411

        AdaptiveUi(
            screenWidthDp = widthDp,
            screenHeightDp = heightDp,
            isNarrow = narrow,
            isCompact = compact,
            isVeryCompact = veryCompact,
            isExpanded = expanded,
            spacingScale = when {
                veryCompact -> 0.85f
                narrow -> 0.90f
                compact -> 0.94f
                expanded -> 1.05f
                else -> 1f
            },
            textScale = when {
                veryCompact -> 0.85f
                narrow -> 0.92f
                compact -> 0.96f
                expanded -> 1.05f
                else -> 1f
            },
            maxContentWidth = 560.dp
        )
    }
}

fun Dp.adaptive(ui: AdaptiveUi): Dp = (value * ui.spacingScale).dp

fun TextUnit.adaptive(ui: AdaptiveUi): TextUnit {
    if (type != TextUnitType.Sp) return this
    return (value * ui.textScale).sp
}
