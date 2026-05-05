package com.example.sava.auth

import androidx.activity.ComponentActivity
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.example.sava.R
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

object GoogleAuthManager {
    fun signIn(
        activity: ComponentActivity,
        onSuccess: (FirebaseUser) -> Unit,
        onError: (String) -> Unit
    ) {
        val webClientId = resolveWebClientId(activity)
        if (webClientId.isBlank() || webClientId == "YOUR_WEB_CLIENT_ID") {
            onError(
                "Google Sign-In is not fully configured yet. Enable Google in Firebase Auth, add your SHA keys, then update google-services.json."
            )
            return
        }

        val credentialManager = CredentialManager.create(activity)
        val googleIdOption = GetSignInWithGoogleOption.Builder(
            serverClientId = webClientId
        )
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        activity.lifecycleScope.launch {
            try {
                val result = credentialManager.getCredential(
                    context = activity,
                    request = request
                )
                handleCredentialResult(
                    credential = result.credential,
                    onSuccess = onSuccess,
                    onError = onError
                )
            } catch (exception: GetCredentialException) {
                onError(exception.message ?: "Google Sign-In was cancelled.")
            }
        }
    }

    private fun handleCredentialResult(
        credential: androidx.credentials.Credential,
        onSuccess: (FirebaseUser) -> Unit,
        onError: (String) -> Unit
    ) {
        if (
            credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            try {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                firebaseAuthWithGoogle(
                    idToken = googleIdTokenCredential.idToken,
                    onSuccess = onSuccess,
                    onError = onError
                )
            } catch (_: GoogleIdTokenParsingException) {
                onError("Could not read the Google account response.")
            }
            return
        }

        onError("No Google account credential was returned.")
    }

    private fun firebaseAuthWithGoogle(
        idToken: String,
        onSuccess: (FirebaseUser) -> Unit,
        onError: (String) -> Unit
    ) {
        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
        FirebaseAuth.getInstance()
            .signInWithCredential(firebaseCredential)
            .addOnSuccessListener { authResult ->
                val user = authResult.user
                if (user == null) {
                    onError("Signed in, but no Firebase user was returned.")
                    return@addOnSuccessListener
                }

                UserProfileStore.upsertUserProfile(
                    user = user,
                    provider = "google",
                    onSuccess = { onSuccess(user) },
                    onError = onError
                )
            }
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Firebase sign-in failed.")
            }
    }

    private fun resolveWebClientId(activity: ComponentActivity): String {
        val generatedId = activity.resources.getIdentifier(
            "default_web_client_id",
            "string",
            activity.packageName
        )

        if (generatedId != 0) {
            return activity.getString(generatedId)
        }

        return activity.getString(R.string.firebase_web_client_id_fallback)
    }

    fun signOut() {
        FirebaseAuth.getInstance().signOut()
    }
}
