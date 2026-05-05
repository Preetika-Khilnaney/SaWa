package com.example.sava.ui.screens

import android.util.Patterns
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sava.auth.EmailLinkApi
import com.example.sava.auth.EmailLinkSessionStore
import com.example.sava.auth.UserProfileStore
import com.example.sava.ui.theme.ChampagneGold
import com.example.sava.ui.theme.DeepCharcoal
import com.example.sava.ui.theme.OffWhite
import com.example.sava.ui.theme.SavaTheme
import com.example.sava.ui.theme.adaptive
import com.example.sava.ui.theme.rememberAdaptiveUi
import com.example.sava.ui.theme.AdaptiveUi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun RegistrationScreen(
    onSuccess: (String) -> Unit,
    onBack: () -> Unit
) {
    val adaptiveUi = rememberAdaptiveUi()
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }
    val scrollState = rememberScrollState()

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var isSendingLink by remember { mutableStateOf(false) }
    var hasVerifiedLink by remember { mutableStateOf(false) }
    var linkStatusMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val verifiedEmail = EmailLinkSessionStore.getVerifiedEmail(context)
        val pendingFullName = EmailLinkSessionStore.getPendingFullName(context)
        if (!pendingFullName.isNullOrBlank()) {
            fullName = pendingFullName
        }
        if (!verifiedEmail.isNullOrBlank()) {
            email = verifiedEmail
            hasVerifiedLink = true
            linkStatusMessage = "Email verified successfully for $verifiedEmail"
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OffWhite)
            .padding(24.dp.adaptive(adaptiveUi))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            SecondaryScreenBackButton(onClick = onBack)
            Spacer(modifier = Modifier.height(60.dp.adaptive(adaptiveUi)))
            Text(
                text = "Join SaVa",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 40.sp.adaptive(adaptiveUi)
                ),
                color = DeepCharcoal
            )
            Spacer(modifier = Modifier.height(40.dp.adaptive(adaptiveUi)))

            WhipAnimatedField(
                label = "Full Name",
                value = fullName,
                delayMillis = 0,
                adaptiveUi = adaptiveUi,
                onValueChange = {
                    fullName = it
                    EmailLinkSessionStore.savePendingFullName(context, it)
                }
            )
            EmailLinkField(
                label = "Email",
                value = email,
                delayMillis = 50,
                isSendingLink = isSendingLink,
                adaptiveUi = adaptiveUi,
                onValueChange = {
                    email = it.trim()
                    hasVerifiedLink = false
                    linkStatusMessage = null
                    if (EmailLinkSessionStore.getVerifiedEmail(context) != email) {
                        EmailLinkSessionStore.clearVerifiedEmail(context)
                    }
                },
                onSendLink = {
                    if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        Toast.makeText(
                            context,
                            "Please enter a valid email address first.",
                            Toast.LENGTH_LONG
                        ).show()
                        return@EmailLinkField
                    }

                    isSendingLink = true
                    EmailLinkSessionStore.savePendingEmail(context, email)
                    EmailLinkSessionStore.savePendingFullName(context, fullName)
                    EmailLinkSessionStore.clearVerifiedEmail(context)
                    CoroutineScope(Dispatchers.IO).launch {
                        val result = EmailLinkApi.sendSignInLink(email, fullName)
                        launch(Dispatchers.Main) {
                            isSendingLink = false
                            result
                                .onSuccess {
                                    hasVerifiedLink = false
                                    linkStatusMessage = "Verification link sent to $email"
                                }
                                .onFailure { exception ->
                                    hasVerifiedLink = false
                                    linkStatusMessage = null
                                    Toast.makeText(
                                        context,
                                        exception.message ?: "We couldn't send the link right now.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                        }
                    }
                }
            )

            if (linkStatusMessage != null) {
                Text(
                    text = linkStatusMessage.orEmpty(),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = ChampagneGold,
                        fontSize = 12.sp.adaptive(adaptiveUi)
                    ),
                    modifier = Modifier.padding(top = 6.dp.adaptive(adaptiveUi))
                )
            }
            if (hasVerifiedLink) {
                WhipAnimatedField(
                    label = "Password",
                    value = password,
                    delayMillis = 100,
                    keyboardType = KeyboardType.Password,
                    isPassword = true,
                    adaptiveUi = adaptiveUi,
                    onValueChange = { password = it }
                )

                val isPasswordReady = password.length >= 8
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .blur(if (isPasswordReady) 0.dp else 8.dp)
                ) {
                    WhipAnimatedField(
                        label = "Confirm Password",
                        value = confirmPassword,
                        delayMillis = 150,
                        keyboardType = KeyboardType.Password,
                        isPassword = true,
                        adaptiveUi = adaptiveUi,
                        onValueChange = { confirmPassword = it }
                    )
                }
            } else {
                Text(
                    text = "Verify your email first to unlock password setup.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = DeepCharcoal.copy(alpha = 0.62f),
                        fontSize = 13.sp.adaptive(adaptiveUi)
                    ),
                    modifier = Modifier.padding(top = 24.dp.adaptive(adaptiveUi))
                )
            }

            Spacer(modifier = Modifier.height(100.dp.adaptive(adaptiveUi)))
        }

        val buttonSize = 64.dp.adaptive(adaptiveUi)
        val buttonWidth by animateDpAsState(
            targetValue = if (isSubmitting) 400.dp.adaptive(adaptiveUi) else buttonSize,
            label = "buttonWidth"
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 40.dp.adaptive(adaptiveUi))
                .size(width = buttonWidth, height = buttonSize)
                .clip(if (isSubmitting) RoundedCornerShape(0.dp) else CircleShape)
                .background(ChampagneGold)
                .clickable {
                    if (isSubmitting) return@clickable

                    val validationMessage = validateRegistrationFields(
                        fullName = fullName,
                        email = email,
                        password = password,
                        confirmPassword = confirmPassword,
                        hasVerifiedLink = hasVerifiedLink
                    )

                    if (validationMessage != null) {
                        Toast.makeText(context, validationMessage, Toast.LENGTH_LONG).show()
                        return@clickable
                    }

                    isSubmitting = true
                    val currentUser = auth.currentUser
                    if (currentUser == null || !email.equals(currentUser.email, ignoreCase = true)) {
                        isSubmitting = false
                        Toast.makeText(
                            context,
                            "Please open the verification link from your email before creating the profile.",
                            Toast.LENGTH_LONG
                        ).show()
                        return@clickable
                    }

                    currentUser.updatePassword(password)
                        .addOnSuccessListener {
                            val profileRequest = UserProfileChangeRequest.Builder()
                                .setDisplayName(fullName.trim())
                                .build()

                            currentUser.updateProfile(profileRequest)
                                .addOnCompleteListener {
                                    UserProfileStore.upsertUserProfile(
                                        user = currentUser,
                                        provider = "password",
                                        displayNameOverride = fullName.trim(),
                                        onSuccess = {
                                            auth.signOut()
                                            EmailLinkSessionStore.clearAll(context)
                                            isSubmitting = false
                                            onSuccess(email.trim())
                                        },
                                        onError = { message ->
                                            isSubmitting = false
                                            Toast.makeText(
                                                context,
                                                message,
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    )
                                }
                        }
                        .addOnFailureListener { exception ->
                            isSubmitting = false
                            Toast.makeText(
                                context,
                                exception.message ?: "Could not complete your profile.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                },
            contentAlignment = Alignment.Center
        ) {
            if (!isSubmitting) {
                Text(
                    "→", 
                    color = OffWhite, 
                    fontSize = 24.sp.adaptive(adaptiveUi), 
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun WhipAnimatedField(
    label: String,
    value: String,
    delayMillis: Int,
    adaptiveUi: AdaptiveUi,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    onValueChange: (String) -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(delayMillis.toLong())
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = androidx.compose.animation.slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp.adaptive(adaptiveUi))
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(1.dp.adaptive(adaptiveUi))
                        .background(DeepCharcoal.copy(alpha = 0.3f))
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(ChampagneGold.copy(alpha = 0.1f))
                            .padding(horizontal = 6.dp.adaptive(adaptiveUi), vertical = 2.dp.adaptive(adaptiveUi))
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = ChampagneGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp.adaptive(adaptiveUi)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp.adaptive(adaptiveUi)))

                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = DeepCharcoal,
                            fontSize = 16.sp.adaptive(adaptiveUi)
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                        visualTransformation = if (isPassword) {
                            PasswordVisualTransformation()
                        } else {
                            VisualTransformation.None
                        }
                    )
                }
            }
        }
    }
}

private fun validateRegistrationFields(
    fullName: String,
    email: String,
    password: String,
    confirmPassword: String,
    hasVerifiedLink: Boolean
): String? {
    if (fullName.trim().isEmpty()) return "Please enter your full name."
    if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) return "Please enter a valid email address."
    if (!hasVerifiedLink) return "Please verify your email by opening the secure link first."
    if (password.length < 8) return "Password must be at least 8 characters long."
    if (confirmPassword != password) return "Passwords do not match."
    return null
}

@Composable
private fun EmailLinkField(
    label: String,
    value: String,
    delayMillis: Int,
    isSendingLink: Boolean,
    adaptiveUi: AdaptiveUi,
    onValueChange: (String) -> Unit,
    onSendLink: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(delayMillis.toLong())
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = androidx.compose.animation.slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp.adaptive(adaptiveUi))
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(1.dp.adaptive(adaptiveUi))
                        .background(DeepCharcoal.copy(alpha = 0.3f))
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(ChampagneGold.copy(alpha = 0.1f))
                            .padding(horizontal = 6.dp.adaptive(adaptiveUi), vertical = 2.dp.adaptive(adaptiveUi))
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = ChampagneGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp.adaptive(adaptiveUi)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp.adaptive(adaptiveUi)))

                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        modifier = Modifier.weight(1f),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = DeepCharcoal,
                            fontSize = 16.sp.adaptive(adaptiveUi)
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        visualTransformation = VisualTransformation.None
                    )

                    Text(
                        text = if (isSendingLink) "Sending..." else "Send Link",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = ChampagneGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp.adaptive(adaptiveUi)
                        ),
                        modifier = Modifier
                            .clickable(enabled = !isSendingLink) { onSendLink() }
                            .padding(start = 12.dp.adaptive(adaptiveUi))
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun RegistrationScreenPreview() {
    SavaTheme {
        RegistrationScreen(onSuccess = {}, onBack = {})
    }
}
