package com.example.sava.auth

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

object UserProfileStore {
    fun upsertUserProfile(
        user: FirebaseUser,
        provider: String,
        displayNameOverride: String? = null,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val resolvedName = displayNameOverride?.takeIf { it.isNotBlank() } ?: user.displayName
        val userData = hashMapOf<String, Any?>(
            "uid" to user.uid,
            "displayName" to resolvedName,
            "email" to user.email,
            "photoUrl" to user.photoUrl?.toString(),
            "provider" to provider,
            "createdAt" to (user.metadata?.creationTimestamp ?: System.currentTimeMillis()),
            "lastLoginAt" to FieldValue.serverTimestamp()
        )

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(user.uid)
            .set(userData, SetOptions.merge())
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Signed in, but storing the user profile failed.")
            }
    }

    fun updateInvestableCorpusForCurrentUser(
        investableCorpusPerMonth: Long,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            onError("No signed-in user found for corpus storage.")
            return
        }

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(user.uid)
            .set(
                mapOf(
                    "uid" to user.uid,
                    "investableCorpusPerMonth" to investableCorpusPerMonth,
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
            )
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Could not store investable corpus.")
            }
    }
}
