package com.example.sava.ui.screens

import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sava.ui.theme.ChampagneGold
import com.example.sava.ui.theme.DeepCharcoal
import com.example.sava.ui.theme.OffWhite
import com.example.sava.ui.theme.RefinedSerif
import com.example.sava.ui.theme.SavaTheme
import com.example.sava.ui.theme.adaptive
import com.example.sava.ui.theme.rememberAdaptiveUi
import com.example.sava.ui.theme.AdaptiveUi
import com.google.firebase.auth.FirebaseAuth

@Composable
fun EmailLoginScreen(
    onLoginSuccess: () -> Unit,
    initialEmail: String? = null,
    onBack: () -> Unit,
    onSignUp: () -> Unit
) {
    val adaptiveUi = rememberAdaptiveUi()
    val auth = remember { FirebaseAuth.getInstance() }
    val scrollState = rememberScrollState()

    var email by remember { mutableStateOf(initialEmail.orEmpty()) }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var helperMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OffWhite)
            .padding(24.dp.adaptive(adaptiveUi))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.Top
        ) {
            SecondaryScreenBackButton(onClick = onBack)
            Spacer(modifier = Modifier.height(34.dp.adaptive(adaptiveUi)))

            Text(
                text = "Welcome back",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontFamily = RefinedSerif,
                    fontSize = 34.sp.adaptive(adaptiveUi),
                    fontWeight = FontWeight.Bold,
                    color = DeepCharcoal
                )
            )

            Spacer(modifier = Modifier.height(8.dp.adaptive(adaptiveUi)))

            Text(
                text = "Enter your email and password to continue with your personal account.",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = DeepCharcoal.copy(alpha = 0.62f),
                    fontFamily = RefinedSerif,
                    fontSize = 16.sp.adaptive(adaptiveUi),
                    lineHeight = 28.sp.adaptive(adaptiveUi)
                )
            )

            Spacer(modifier = Modifier.height(36.dp.adaptive(adaptiveUi)))

            AuthTextField(
                value = email,
                onValueChange = {
                    email = it.trim()
                    errorMessage = null
                    helperMessage = null
                },
                label = "Email",
                keyboardType = KeyboardType.Email,
                adaptiveUi = adaptiveUi
            )

            Spacer(modifier = Modifier.height(18.dp.adaptive(adaptiveUi)))

            AuthTextField(
                value = password,
                onValueChange = {
                    password = it
                    errorMessage = null
                    helperMessage = null
                },
                label = "Password",
                keyboardType = KeyboardType.Password,
                isPassword = true,
                adaptiveUi = adaptiveUi
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

            if (helperMessage != null) {
                Spacer(modifier = Modifier.height(10.dp.adaptive(adaptiveUi)))
                Text(
                    text = helperMessage.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = DeepCharcoal.copy(alpha = 0.55f),
                        fontSize = 13.sp.adaptive(adaptiveUi)
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp.adaptive(adaptiveUi)))

            Text(
                text = "Forgot Password",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = ChampagneGold,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = TextDecoration.Underline,
                    fontSize = 14.sp.adaptive(adaptiveUi)
                ),
                modifier = Modifier.clickable {
                    errorMessage = null
                    helperMessage = null

                    if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        helperMessage = "Enter your email first so we can send a reset link."
                        return@clickable
                    }

                    auth.sendPasswordResetEmail(email)
                        .addOnSuccessListener {
                            helperMessage = "Password reset link sent to $email"
                        }
                        .addOnFailureListener {
                            helperMessage = "We couldn't send a reset link right now. Please try again."
                        }
                }
            )

            Spacer(modifier = Modifier.height(28.dp.adaptive(adaptiveUi)))

            Button(
                onClick = {
                    if (isLoading) return@Button

                    errorMessage = null
                    helperMessage = null

                    if (!Patterns.EMAIL_ADDRESS.matcher(email).matches() || password.isBlank()) {
                        errorMessage = "Incorrect credentials"
                        return@Button
                    }

                    isLoading = true
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnSuccessListener {
                            isLoading = false
                            onLoginSuccess()
                        }
                        .addOnFailureListener {
                            isLoading = false
                            errorMessage = "Incorrect credentials"
                        }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp.adaptive(adaptiveUi)),
                shape = RoundedCornerShape(20.dp.adaptive(adaptiveUi)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DeepCharcoal,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = if (isLoading) "Signing in..." else "Login",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp.adaptive(adaptiveUi)
                    )
                )
            }

            Spacer(modifier = Modifier.height(26.dp.adaptive(adaptiveUi)))

            Row {
                Text(
                    text = "Don't have an account? ",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = DeepCharcoal.copy(alpha = 0.42f),
                        fontSize = 14.sp.adaptive(adaptiveUi)
                    )
                )

                Text(
                    text = "Sign up",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = ChampagneGold,
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = TextDecoration.Underline,
                        fontSize = 14.sp.adaptive(adaptiveUi)
                    ),
                    modifier = Modifier.clickable { onSignUp() }
                )
            }

            Spacer(modifier = Modifier.height(32.dp.adaptive(adaptiveUi)))
        }
    }
}

@Composable
private fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType,
    adaptiveUi: AdaptiveUi,
    isPassword: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(22.dp.adaptive(adaptiveUi)),
        textStyle = TextStyle(
            color = DeepCharcoal,
            fontSize = 16.sp.adaptive(adaptiveUi),
            fontFamily = RefinedSerif
        ),
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = DeepCharcoal.copy(alpha = 0.5f),
                    fontSize = 14.sp.adaptive(adaptiveUi)
                )
            )
        },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = if (isPassword) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = ChampagneGold.copy(alpha = 0.7f),
            unfocusedBorderColor = DeepCharcoal.copy(alpha = 0.18f),
            cursorColor = ChampagneGold,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White
        )
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun EmailLoginScreenPreview() {
    SavaTheme {
        EmailLoginScreen(
            onLoginSuccess = {},
            onBack = {},
            onSignUp = {}
        )
    }
}
