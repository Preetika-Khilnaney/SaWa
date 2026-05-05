package com.example.sava.ui.screens

import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sava.R
import com.example.sava.auth.GoogleAuthManager
import com.example.sava.ui.theme.*
import com.example.sava.ui.theme.adaptive
import com.example.sava.ui.theme.rememberAdaptiveUi

@Composable
fun UserGateScreen(
    onRegister: () -> Unit,
    onLogin: () -> Unit,
    onGoogleLoginSuccess: () -> Unit
) {
    val adaptiveUi = rememberAdaptiveUi()
    val context = LocalContext.current
    val isInPreview = LocalInspectionMode.current
    val activity = if (isInPreview) null else context as? ComponentActivity
    var isGoogleLoading by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OffWhite)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp.adaptive(adaptiveUi)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(64.dp.adaptive(adaptiveUi)))

            // Heading Section - Adaptive sizes
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = buildAnnotatedString {
                        append("Invest Smarter,\n")
                        withStyle(style = SpanStyle(color = ChampagneGold)) {
                            append("Build Wealth")
                        }
                        append("\nFaster.")
                    },
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 38.sp.adaptive(adaptiveUi),
                        lineHeight = 46.sp.adaptive(adaptiveUi),
                        fontFamily = RefinedSerif,
                        fontWeight = FontWeight.Bold,
                        color = DeepCharcoal
                    )
                )

                Spacer(modifier = Modifier.height(16.dp.adaptive(adaptiveUi)))

                Text(
                    text = "Your journey to financial freedom starts here.",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = DeepCharcoal.copy(alpha = 0.6f),
                        fontFamily = RefinedSerif,
                        fontSize = 18.sp.adaptive(adaptiveUi),
                        lineHeight = 26.sp.adaptive(adaptiveUi)
                    )
                )
            }

            Spacer(modifier = Modifier.height(32.dp.adaptive(adaptiveUi)))

            // Main Illustration - Edge to edge and prominent
            Image(
                painter = painterResource(id = R.drawable.img),
                contentDescription = "Wealth Growth Illustration",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp.adaptive(adaptiveUi)),
                contentScale = ContentScale.FillWidth
            )

            Spacer(modifier = Modifier.height(40.dp.adaptive(adaptiveUi)))

            // Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp.adaptive(adaptiveUi)),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                GateButtonStyled(
                    text = "Create Account",
                    backgroundColor = Brush.verticalGradient(listOf(Color(0xFFEBC15A), Color(0xFFD4AF37))),
                    contentColor = Color.White,
                    adaptiveUi = adaptiveUi,
                    onClick = onRegister
                )

                GateButtonStyled(
                    text = "Login",
                    backgroundColor = Brush.verticalGradient(listOf(Color.White, Color.White)),
                    contentColor = ChampagneGold,
                    isOutlined = true,
                    borderColor = ChampagneGold.copy(alpha = 0.6f),
                    adaptiveUi = adaptiveUi,
                    onClick = onLogin
                )

                GateButtonStyled(
                    text = if (isGoogleLoading) "Connecting..." else "Continue with Google",
                    backgroundColor = Brush.verticalGradient(listOf(Color.White, Color.White)),
                    contentColor = DeepCharcoal,
                    borderColor = Color(0xFFD2D2D2),
                    adaptiveUi = adaptiveUi,
                    leadingIcon = {
                        Image(
                            painter = painterResource(id = R.drawable.google),
                            contentDescription = "Google",
                            modifier = Modifier.size(26.dp.adaptive(adaptiveUi)),
                            contentScale = ContentScale.Fit
                        )
                    },
                    onClick = {
                        if (isGoogleLoading) return@GateButtonStyled

                        if (activity == null) {
                            if (!isInPreview) {
                                Toast.makeText(
                                    context,
                                    "Google Sign-In is unavailable on this screen.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            return@GateButtonStyled
                        }

                        isGoogleLoading = true
                        GoogleAuthManager.signIn(
                            activity = activity,
                            onSuccess = {
                                isGoogleLoading = false
                                onGoogleLoginSuccess()
                            },
                            onError = { message ->
                                isGoogleLoading = false
                                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                            }
                        )
                    }
                )

                Spacer(modifier = Modifier.height(8.dp.adaptive(adaptiveUi)))

                SecurityBadge(adaptiveUi)
                
                Spacer(modifier = Modifier.height(56.dp.adaptive(adaptiveUi)))
            }
        }
    }
}

@Composable
fun GateButtonStyled(
    text: String,
    backgroundColor: Brush,
    contentColor: Color,
    adaptiveUi: com.example.sava.ui.theme.AdaptiveUi,
    isOutlined: Boolean = false,
    borderColor: Color = ChampagneGold,
    leadingIcon: (@Composable () -> Unit)? = null,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(24.dp.adaptive(adaptiveUi))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp.adaptive(adaptiveUi))
            .clip(shape)
            .then(
                if (isOutlined) {
                    Modifier
                        .background(Color.White)
                        .border(1.5.dp.adaptive(adaptiveUi), borderColor, shape)
                } else {
                    Modifier
                        .background(backgroundColor)
                        .border(1.dp.adaptive(adaptiveUi), borderColor, shape)
                }
            )
            .clickable { onClick() }
            .padding(horizontal = 18.dp.adaptive(adaptiveUi)),
        contentAlignment = Alignment.Center
    ) {
        leadingIcon?.let {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(20.dp.adaptive(adaptiveUi)),
                contentAlignment = Alignment.Center
            ) {
                it()
            }
        }

        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge.copy(
                color = contentColor,
                fontSize = 15.sp.adaptive(adaptiveUi),
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.align(Alignment.Center)
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(17.dp.adaptive(adaptiveUi))
        )
    }
}

@Composable
fun SecurityBadge(adaptiveUi: com.example.sava.ui.theme.AdaptiveUi) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(36.dp.adaptive(adaptiveUi))
                .clip(CircleShape)
                .background(ChampagneGold.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = ChampagneGold,
                modifier = Modifier.size(18.dp.adaptive(adaptiveUi))
            )
        }
        Spacer(modifier = Modifier.width(12.dp.adaptive(adaptiveUi)))
        Text(
            text = "Your data is always protected",
            style = MaterialTheme.typography.labelSmall.copy(
                color = DeepCharcoal.copy(alpha = 0.5f),
                fontSize = 13.sp.adaptive(adaptiveUi)
            )
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun UserGateScreenPreview() {
    SavaTheme {
        UserGateScreen(onRegister = {}, onLogin = {}, onGoogleLoginSuccess = {})
    }
}
