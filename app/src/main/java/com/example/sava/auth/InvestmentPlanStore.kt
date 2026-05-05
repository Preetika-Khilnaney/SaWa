package com.example.sava.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

data class SavedPlanGoal(
    val goalName: String,
    val expectedEarnings: Double,
    val timeAvailableYears: Double,
    val annualRate: Double,
    val fundType: String,
    val monthlySip: Double
)

data class SavedInvestmentPlan(
    val id: String,
    val documentId: String,
    val uid: String,
    val optimumRiskProfile: String,
    val age: Int,
    val investableCorpusPerMonth: Double,
    val allocatedSip: Double,
    val remainingCorpus: Double,
    val goals: List<SavedPlanGoal>,
    val createdAtMillis: Long,
    val hiddenKey: String,
    val storagePath: String
)

object InvestmentPlanStore {
    private const val COLLECTION = "investment_plans"
    private const val USERS_COLLECTION = "users"
    private const val HIDDEN_PLAN_KEYS_FIELD = "hiddenPlanKeys"

    private fun userPlansPath(uid: String): String = "$USERS_COLLECTION/$uid/$COLLECTION"

    fun savePlan(
        optimumRiskProfile: String,
        age: Int,
        investableCorpusPerMonth: Double,
        allocatedSip: Double,
        remainingCorpus: Double,
        goals: List<SavedPlanGoal>,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            onError("No signed-in user found for plan storage.")
            return
        }

        val payload = hashMapOf<String, Any?>(
            "uid" to user.uid,
            "displayName" to user.displayName,
            "email" to user.email,
            "optimumRiskProfile" to optimumRiskProfile,
            "age" to age,
            "investableCorpusPerMonth" to investableCorpusPerMonth,
            "allocatedSip" to allocatedSip,
            "remainingCorpus" to remainingCorpus,
            "goalCount" to goals.size,
            "goals" to goals.map { goal ->
                mapOf(
                    "goalName" to goal.goalName,
                    "expectedEarnings" to goal.expectedEarnings,
                    "timeAvailableYears" to goal.timeAvailableYears,
                    "annualRate" to goal.annualRate,
                    "fundType" to goal.fundType,
                    "monthlySip" to goal.monthlySip
                )
            },
            "createdAt" to FieldValue.serverTimestamp()
        )

        FirebaseFirestore.getInstance()
            .collection(USERS_COLLECTION)
            .document(user.uid)
            .collection(COLLECTION)
            .add(payload)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Could not save the investment plan.")
            }
    }

    fun fetchPlansForCurrentUser(
        onSuccess: (List<SavedInvestmentPlan>) -> Unit,
        onError: (String) -> Unit = {}
    ) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            onSuccess(emptyList())
            return
        }

        val firestore = FirebaseFirestore.getInstance()
        firestore
            .collection(USERS_COLLECTION)
            .document(user.uid)
            .get()
            .addOnSuccessListener { userSnapshot ->
                val hiddenKeys = (userSnapshot.get(HIDDEN_PLAN_KEYS_FIELD) as? List<*>)
                    ?.mapNotNull { it as? String }
                    ?.toSet()
                    .orEmpty()

                firestore
                    .collection(USERS_COLLECTION)
                    .document(user.uid)
                    .collection(COLLECTION)
                    .get()
                    .addOnSuccessListener { scopedSnapshot ->
                        val scopedPlans = scopedSnapshot.documents.mapNotNull { document ->
                            document.toSavedInvestmentPlan(
                                userId = user.uid,
                                hiddenKey = "scoped:${document.id}",
                                storagePath = userPlansPath(user.uid)
                            )
                        }

                        firestore
                            .collection(COLLECTION)
                            .whereEqualTo("uid", user.uid)
                            .get()
                            .addOnSuccessListener { legacySnapshot ->
                                val legacyPlans = legacySnapshot.documents.mapNotNull { document ->
                                    document.toSavedInvestmentPlan(
                                        userId = user.uid,
                                        hiddenKey = "legacy:${document.id}",
                                        storagePath = COLLECTION
                                    )
                                }

                                val plans = (scopedPlans + legacyPlans)
                                    .filterNot { it.hiddenKey in hiddenKeys }
                                    .distinctBy { it.hiddenKey }
                                    .sortedByDescending { it.createdAtMillis }

                                onSuccess(plans)
                            }
                            .addOnFailureListener {
                                val plans = scopedPlans
                                    .filterNot { it.hiddenKey in hiddenKeys }
                                    .distinctBy { it.hiddenKey }
                                    .sortedByDescending { it.createdAtMillis }

                                onSuccess(plans)
                            }
                    }
                    .addOnFailureListener { exception ->
                        onError(exception.message ?: "Could not fetch saved investment plans.")
                    }
            }
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Could not fetch saved investment plans.")
            }
    }

    fun deletePlan(
        plan: SavedInvestmentPlan,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            onError("No signed-in user found for plan deletion.")
            return
        }

        val firestore = FirebaseFirestore.getInstance()
        firestore
            .collection(plan.storagePath)
            .document(plan.documentId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception ->
                firestore
                    .collection(USERS_COLLECTION)
                    .document(user.uid)
                    .set(
                        mapOf(
                            HIDDEN_PLAN_KEYS_FIELD to FieldValue.arrayUnion(plan.hiddenKey)
                        ),
                        SetOptions.merge()
                    )
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { fallbackException ->
                        onError(
                            fallbackException.message
                                ?: exception.message
                                ?: "Could not delete the investment plan."
                        )
                    }
            }
    }

    private fun DocumentSnapshot.toSavedInvestmentPlan(
        userId: String,
        hiddenKey: String,
        storagePath: String
    ): SavedInvestmentPlan? {
        val goals = (get("goals") as? List<*>)
            ?.mapNotNull { goalItem ->
                val goalMap = goalItem as? Map<*, *> ?: return@mapNotNull null
                val goalName = goalMap["goalName"] as? String ?: return@mapNotNull null
                val expectedEarnings = (goalMap["expectedEarnings"] as? Number)?.toDouble() ?: return@mapNotNull null
                val timeAvailableYears = (goalMap["timeAvailableYears"] as? Number)?.toDouble() ?: return@mapNotNull null
                val annualRate = (goalMap["annualRate"] as? Number)?.toDouble() ?: return@mapNotNull null
                val fundType = goalMap["fundType"] as? String ?: return@mapNotNull null
                val monthlySip = (goalMap["monthlySip"] as? Number)?.toDouble() ?: return@mapNotNull null
                SavedPlanGoal(
                    goalName = goalName,
                    expectedEarnings = expectedEarnings,
                    timeAvailableYears = timeAvailableYears,
                    annualRate = annualRate,
                    fundType = fundType,
                    monthlySip = monthlySip
                )
            }
            .orEmpty()

        val profile = getString("optimumRiskProfile") ?: return null
        val age = getLong("age")?.toInt() ?: return null
        val corpus = (getDouble("investableCorpusPerMonth")
            ?: getLong("investableCorpusPerMonth")?.toDouble()) ?: return null
        val allocatedSip = (getDouble("allocatedSip")
            ?: getLong("allocatedSip")?.toDouble()) ?: return null
        val remainingCorpus = (getDouble("remainingCorpus")
            ?: getLong("remainingCorpus")?.toDouble()) ?: return null
        val createdAtMillis = getTimestamp("createdAt")?.toDate()?.time ?: 0L

        return SavedInvestmentPlan(
            id = hiddenKey,
            documentId = id,
            uid = userId,
            optimumRiskProfile = profile,
            age = age,
            investableCorpusPerMonth = corpus,
            allocatedSip = allocatedSip,
            remainingCorpus = remainingCorpus,
            goals = goals,
            createdAtMillis = createdAtMillis,
            hiddenKey = hiddenKey,
            storagePath = storagePath
        )
    }
}
