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

//충전 / 지출 두 종류로 나뉨
enum class TransactionType {
    CHARGE, SPEND
}
