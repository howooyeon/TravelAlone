package com.guru.travelalone

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object CounterHelper {
    private val db = FirebaseFirestore.getInstance()
    private const val COUNTERS_COLLECTION = "counters"
    private const val MEMBERS_COUNTER_DOC = "members_counter"
    private const val BUDGET_CHARGES_COUNTER_DOC = "budget_charges_counter"
    private const val BUDGET_SPENDS_COUNTER_DOC ="budget_spends_counter"

    suspend fun getNextMemberId(): Long {
        val counterRef = db.collection(COUNTERS_COLLECTION).document(MEMBERS_COUNTER_DOC)

        return db.runTransaction { transaction ->
            val snapshot = transaction.get(counterRef)
            val newCount = if (snapshot.exists()) {
                val currentCount = snapshot.getLong("count") ?: 0L
                currentCount + 1
            } else {
                1L
            }
            transaction.set(counterRef, mapOf("count" to newCount))
            newCount
        }.await()
    }

    suspend fun getChargeId(): Long {
        val counterRef = db.collection(COUNTERS_COLLECTION).document(BUDGET_CHARGES_COUNTER_DOC)

        return db.runTransaction { transaction ->
            val snapshot = transaction.get(counterRef)
            val newCount = if (snapshot.exists()) {
                val currentCount = snapshot.getLong("count") ?: 0L
                currentCount + 1
            } else {
                1L
            }
            transaction.set(counterRef, mapOf("count" to newCount))
            newCount
        }.await()
    }

    suspend fun getSpendId(): Long {
        val counterRef = db.collection(COUNTERS_COLLECTION).document(BUDGET_SPENDS_COUNTER_DOC)

        return db.runTransaction { transaction ->
            val snapshot = transaction.get(counterRef)
            val newCount = if (snapshot.exists()) {
                val currentCount = snapshot.getLong("count") ?: 0L
                currentCount + 1
            } else {
                1L
            }
            transaction.set(counterRef, mapOf("count" to newCount))
            newCount
        }.await()
    }
}
