package com.example.sava.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sava.auth.GoogleAuthManager
import com.example.sava.ui.theme.ChampagneGold
import com.example.sava.ui.theme.DeepCharcoal
import com.example.sava.ui.theme.OffWhite
import com.example.sava.ui.theme.RefinedSerif
import com.example.sava.ui.theme.adaptive
import com.example.sava.ui.theme.rememberAdaptiveUi
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ProfileScreen(onSignedOut: () -> Unit) {
    val adaptiveUi = rememberAdaptiveUi()
    val user = FirebaseAuth.getInstance().currentUser

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFFCF7), OffWhite, Color(0xFFF7F1E6))
                )
            )
            .padding(20.dp.adaptive(adaptiveUi)),
        verticalArrangement = Arrangement.spacedBy(16.dp.adaptive(adaptiveUi))
    ) {
        Text(
            text = "Profile",
            style = MaterialTheme.typography.displayLarge.copy(
                color = DeepCharcoal,
                fontFamily = RefinedSerif,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp.adaptive(adaptiveUi)
            )
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp.adaptive(adaptiveUi)),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.96f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp.adaptive(adaptiveUi))
        ) {
            Column(
                modifier = Modifier.padding(20.dp.adaptive(adaptiveUi)),
                verticalArrangement = Arrangement.spacedBy(8.dp.adaptive(adaptiveUi))
            ) {
                Text(
                    text = user?.displayName?.takeIf { it.isNotBlank() } ?: "SaWa Investor",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = DeepCharcoal,
                        fontWeight = FontWeight.Bold,
                        fontSize = MaterialTheme.typography.titleLarge.fontSize.adaptive(adaptiveUi)
                    )
                )
                Text(
                    text = user?.email ?: "No email available",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = DeepCharcoal.copy(alpha = 0.68f),
                        fontSize = 14.sp.adaptive(adaptiveUi)
                    )
                )
                Spacer(modifier = Modifier.height(8.dp.adaptive(adaptiveUi)))
                Text(
                    text = "Use this space to manage your account and sign out securely whenever you need to.",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = DeepCharcoal.copy(alpha = 0.72f),
                        lineHeight = 22.sp.adaptive(adaptiveUi),
                        fontSize = 14.sp.adaptive(adaptiveUi)
                    )
                )
                Spacer(modifier = Modifier.height(12.dp.adaptive(adaptiveUi)))
                Button(
                    onClick = {
                        GoogleAuthManager.signOut()
                        onSignedOut()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp.adaptive(adaptiveUi)),
                    shape = RoundedCornerShape(16.dp.adaptive(adaptiveUi)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DeepCharcoal,
                        contentColor = ChampagneGold
                    )
                ) {
                    Text(
                        "Sign out",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontSize = 14.sp.adaptive(adaptiveUi)
                        )
                    )
                }
            }
        }
    }
}
