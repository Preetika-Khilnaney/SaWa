package com.example.sava.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

data class RiskAssessmentRecord(
    val uid: String,
    val age: Int,
    val investableCorpusPerMonth: Long,
    val behaviouralRiskProfile: String,
    val riskCapacity: String,
    val optimumRiskCapability: String
)

object RiskAssessmentStore {
    fun saveLatestAssessment(
        age: Int,
        investableCorpusPerMonth: Long,
        behaviouralRiskProfile: String,
        riskCapacity: String,
        optimumRiskCapability: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            onError("No signed-in user found for risk assessment storage.")
            return
        }

        val assessmentData = hashMapOf<String, Any?>(
            "uid" to user.uid,
            "displayName" to user.displayName,
            "email" to user.email,
            "age" to age,
            "investableCorpusPerMonth" to investableCorpusPerMonth,
            "behaviouralRiskProfile" to behaviouralRiskProfile,
            "riskCapacity" to riskCapacity,
            "optimumRiskCapability" to optimumRiskCapability,
            "updatedAt" to FieldValue.serverTimestamp()
        )

        val firestore = FirebaseFirestore.getInstance()

        firestore
            .collection("risk_assessments")
            .document(user.uid)
            .set(assessmentData, SetOptions.merge())
            .addOnSuccessListener {
                saveAssessmentHistory(
                    firestore = firestore,
                    userId = user.uid,
                    assessmentData = assessmentData,
                    onSuccess = onSuccess,
                    onError = onError
                )
            }
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Could not store the risk assessment result.")
            }
    }

    private fun saveAssessmentHistory(
        firestore: FirebaseFirestore,
        userId: String,
        assessmentData: Map<String, Any?>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val historyData = assessmentData.toMutableMap().apply {
            put("userId", userId)
            put("createdAt", FieldValue.serverTimestamp())
        }

        firestore
            .collection("risk_assessment_history")
            .add(historyData)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Latest assessment was saved, but history storage failed.")
            }
    }

    fun fetchLatestAssessment(
        onFound: (RiskAssessmentRecord) -> Unit,
        onNotFound: () -> Unit,
        onError: (String) -> Unit = {}
    ) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            onNotFound()
            return
        }

        FirebaseFirestore.getInstance()
            .collection("risk_assessments")
            .document(user.uid)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.exists()) {
                    onNotFound()
                    return@addOnSuccessListener
                }

                val age = snapshot.getLong("age")?.toInt()
                val investableCorpusPerMonth = snapshot.getLong("investableCorpusPerMonth")
                val behaviouralRiskProfile = snapshot.getString("behaviouralRiskProfile")
                val riskCapacity = snapshot.getString("riskCapacity")
                val optimumRiskCapability = snapshot.getString("optimumRiskCapability")

                if (
                    age == null ||
                    investableCorpusPerMonth == null ||
                    behaviouralRiskProfile.isNullOrBlank() ||
                    riskCapacity.isNullOrBlank() ||
                    optimumRiskCapability.isNullOrBlank()
                ) {
                    onNotFound()
                    return@addOnSuccessListener
                }

                onFound(
                    RiskAssessmentRecord(
                        uid = user.uid,
                        age = age,
                        investableCorpusPerMonth = investableCorpusPerMonth,
                        behaviouralRiskProfile = behaviouralRiskProfile,
                        riskCapacity = riskCapacity,
                        optimumRiskCapability = optimumRiskCapability
                    )
                )
            }
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Could not fetch the saved risk assessment.")
            }
    }
}
