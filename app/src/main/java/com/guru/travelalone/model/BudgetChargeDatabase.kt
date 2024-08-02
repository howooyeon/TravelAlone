package com.guru.travelalone

import com.google.firebase.Timestamp
import java.util.Date

data class BudgetCharge(
    val id: Long = 0,
    val user_uid: String = "",
    val budget_id: Long = 0,
    val charge_amount: String = "",
    val charge_memo: String = "",
    val charge_date: Date
) {
    // 기본 생성자
    constructor() : this(0, "", 0, "", "",Date())
}

