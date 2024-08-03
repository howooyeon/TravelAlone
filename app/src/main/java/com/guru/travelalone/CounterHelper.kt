package com.guru.travelalone

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

//데이터 베이스 각 도큐먼트 id 값 부텨 (구별에 용이하기 위해)
object CounterHelper {
    private val db = FirebaseFirestore.getInstance()
    private const val COUNTERS_COLLECTION = "counters"
    private const val MEMBERS_COUNTER_DOC = "members_counter"
    private const val BUDGET_CHARGES_COUNTER_DOC = "budget_charges_counter"
    private const val BUDGET_SPENDS_COUNTER_DOC ="budget_spends_counter"

    //유저 id
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

    //충전 id
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

    //지출 id
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
