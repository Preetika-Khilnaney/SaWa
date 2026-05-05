package com.example.sava.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.sava.ui.theme.DeepCharcoal
import com.example.sava.ui.theme.adaptive
import com.example.sava.ui.theme.rememberAdaptiveUi

@Composable
fun SecondaryScreenBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val adaptiveUi = rememberAdaptiveUi()
    Box(
        modifier = modifier
            .size(42.dp.adaptive(adaptiveUi))
            .background(Color.White.copy(alpha = 0.94f), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = DeepCharcoal,
            modifier = Modifier.padding(2.dp.adaptive(adaptiveUi))
        )
    }
}
