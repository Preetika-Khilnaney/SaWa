package com.example.sava.ui.screens

import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sava.ui.theme.ChampagneGold
import com.example.sava.ui.theme.DeepCharcoal
import com.example.sava.ui.theme.OffWhite
import com.example.sava.ui.theme.RefinedSerif
import com.example.sava.ui.theme.adaptive
import com.example.sava.ui.theme.rememberAdaptiveUi

@Composable
fun EmailLinkCallbackScreen(
    onBack: () -> Unit,
    onComplete: (String) -> Unit
) {
    val adaptiveUi = rememberAdaptiveUi()
    var email by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OffWhite)
            .padding(24.dp.adaptive(adaptiveUi))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {
            SecondaryScreenBackButton(onClick = onBack)
            Spacer(modifier = Modifier.height(36.dp.adaptive(adaptiveUi)))

            Text(
                text = "Complete verification",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontFamily = RefinedSerif,
                    fontSize = 32.sp.adaptive(adaptiveUi),
                    fontWeight = FontWeight.Bold,
                    color = DeepCharcoal
                )
            )

            Spacer(modifier = Modifier.height(10.dp.adaptive(adaptiveUi)))

            Text(
                text = "Enter the email address that received this link so we can finish verifying it in the app.",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = DeepCharcoal.copy(alpha = 0.68f),
                    fontFamily = RefinedSerif,
                    fontSize = 16.sp.adaptive(adaptiveUi),
                    lineHeight = 27.sp.adaptive(adaptiveUi)
                )
            )

            Spacer(modifier = Modifier.height(30.dp.adaptive(adaptiveUi)))

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it.trim()
                    errorMessage = null
                },
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(10.dp.adaptive(adaptiveUi)))
                Text(
                    text = errorMessage.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFFC84B4B),
                        fontSize = 13.sp.adaptive(adaptiveUi)
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp.adaptive(adaptiveUi)))

            Button(
                onClick = {
                    if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        errorMessage = "Enter the same email address that received the link."
                        return@Button
                    }
                    onComplete(email)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp.adaptive(adaptiveUi)),
                shape = RoundedCornerShape(18.dp.adaptive(adaptiveUi)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ChampagneGold,
                    contentColor = DeepCharcoal
                )
            ) {
                Text(
                    text = "Verify email",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp.adaptive(adaptiveUi)
                    )
                )
            }
        }
    }
}
