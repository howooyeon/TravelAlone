package com.guru.travelalone

import java.util.Date

data class Transaction(
    val id: Long,
    val user_uid: String,
    val amount: String,
    val category: String?,
    val store: String?,
    val memo: String,
    val date: Date,
    val type: TransactionType
)

enum class TransactionType {
    CHARGE, SPEND
}
